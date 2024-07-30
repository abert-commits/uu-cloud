package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.TradeConfigScheme;
import org.uu.wallet.entity.TransactionRewards;
import org.uu.wallet.mapper.TransactionRewardsMapper;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.service.ITransactionRewardsService;
import org.uu.wallet.util.TradeConfigHelperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <p>
 * 交易奖励表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-03-19
 */
@Service
@Slf4j
public class TransactionRewardsServiceImpl extends ServiceImpl<TransactionRewardsMapper, TransactionRewards> implements ITransactionRewardsService {

    @Autowired
    private TradeConfigHelperUtil tradeConfigHelperUtil; // 适当替换为你的实际Mapper
    @Autowired
    private IMemberInfoService memberInfoService;

    /**
     * 检查会员是否达到当日卖出奖励的金额或次数限制，返回应该发放的奖励金额
     *
     * @param memberId
     * @return 大于0 表示未达到限制，可以继续发放奖励；否则 表示已达到限制，不再发放奖励
     */
    @Override
    public BigDecimal canReward(String memberId, BigDecimal bonus) {
        MemberInfo memberInfo = memberInfoService.getMemberInfoById(memberId);;
        // 根据会员标签获取对应配置信息
        TradeConfigScheme schemeConfigByMemberTag = tradeConfigHelperUtil.getSchemeConfigByMemberTag(memberInfo);

        // 获取卖出奖励每日限制金额
        BigDecimal dailyAmountLimit = schemeConfigByMemberTag.getSchemeSalesBonusAmountLimit();

        // 获取卖出奖励每日限制次数
        Integer dailyCountLimit = schemeConfigByMemberTag.getSchemeSalesBonusNumLimit();

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        BigDecimal rewardAmount = BigDecimal.ZERO;;

        // 如果设置了金额限制且金额限制大于或等于1，计算当日总金额
        if (dailyAmountLimit != null && dailyAmountLimit.compareTo(BigDecimal.ONE) >= 0) {
            BigDecimal totalAmount = lambdaQuery()
                    .eq(TransactionRewards::getMemberId, memberId)
                    .eq(TransactionRewards::getType, 2) // 2 代表卖出奖励
                    .between(TransactionRewards::getCreateTime, startOfDay, endOfDay)
                    .select(TransactionRewards::getRewardAmount) // 仅选择奖励金额字段进行查询
                    .list() // 直接获取结果列表
                    .stream()
                    .map(TransactionRewards::getRewardAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add); // 计算总金额
            log.info("计算会员卖出奖励, 会员ID:{}, 当日限额:{}, 实际已发放金额:{}, 规则中的金额:{}", memberId, dailyAmountLimit, totalAmount, bonus);
            // 当日剩余奖励
            rewardAmount = dailyAmountLimit.subtract(totalAmount);
            rewardAmount = rewardAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : rewardAmount;
            // 当日剩余奖励>规则中要发的奖励, 直接发放规则中的奖励
            if (rewardAmount.compareTo(bonus) > 0) {
                rewardAmount = bonus;
            }
        } else {
            rewardAmount = bonus;
        }

        // 如果设置了次数限制且次数限制大于或等于1，计算当日总次数
        if (dailyCountLimit != null && dailyCountLimit >= 1) {
            long totalCount = lambdaQuery()
                    .eq(TransactionRewards::getMemberId, memberId)
                    .eq(TransactionRewards::getType, 2)
                    .between(TransactionRewards::getCreateTime, startOfDay, endOfDay)
                    .count(); // 直接获取满足条件的记录数
            log.info("计算会员卖出奖励, 会员ID:{}, 当日限次:{}, 实际已发放次数:{}", memberId, dailyCountLimit, totalCount);
            // 如果次数不满足条件, 奖励金额为0
            if(totalCount >= dailyCountLimit){
                rewardAmount = BigDecimal.ZERO;
            }
        }

        return rewardAmount;
    }
}
