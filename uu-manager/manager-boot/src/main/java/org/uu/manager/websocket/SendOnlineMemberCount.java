package org.uu.manager.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.pay.dto.MerchantLastOrderWarnDTO;
import org.uu.common.pay.dto.SmsBalanceWarnDTO;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.api.MerchantInfoClient;
import org.uu.manager.api.SmsThirdApiFeignClient;
import org.uu.manager.enums.WarnMsgTypeEnum;
import org.uu.manager.util.SpringContextUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 推送推荐金额列表至前端(钱包用户)
 *
 * @author Simon
 * @date 2023/11/08
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SendOnlineMemberCount {

    private final RedisUtils redisUtils;
    private final SmsThirdApiFeignClient smsThirdApiFeignClient;
    private final MerchantInfoClient merchantInfoClient;


    /**
     * 推送消息给前端
     */
    public Boolean send() {

        try {
            //获取redis 在线人数
            Long onLineCount = CommonUtils.getOnlineCount(redisUtils);
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("onlineMemberCount", String.valueOf(onLineCount));

//            RestResult<SmsBalanceWarnDTO> checkResponse = smsThirdApiFeignClient.checkBalance();
//            List<JSONObject> warnItemList = new ArrayList<>();
//            SmsBalanceWarnDTO smsCheckResult = checkResponse.getData();
//            if (smsCheckResult != null && smsCheckResult.getIsWarn()) {
//                JSONObject warnItem = (JSONObject) JSONObject.toJSON(smsCheckResult);
//                warnItem.put("type", WarnMsgTypeEnum.SMS_BALANCE_INSUFFICIENT.getCode());
//                warnItemList.add(warnItem);
//            }
//            // 查询代收代付最后一笔订单发生时间
//            List<MerchantLastOrderWarnDTO> latestOrderTime = merchantInfoClient.getLatestOrderTime();
//            if (latestOrderTime != null && !latestOrderTime.isEmpty()) {
//                List<JSONObject> warnOrderList = new ArrayList<>();
//                for (MerchantLastOrderWarnDTO merchantLastOrderWarnDTO : latestOrderTime) {
//                    JSONObject warnItem = (JSONObject) JSONObject.toJSON(merchantLastOrderWarnDTO);
//                    warnOrderList.add(warnItem);
//                }
//                jsonMsg.put("warnOrderItems", warnOrderList);
//            }
//
//            jsonMsg.put("warnItems", warnItemList);


            // 获取当前系统时间
            jsonMsg.put("currentDateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            OnlineMemberCountWebSocketService onlineMemberCountWebSocketService = SpringContextUtil.getBean(OnlineMemberCountWebSocketService.class);

            if (onlineMemberCountWebSocketService != null) {
                //群发WebSocket消息
                return onlineMemberCountWebSocketService.AppointSending(JSON.toJSONString(jsonMsg, SerializerFeature.WriteMapNullValue));
            } else {
                log.error("webSocket推送在线人数给前端失败, onlineMemberCountWebSocketService为null");
                return false;
            }


        } catch (Exception e) {
            log.error("webSocket推送在线人数给前端失败, e: {}", e);
            return false;
        }
    }
}
