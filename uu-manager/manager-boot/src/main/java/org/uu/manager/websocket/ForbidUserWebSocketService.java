package org.uu.manager.websocket;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.uu.manager.config.CustomConfigurator;
import org.uu.manager.util.JsonUtil;
import org.uu.manager.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;



@Slf4j
@Component
@ServerEndpoint(value = "/websocket/forbidUser/{userId}", configurator = CustomConfigurator.class)
//@DependsOn("redisConfig")
public class ForbidUserWebSocketService implements CommandLineRunner {


    // 这个方法将由 WebSocketServiceInitializer 调用
//    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//        init();
//    }

    @Autowired
    private RedisTemplate redisTemplate;

    // Redis消息监听器和发布者
    private RedisMessageListenerContainer redisContainer;
    private RedisPublisher redisPublisher;

    @Override
    public void run(String... args) throws Exception {
        init();
    }


    private void init() {
        // 确保 redisTemplate 不为 null
        if (this.redisTemplate != null) {
            this.redisPublisher = new RedisPublisher(this.redisTemplate, "forbidUserMsg");
            this.redisContainer = SpringContextUtil.getBean(RedisMessageListenerContainer.class);
            if (this.redisContainer != null) {
                this.redisContainer.addMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message message, byte[] pattern) {

                        log.info("[推送禁用用户消息]收到频道上的消息时, message: {}", message);

                        // 当收到频道上的消息时执行
                        handleMessageFromRedis(new String(message.getBody()));
                    }
                }, new ChannelTopic("forbidUserMsg"));
            } else {
                log.error("[推送禁用用户消息失败] redisTemplate 为null");
            }
        } else {
            log.error("[推送禁用用户消息失败] redisContainer 为null");
        }
    }


    // 处理从Redis接收的消息
    private void handleMessageFromRedis(String message) {

        log.info("[推送禁用用户消息]处理从Redis接收的消息, message: {}", message);

        String jsonMessage = message.trim();
        if (jsonMessage.startsWith("\"") && jsonMessage.endsWith("\"")) {
            jsonMessage = jsonMessage.substring(1, jsonMessage.length() - 1);
            // 对 jsonMessage 进行解码（如果它被额外转义）
            jsonMessage = StringEscapeUtils.unescapeJava(jsonMessage);
        }

        //判断是否是json格式
        if (JsonUtil.isValidJSONObjectOrArray(jsonMessage)) {

            log.info("[推送禁用用户消息]处理从Redis接收的消息, 消息为json格式, message: {}", jsonMessage);

            //将消息转为json对象
            JSONObject jsonData = JSON.parseObject(jsonMessage);

            if (jsonData == null) {
                log.error("[推送禁用用户消息]处理从Redis接收的消息 jsonData为null, message: {}, jsonMessage: {}", message, jsonMessage);
                return;
            }

            //将消息进行群发
            GroupSending(JSON.toJSONString(jsonData, SerializerFeature.WriteMapNullValue));
        } else {
            log.error("[推送禁用用户消息]处理从Redis接收的消息 解析JSON失败, message: {}, jsonMessage: {}", message, jsonMessage);
        }

    }


    // 改写发送消息的部分，将消息发布到Redis而不是直接发送
    public boolean AppointSending(String message) {
        try {
            // 将消息发布到Redis频道
            redisPublisher.publish("forbidUserMsg", message);
            log.info("消息发布到Redis频道成功: {}", message);
            return true;
        } catch (Exception e) {
            log.error("发布到Redis频道失败: {}", message, e);
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
    private static ConcurrentHashMap<String, ForbidUserWebSocketService> webSocketSet = new ConcurrentHashMap<>();

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
        log.info("[推送禁用用户消息]webSocket连接成功，当前连接人数为：{}, userId: {}", webSocketSet.size(), userId);

        //连接成功 推送在线人数给前端
        SendForbidUserMsg sendForbidUserMsg = SpringContextUtil.getBean(SendForbidUserMsg.class);

        if (sendForbidUserMsg != null){
            sendForbidUserMsg.send("websoket connected","1",userId);
        }

//        GroupSending(userId + " 来了");
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void OnClose() {
        webSocketSet.remove(this.userId);
        log.info("[推送禁用用户消息][WebSocket] 退出成功，当前连接人数为：={}", webSocketSet.size());

//        GroupSending(userId + " 走了");
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void OnMessage(String message_str) {

        // 检查是否为心跳消息
//        if (isHeartbeatMessage(message_str)) {
//            // 心跳消息，不执行任何操作
//            return;
//        }

//        log.info("[推送会员在线人数] WebSocket 收到消息：{}", message_str);

        //判断消息是不是json格式
//        if (JsonUtil.isValidJSONObjectOrArray(message_str)) {
//
//        } else if ("connected".equals(message_str)) {
////            log.info("收到前端webSocket心跳: ", message_str);
//        } else {
//            log.error("消息格式不正确: ", message_str);
//        }
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
            log.info("[推送禁用用户消息]webSocket发生错误");
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
    public boolean AppointSending(String userId, String message) {
        try {
            //判断当前连接是否存在 如存在 才进行推送
            if (webSocketSet.get(userId) == null) {
                //该会员已经断开连接了
                log.error("[推送禁用用户信息]webSocket发送指定消息失败: 会员id不存在(已断开连接)");
                return false;
            }
            webSocketSet.get(userId).session.getBasicRemote().sendText(message);
            log.info("[推送禁用用户信息]webSocket发送指定消息成功: userId: {}, message: {}", userId, message);
            return true;
        } catch (Exception e) {
            log.info("[推送禁用用户信息]webSocket发送指定消息失败: userId: {}, message: {}", userId, message);
            e.printStackTrace();
            return false;
        }
    }


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
