package org.uu.manager.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.service.ISysMessageService;
import org.uu.manager.util.SpringContextUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
@Slf4j
@RequiredArgsConstructor
public class SendForbidUserMsg {



    /**
     * 推送消息给前端
     */
    public Boolean send(String msg,String status,String userId) {

        try {

            //获取redis 在线人数

            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("forbidUserMsg", msg);
            jsonMsg.put("status", status);
            jsonMsg.put("userId", userId);
            // 获取未读系统消息
            ISysMessageService sysMessageService = SpringContextUtil.getBean(ISysMessageService.class);
            Integer unReadMessageCount = sysMessageService.unReadMessageCount(userId);
            jsonMsg.put("unReadMessageCount", unReadMessageCount);
            // 获取当前系统时间
            jsonMsg.put("currentDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            ForbidUserWebSocketService forbidUserWebSocketService = SpringContextUtil.getBean(ForbidUserWebSocketService.class);

            if (forbidUserWebSocketService != null) {
                //群发WebSocket消息
                return forbidUserWebSocketService.AppointSending(userId, JSON.toJSONString(jsonMsg, SerializerFeature.WriteMapNullValue));
            } else {
                log.error("webSocket推送禁用用户信息失败, forbidUserWebSocketService为null");
                return false;
            }


        } catch (Exception e) {
            log.error("webSocket推送禁用用户信息失败, e: {}", e);
            return false;
        }
    }
}
