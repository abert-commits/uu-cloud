package org.uu.wallet.webSocket;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.wallet.req.AmountListReq;
import org.uu.wallet.req.BuyListReq;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.vo.BuyListVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 推送推荐金额列表至前端(钱包用户)
 *
 * @author Simon
 * @date 2023/11/08
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MemberSendAmountList {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MemberAmountListWebSocketService memberAmountListWebSocketService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 推送消息给前端
     */
    public void send() {


        try {
            //从redis里面获取推荐金额数据(userId、最小金额, 最大金额)
            Map userId_minimumAmount_maximumAmount = redisTemplate.boundHashOps(RedisKeys.AMOUNT_LIST_REQ).entries();


            log.info("推送前端金额列表: 从redis里面获取用户id和查询条件: 待推送用户:  {}", JSON.toJSONString(userId_minimumAmount_maximumAmount, SerializerFeature.WriteMapNullValue));

            //遍历redis里面的数据(userId:充值金额)
            for (Object userId : userId_minimumAmount_maximumAmount.keySet()) {

                //将Redis的数据 转为 实体对象
                AmountListReq amountListReq = JSON.parseObject(String.valueOf(userId_minimumAmount_maximumAmount.get(userId)), AmountListReq.class);

                BuyListReq buyListReq = new BuyListReq();
                BeanUtil.copyProperties(amountListReq, buyListReq);
                buyListReq.setMemberId(amountListReq.getUserId());

                //从Redis里面获取买入金额列表
                PageReturn<BuyListVo> buyList = redisUtil.getBuyList(buyListReq);

                JSONObject jsonMsg = new JSONObject();

                jsonMsg.put("userId", userId);
                jsonMsg.put("buyList", buyList.getList());

                //根据webSocketID发送给前端推荐金额列表
                boolean send = memberAmountListWebSocketService.AppointSending(JSON.toJSONString(jsonMsg, SerializerFeature.WriteMapNullValue));
                if (send) {
                    //log.info("webSocket推送推荐金额给前端成功: 用户id: {}, data: {}", userId, JSON.toJSONString(jsonMsg, SerializerFeature.WriteMapNullValue));
                    log.info("webSocket推送推荐金额给前端成功: 用户id: {}", userId);
                } else {
                    log.error("webSocket推送推荐金额给前端失败: 用户id: {}, data: {}", userId, JSON.toJSONString(jsonMsg, SerializerFeature.WriteMapNullValue));
                }
            }
        } catch (Exception e) {
            log.error("webSocket推送推荐金额给前端失败, e: {}", e);
        }

    }
}
