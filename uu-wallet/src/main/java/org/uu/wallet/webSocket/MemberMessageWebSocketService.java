package org.uu.wallet.webSocket;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.enums.MemberWebSocketMessageTypeEnum;
import org.uu.common.core.websocket.receive.member.MemberWebSocketReceiveMessage;
import org.uu.common.core.websocket.send.member.MemberWebSocketSendMessage;
import org.uu.wallet.config.CustomConfigurator;
import org.uu.wallet.util.JsonUtil;
import org.uu.wallet.util.SpringContextUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ServerEndpoint 注解的作用
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */

@Slf4j
@Component
@ServerEndpoint(value = "/websocket/notifyMemberMessage/{userId}", configurator = CustomConfigurator.class)
public class MemberMessageWebSocketService {
    /**
     * 用于存所有的连接服务的客户端，这个对象存储是安全的
     * 这里的v (用来存放每个客户端对应的WebSocket对象)
     */
    private static final ConcurrentHashMap<String, MemberMessageWebSocketService> webSocketSet = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, MemberMessageWebSocketService>> MemberMessageType = new ConcurrentHashMap<>();

    /**
     * 与某个客户端的连接对话，需要通过它来给客户端发送消息
     */
    private Session session;
    /**
     * 标识当前连接客户端的用户名
     */
    private String userId;

    // 这个方法将由 WebSocketServiceInitializer 调用
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        init();
    }

    private RedisTemplate<String, String> redisTemplate;

    private RedisPublisher redisPublisher;

    // 初始化方法
    private void init() {
        // 确保 redisTemplate 不为 null
        if (this.redisTemplate != null) {
            this.redisPublisher = new RedisPublisher(this.redisTemplate);
            // Redis消息监听器和发布者
            RedisMessageListenerContainer redisContainer = SpringContextUtil.getBean(RedisMessageListenerContainer.class);
            redisContainer.addMessageListener((message, pattern) -> {
                // 当收到频道上的消息时执行
                handleMessageFromRedis(new String(message.getBody()));
            }, new ChannelTopic(RedisConstants.MEMBER_WEBSOCKET_MESSAGE_CHANNEL));
        }
    }


    /**
     * 处理从Redis接收的消息
     *
     * @param message 消息内容
     */
    private void handleMessageFromRedis(String message) {
        String jsonMessage = message.trim();
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = jsonMessage.substring(1, jsonMessage.length() - 1);
            // 对 jsonMessage 进行解码（如果它被额外转义）
            jsonMessage = StringEscapeUtils.unescapeJava(jsonMessage);
        }

        //判断是不是json格式
        if (JsonUtil.isValidJSONObjectOrArray(jsonMessage)) {
            if (StringUtils.isEmpty(jsonMessage) || StringUtils.isEmpty(jsonMessage.trim())) {
                log.info("[会员通知失败] 解析从Redis接收的消息为空");
                return;
            }

            // 将JSON字符串转换为 MemberWebSocketMessage 实体类
            MemberWebSocketSendMessage<?> memberWebSocketMessage = JsonUtil.fromJson(jsonMessage, MemberWebSocketSendMessage.class);
            if (Objects.nonNull(memberWebSocketMessage)) {
                //判断当前连接是否存在 如存在 才进行推送
                String memberId = memberWebSocketMessage.getMemberId();
                if (Objects.isNull(memberId) || StringUtils.isEmpty(memberId) || StringUtils.isEmpty(memberId.trim())) {
                    log.info("[会员通知失败] 未指定用户ID");
                    return;
                }
                MemberMessageWebSocketService notifyOrderStatusChangeWebSocketService = webSocketSet.get(memberId);
                if (Objects.isNull(notifyOrderStatusChangeWebSocketService)) {
                    log.info("[会员通知失败] 当前用户未连接 无法进行消息推送");
                    return;
                }
                this.sendMessageToClientByMessageType(memberId, memberWebSocketMessage.getMessageType(), jsonMessage);
                return;
            }
        }
        log.error("[订单交易状态改变通知]处理从Redis接收的消息 解析JSON失败, message: {}", jsonMessage);
    }

    public void sendMessageToClientByMessageType(
            String userId,
            String messageType,
            String jsonMessage) {
        try {
            // 校验消息类型
            MemberWebSocketMessageTypeEnum memberWebSocketMessageTypeEnum =
                    MemberWebSocketMessageTypeEnum.buildMemberWebSocketMessageTypeEnumByMessageType(messageType);
            if (Objects.isNull(memberWebSocketMessageTypeEnum)) {
                log.info("[会员通知失败] 不支持的消息类型  消息类型:{}", messageType);
            }
            // 判断当前用户是否订阅了当前消息类型
            if (isTopic(userId, memberWebSocketMessageTypeEnum)) {
                MemberMessageType.get(userId).get(messageType).session.getBasicRemote().sendText(jsonMessage);
            }
        } catch (IOException exception) {
            log.info("[会员通知失败] 消息内容: {}, 原因: {}", jsonMessage, exception.getMessage());
        }
    }

    public Boolean isTopic(String userId,
                           MemberWebSocketMessageTypeEnum messageTypeEnum) {
        return Objects.nonNull(userId) && StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(userId.trim())
                && !MemberMessageType.get(userId).isEmpty()
                && Objects.nonNull(MemberMessageType.get(userId).get(messageTypeEnum.getMessageType()));
    }

    public boolean memberMessageSendToWebSocket(String message) {
        try {
            // 将消息发布到Redis频道
            redisPublisher.publish(RedisConstants.MEMBER_WEBSOCKET_MESSAGE_CHANNEL, message);
            return true;
        } catch (Exception e) {
            log.error("发布到Redis频道失败: {}", message, e);
            return false;
        }
    }


    // Redis发布者
    public static class RedisPublisher {
        private final RedisTemplate<String, String> template;

        public RedisPublisher(RedisTemplate<String, String> template) {
            this.template = template;
        }

        public void publish(String channel, String message) {
            template.convertAndSend(channel, message);
        }
    }

    /**
     * 连接建立成功调用的方法
     * session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    @SuppressWarnings("all")
    public void OnOpen(Session session, @PathParam(value = "userId") String userId) {
        this.session = session;
        this.userId = userId;
        // userId是用来表示唯一客户端，如果需要指定发送，需要指定发送通过userId来区分
        webSocketSet.put(userId, this);
        MemberMessageType.put(userId, new ConcurrentHashMap<>());
        log.info("[订单交易状态改变通知]webSocket连接成功，当前连接人数为：{}, userId: {}", webSocketSet.size(), userId);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    @SuppressWarnings("all")
    public void OnClose() {
        webSocketSet.remove(this.userId);
        MemberMessageType.remove(this.userId);
        log.info("[订单交易状态改变通知][WebSocket] 退出成功，当前连接人数为：={}", webSocketSet.size());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    @SuppressWarnings("all")
    public void OnMessage(String messageType) {
        // 判断客户端传入的消息体是否为空
        if (StringUtils.isEmpty(messageType) || StringUtils.isEmpty(messageType.trim())) {
            log.info("订阅消息体为空");
            return;
        }

        MemberWebSocketReceiveMessage receiveMessage = JSON.parseObject(messageType, MemberWebSocketReceiveMessage.class);
        if (Objects.isNull(receiveMessage)) {
            log.info("订阅消息体为空");
            return;
        }

        MemberWebSocketMessageTypeEnum receiveMessageTypeEnum =
                MemberWebSocketMessageTypeEnum.buildMemberWebSocketMessageTypeEnumByMessageType(receiveMessage.getSubscribeType());

        if (Objects.isNull(receiveMessageTypeEnum)) {
            log.error("不支持的消息订阅类型::{}", messageType);
            return;
        }

        ConcurrentHashMap<String, MemberMessageWebSocketService> webSocketServiceConcurrentHashMap = MemberMessageType.get(this.userId);

        switch (receiveMessageTypeEnum) {
            case KEEP_LIVE:
                log.info("用户({})订阅心跳信息", this.userId);
                webSocketServiceConcurrentHashMap.put(MemberWebSocketMessageTypeEnum.KEEP_LIVE.getMessageType(), this);
                return;
            case BUY_INR:
                log.info("用户({})订阅INR买入信息", this.userId);
                webSocketServiceConcurrentHashMap.put(MemberWebSocketMessageTypeEnum.BUY_INR.getMessageType(), this);
                return;
            case BUY_USDT:
                log.info("用户({})订阅USDT买入信息", this.userId);
                webSocketServiceConcurrentHashMap.put(MemberWebSocketMessageTypeEnum.BUY_USDT.getMessageType(), this);
                return;
            case SELL_INR:
                log.info("用户({})订阅INR卖出信息", this.userId);
                webSocketServiceConcurrentHashMap.put(MemberWebSocketMessageTypeEnum.SELL_INR.getMessageType(), this);
                return;
            default:
                log.error("未知消息类型订阅");
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    @SuppressWarnings("all")
    public void onError(Session session, Throwable error) {
        try {
            session.close();
            log.info("[会员通知]webSocket发生错误 原因: {}", error.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 群发
     *
     * @param message
     */
    @SuppressWarnings("all")
    public void GroupSending(String message) {
        for (String userId : webSocketSet.keySet()) {
            try {
                webSocketSet.get(userId).session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
