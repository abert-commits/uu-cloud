//package org.uu.wallet.webSocket;
//
//import lombok.extern.slf4j.Slf4j;
//import org.uu.wallet.util.RedisBean;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.websocket.*;
//import javax.websocket.server.PathParam;
//import javax.websocket.server.ServerEndpoint;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.TimeUnit;
//
///**
// * @ServerEndpoint 注解的作用
// * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
// * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
// */
//
//@Slf4j
//@Component
//@ServerEndpoint("/websocket/getRecommendAmount/{userId}")
//public class MerchantGetAmountWebSocketController {
//
//    /**
//     * 用于存所有的连接服务的客户端，这个对象存储是安全的
//     * 这里的v (用来存放每个客户端对应的WebSocket对象)
//     */
//    private static ConcurrentHashMap<String, MerchantGetAmountWebSocketController> webSocketSet = new ConcurrentHashMap<>();
//    /**
//     * 与某个客户端的连接对话，需要通过它来给客户端发送消息
//     */
//    private Session session;
//    /**
//     * 标识当前连接客户端的用户名
//     */
//    private String userId;
//
////    @Autowired
////    private RedisTemplate redisTemplate;
//
//    /**
//     * 连接建立成功调用的方法
//     * session为与某个客户端的连接会话，需要通过它来给客户端发送数据
//     */
//    @OnOpen
//    public void OnOpen(Session session, @PathParam(value = "userId") String userId) {
//        this.session = session;
//        this.userId = userId;
//        // userId是用来表示唯一客户端，如果需要指定发送，需要指定发送通过userId来区分
//        webSocketSet.put(userId, this);
//        log.info("webSocket连接成功，当前连接人数为：{}, userId: {}", webSocketSet.size(), userId);
//
////        GroupSending(userId + " 来了");
//    }
//
//    /**
//     * 连接关闭调用的方法
//     */
//    @OnClose
//    public void OnClose() {
//        webSocketSet.remove(this.userId);
//        log.info("[WebSocket] 退出成功，当前连接人数为：={}", webSocketSet.size());
//
////        GroupSending(userId + " 走了");
//
//        //删除充值金额和webSocketID
//        RedisTemplate redisTemplate = RedisBean.redis;
//        redisTemplate.boundHashOps("userIdAndCollectionAmount").delete(userId);
//    }
//
//    /**
//     * 收到客户端消息后调用的方法
//     */
//    @OnMessage
//    public void OnMessage(String message_str) {
//
//
//        log.info("[WebSocket] 收到消息：{}", message_str);
//        //判断是否需要指定发送，具体规则自定义
//        //message_str的格式 TOUSER:user2;message:aaaaaaaaaaaaaaaaaa;
//        if (message_str.indexOf("TOUSER") == 0) {
//            //取出 userId和message的值
//            String[] split = message_str.split(";");
//            String[] split1 = split[0].split(":");
//            String[] split2 = split[1].split(":");
//            String userId = split1[1];
//            String message = split2[1];
//
//            //记录充值金额和webSocketID (记录到Redis)
//            RedisTemplate redisTemplate = RedisBean.redis;
//            BoundHashOperations userIdAndCollectionAmount = redisTemplate.boundHashOps("userIdAndCollectionAmount");
//
//            userIdAndCollectionAmount.put(userId, message);
//            //默认过期时间为30分钟
//            userIdAndCollectionAmount.expire(30, TimeUnit.MINUTES);
//
//            log.info("记录充值金额和用户id到Redis: userId: {}, message: {}", userId, message);
//
//            //指定发送
////            AppointSending(userId, message);
//        } else {
//            //群发
//            log.info("消息格式不正确: ", message_str);
////            GroupSending(message_str);
//        }
//    }
//
//    /**
//     * 发生错误时调用
//     *
//     * @param session
//     * @param error
//     */
//    @OnError
//    public void onError(Session session, Throwable error) {
//        log.info("发生错误");
//        error.printStackTrace();
//    }
//
//    /**
//     * 群发
//     *
//     * @param message
//     */
//    public void GroupSending(String message) {
//        for (String userId : webSocketSet.keySet()) {
//            try {
//                webSocketSet.get(userId).session.getBasicRemote().sendText(message);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 指定发送
//     *
//     * @param userId
//     * @param message
//     */
//    public boolean AppointSending(String userId, String message) {
//        try {
//
//            //判断当前连接是否存在 如存在 才进行推送
//            if (webSocketSet.get(userId) == null) {
//                return false;
//            }
//            webSocketSet.get(userId).session.getBasicRemote().sendText(message);
//            log.info("webSocket发送指定消息成功: userId: {}, message: {}", userId, message);
//            return true;
//        } catch (Exception e) {
//            log.info("webSocket发送指定消息失败: userId: {}, message: {}", userId, message);
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
