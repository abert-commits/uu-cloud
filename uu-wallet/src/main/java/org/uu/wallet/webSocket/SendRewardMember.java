package org.uu.wallet.webSocket;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.service.ITaskCollectionRecordService;
import org.uu.wallet.util.SpringContextUtil;
import org.uu.wallet.vo.PrizeWinnersVo;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 推送获奖会员至前端(钱包用户)
 *
 * @author Simon
 * @date 2023/11/08
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SendRewardMember {

    private final ITaskCollectionRecordService taskCollectionRecordService;


    /**
     * 推送消息给前端
     */
    public Boolean send() {

        try {
            // 查询领奖会员列表
            List<PrizeWinnersVo> prizeWinnersList = taskCollectionRecordService.getPrizeWinnersList();
            if (Collections.isEmpty(prizeWinnersList)) {
                log.info("webSocket推送获奖会员给前端: 未查询到数据, 无需推送");
                return Boolean.TRUE;
            }

            RewardMemberWebSocketService rewardMemberWebSocketService = SpringContextUtil.getBean(RewardMemberWebSocketService.class);
            if (rewardMemberWebSocketService != null) {
                //群发WebSocket消息
                return rewardMemberWebSocketService.AppointSending(JSON.toJSONString(prizeWinnersList));
            } else {
                log.error("webSocket推送获奖会员给前端失败, onlineMemberCountWebSocketService为null");
                return false;
            }

        } catch (Exception e) {
            log.error("webSocket推送获奖会员给前端失败:", e);
            return false;
        }
    }
}
