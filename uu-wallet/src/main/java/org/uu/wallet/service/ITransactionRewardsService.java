package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.entity.TransactionRewards;

import java.math.BigDecimal;

/**
 * <p>
 * 交易奖励表 服务类
 * </p>
 *
 * @author
 * @since 2024-03-19
 */
public interface ITransactionRewardsService extends IService<TransactionRewards> {

    /**
     * 检查会员是否达到当日卖出奖励的金额或次数限制，返回应该发放的奖励金额
     *
     * @param memberId
     * @return 大于0 表示未达到限制，可以继续发放奖励；否则 表示已达到限制，不再发放奖励
     */
    BigDecimal canReward(String memberId, BigDecimal bonus);

}
