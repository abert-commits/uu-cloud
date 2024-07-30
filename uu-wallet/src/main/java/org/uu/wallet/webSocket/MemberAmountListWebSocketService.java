package org.uu.wallet.webSocket;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.wallet.config.CustomConfigurator;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.req.AmountListReq;
import org.uu.wallet.service.impl.MemberInfoServiceImpl;
import org.uu.wallet.util.JsonUtil;
import org.uu.wallet.util.SpringContextUtil;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @ServerEndpoint 注解的作用
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */

@Slf4j
@Component
@ServerEndpoint(value = "/websocket/getAmountList/{userId}", configurator = CustomConfigurator.class)

public class MemberAmountListWebSocketService {


    // 这个方法将由 WebSocketServiceInitializer 调用
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        init();
    }

    private RedisTemplate<String, String> redisTemplate;

    // Redis消息监听器和发布者
    private RedisMessageListenerContainer redisContainer;
    private RedisPublisher redisPublisher;


    // 初始化方法
    private void init() {
        // 确保 redisTemplate 不为 null
        if (this.redisTemplate != null) {
            this.redisPublisher = new RedisPublisher(this.redisTemplate, "websocketChannelAmountList");
            this.redisContainer = SpringContextUtil.getBean(RedisMessageListenerContainer.class);
            this.redisContainer.addMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {

                    //log.info("[推送买入金额列表]收到频道上的消息时, message: {}", message);

                    // 当收到频道上的消息时执行
                    handleMessageFromRedis(new String(message.getBody()));
                }
            }, new ChannelTopic("websocketChannelAmountList"));
        }
    }

    // 处理从Redis接收的消息
    private void handleMessageFromRedis(String message) {

        //log.info("[推送买入金额列表]处理从Redis接收的消息, message: {}", message);

        String jsonMessage = message.trim();
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = jsonMessage.substring(1, jsonMessage.length() - 1);
            // 对 jsonMessage 进行解码（如果它被额外转义）
            jsonMessage = StringEscapeUtils.unescapeJava(jsonMessage);
        }

        //判断是否是json格式
        if (JsonUtil.isValidJSONObjectOrArray(jsonMessage)) {

            //log.info("[推送买入金额列表]处理从Redis接收的消息, 消息为json格式, message: {}", jsonMessage);

            //将消息转为json对象
            JSONObject jsonData = JSON.parseObject(jsonMessage);

            if (jsonData == null) {
                log.error("[推送买入金额列表]处理从Redis接收的消息 jsonData为null, message: {}, jsonMessage: {}", message, jsonMessage);
                return;
            }

            //获取会员id
            String userId = (String) jsonData.get("userId");

            if (StringUtils.isEmpty(userId)) {
                log.error("[推送买入金额列表]处理从Redis接收的消息 userId为null, message: {}, jsonMessage: {}", message, jsonMessage);
                return;
            }

            //获取金额列表
            Object buyListObj = jsonData.get("buyList");

            if (buyListObj instanceof JSONArray) {

                JSONArray messageArray = (JSONArray) buyListObj;

                //log.info("[推送买入金额列表]处理从Redis接收的消息, 解析 JSON 数组, messageArray: {}", messageArray);

                try {
                    //判断当前连接是否存在 如存在 才进行推送
                    if (webSocketSet.get(userId) != null) {
                        webSocketSet.get(userId).session.getBasicRemote().sendText(String.valueOf(messageArray));
                        log.info("[推送买入金额列表]webSocket发送指定消息成功: userId: {}", userId);
                    }
                } catch (Exception e) {
                    log.info("[推送买入金额列表]webSocket发送指定消息失败: userId: {}, message: {}", userId, jsonMessage);
                    e.printStackTrace();
                }
            } else {
                log.error("[推送买入金额列表]处理从Redis接收的消息 解析JSON数组失败, message: {}, buyListObj: {}", jsonMessage, buyListObj);
            }
        } else {
            log.error("[推送买入金额列表]处理从Redis接收的消息 解析JSON失败, message: {}, jsonMessage: {}", message, jsonMessage);
        }
    }

    // 改写发送消息的部分，将消息发布到Redis而不是直接发送
    public boolean AppointSending(String jsonMsg) {
        try {
            // 将消息发布到Redis频道
            redisPublisher.publish("websocketChannelAmountList", jsonMsg);
            //log.info("消息发布到Redis频道成功: {}", jsonMsg);
            return true;
        } catch (Exception e) {
            log.error("发布到Redis频道失败: {}", jsonMsg, e);
            return false;
        }
    }

    // Redis发布者
    public static class RedisPublisher {
        private RedisTemplate<String, String> template;
        private String channel;

        public RedisPublisher(RedisTemplate<String, String> template, String channel) {
            this.template = template;
            this.channel = channel;
        }

        public void publish(String channel, String message) {
            template.convertAndSend(channel, message);
        }
    }


    /**
     * 用于存所有的连接服务的客户端，这个对象存储是安全的
     * 这里的v (用来存放每个客户端对应的WebSocket对象)
     */
    private static ConcurrentHashMap<String, MemberAmountListWebSocketService> webSocketSet = new ConcurrentHashMap<>();

    /**
     * 与某个客户端的连接对话，需要通过它来给客户端发送消息
     */
    private Session session;
    /**
     * 标识当前连接客户端的用户名
     */
    private String userId;

    /**
     * 连接建立成功调用的方法
     * session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void OnOpen(Session session, @PathParam(value = "userId") String userId) {
        this.session = session;
        this.userId = userId;
        // userId是用来表示唯一客户端，如果需要指定发送，需要指定发送通过userId来区分
        webSocketSet.put(userId, this);
        log.info("[推送买入金额列表]webSocket连接成功，当前连接人数为：{}, userId: {}", webSocketSet.size(), userId);

//        GroupSending(userId + " 来了");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void OnClose() {
        webSocketSet.remove(this.userId);
        log.info("[推送买入金额列表][WebSocket] 退出成功，当前连接人数为：={}", webSocketSet.size());

//        GroupSending(userId + " 走了");

        //删除充值金额和webSocketID
        RedisTemplate redisTemplate = SpringContextUtil.getBean("redisTemplate");
        redisTemplate.boundHashOps(RedisKeys.AMOUNT_LIST_REQ).delete(userId);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void OnMessage(String message_str) {

        // 检查是否为心跳消息
        if (isHeartbeatMessage(message_str)) {
            // 心跳消息，不执行任何操作
            return;
        }

        //log.info("[钱包买入金额列表WebSocket] 收到消息：{}", message_str);

        //判断消息是不是json格式
        if (JsonUtil.isValidJSONObjectOrArray(message_str)) {

            //将消息数据转为实体对象
            AmountListReq amountListReq = JSON.parseObject(message_str, AmountListReq.class);

            if (StringUtils.isEmpty(amountListReq.getPaymentType())) {
                amountListReq.setPaymentType(null);
            }

            if (amountListReq == null) {
                log.error("[推送买入金额列表]webSocket收到客户端消息处理失败: amountListReq 为null");
                return;
            }

            //校验最小金额是否大于最大金额
            if (amountListReq.getMinimumAmount() != null && amountListReq.getMaximumAmount() != null && amountListReq.getMinimumAmount().compareTo(amountListReq.getMaximumAmount()) > 0) {
                log.error("[推送买入金额列表]WebSocket最小金额不能大于最大金额: {}", message_str);
                return;
            }

            //获取会员id
            String userId = amountListReq.getUserId();

            //校验会员ID是否合法
            if (StringUtils.isEmpty(userId) || webSocketSet.get(userId) == null) {
                log.error("[推送买入金额列表]WebSocket会员id错误");
                return;
            }

            MemberInfoServiceImpl memberInfoService = SpringContextUtil.getBean(MemberInfoServiceImpl.class);
            //查询是否存在该用户
            MemberInfo memberInfo = memberInfoService.getById(userId);

            if (memberInfo == null) {
                log.error("[推送买入金额列表]WebSocket会员id不存在");
                return;
            }

            //记录最小金额,最大金额和webSocketID (记录到Redis)
            RedisTemplate redisTemplate = SpringContextUtil.getBean("redisTemplate");

            if (redisTemplate == null) {
                log.error("[推送买入金额列表]webSocket收到客户端消息处理失败: RedisTemplate 未初始化");
                return;
            }

            try {
                BoundHashOperations userIdAndCollectionAmount = redisTemplate.boundHashOps(RedisKeys.AMOUNT_LIST_REQ);

                String amountListReqJson = JSON.toJSONString(amountListReq, SerializerFeature.WriteMapNullValue);

                //存入会员ID 最小金额 最大金额 页码 支付类型
                userIdAndCollectionAmount.put(userId, amountListReqJson);
                //默认过期时间为30分钟
                userIdAndCollectionAmount.expire(30, TimeUnit.MINUTES);

                log.info("[推送买入金额列表]记录用户id, 最小金额, 最大金额到Redis: userId: {}, message: {}", userId, JSON.toJSONString(amountListReq, SerializerFeature.WriteMapNullValue));

            } catch (Exception e) {
                log.error("[推送买入金额列表]记录用户id, 最小金额, 最大金额到Redis失败, 用户id: {}, e: {}", userId, e);
            }
        } else if ("connected".equals(message_str)) {
//            log.info("收到前端webSocket心跳: ", message_str);
        } else {
            log.error("[推送买入金额列表]消息格式不正确: ", message_str);
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        try {
            session.close();
            log.info("[推送买入金额列表]webSocket发生错误");
            error.printStackTrace();
        } catch (Exception e) {

        }
    }

    /**
     * 群发
     *
     * @param message
     */
    public void GroupSending(String message) {
        for (String userId : webSocketSet.keySet()) {
            try {
                webSocketSet.get(userId).session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 指定发送
     *
     * @param userId
     * @param message
     */
//    public boolean AppointSending(String userId, String message) {
//        try {
//
//            //判断当前连接是否存在 如存在 才进行推送
//            if (webSocketSet.get(userId) == null) {
//                //该会员已经断开连接了, 删除redis信息
//                //删除充值金额和webSocketID
//                RedisTemplate redisTemplate = SpringContextUtil.getBean("redisTemplate");
//                redisTemplate.boundHashOps(RedisKeys.AMOUNT_LIST_REQ).delete(userId);
//                return false;
//            }
//            webSocketSet.get(userId).session.getBasicRemote().sendText(message);
//            log.info("[推送买入金额列表]webSocket发送指定消息成功: userId: {}, message: {}", userId, message);
//            return true;
//        } catch (Exception e) {
//            log.info("[推送买入金额列表]webSocket发送指定消息失败: userId: {}, message: {}", userId, message);
//            e.printStackTrace();
//            return false;
//        }
//    }

    /**
     * 检查消息是否为心跳消息
     */
    private boolean isHeartbeatMessage(String message) {
        try {
            JSONObject json = JSON.parseObject(message);
            return "connected".equals(json.getString("type"));
        } catch (Exception e) {
            return false;
        }
    }
}
