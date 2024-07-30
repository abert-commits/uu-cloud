package org.uu.wallet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.message.ExecuteCommissionAndDividendsMessage;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.CommissionFlagEnum;
import org.uu.wallet.entity.CommissionDividends;
import org.uu.wallet.entity.MemberAccountChange;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.service.CommissionDividendsService;
import org.uu.wallet.service.ExecuteCommissionOrDividendsService;
import org.uu.wallet.service.IMemberAccountChangeService;
import org.uu.wallet.util.OrderNumberGeneratorUtil;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecuteCommissionOrDividendsServiceImpl implements ExecuteCommissionOrDividendsService {
    private final IMemberAccountChangeService memberAccountChangeService;

    private final RedissonUtil redissonUtil;

    private final MemberInfoMapper memberInfoMapper;

    private final CommissionDividendsService commissionDividendsService;

    private final OrderNumberGeneratorUtil orderNumberGenerator;

    @Override
    public Boolean executeCommissionOrDividends(ExecuteCommissionAndDividendsMessage message) {
        // 幂等消费
        CommissionDividends commissionDividends = this.commissionDividendsService.lambdaQuery()
                .eq(CommissionDividends::getFromMember, message.getFromMemberId())
                .eq(CommissionDividends::getToMember, message.getToMemberId())
                .eq(CommissionDividends::getOrderNo, message.getOrderNo())
                .eq(CommissionDividends::getRecordType, message.getType())
                .one();
        if (Objects.nonNull(commissionDividends)) {
            log.info("[Rabbit消费执行分红和返佣队列消息] - 消费失败 该消息已经被消费 message::{}", message);
            return true;
        }

        //分布式锁key six-nine-pay-executeCommissionOrDividends-orderNo-订单号
        String key = "six-nine-pay-executeCommissionOrDividends-orderNo-" + message.getOrderNo();
        RLock lock = redissonUtil.getLock(key);
        boolean lockFlag = false;
        try {
            lockFlag = lock.tryLock(10, TimeUnit.SECONDS);
            if (lockFlag) {
                // 锁行
                MemberInfo memberInfo = this.memberInfoMapper.selectMemberInfoForUpdate(message.getToMemberId());

                String changeType;
                String changeOrderNo;
                switch (message.getType()) {
                    case 1:
                        changeType = MemberAccountChangeEnum.BUY_COMMISSION.getCode();
                        changeOrderNo = orderNumberGenerator.generateOrderNo(MemberAccountChangeEnum.BUY_COMMISSION.getPrefix());
                        memberInfo.setTotalBuyCommissionAmount(memberInfo.getTotalBuyCommissionAmount().add(message.getAmount()));
                        break;
                    case 2:
                        changeType = MemberAccountChangeEnum.SELL_COMMISSION.getCode();
                        changeOrderNo = orderNumberGenerator.generateOrderNo(MemberAccountChangeEnum.SELL_COMMISSION.getPrefix());
                        memberInfo.setTotalSellCommissionAmount(memberInfo.getTotalSellCommissionAmount().add(message.getAmount()));
                        break;
                    case 3:
                        changeType = MemberAccountChangeEnum.PLATFORM_DIVIDENDS.getCode();
                        changeOrderNo = orderNumberGenerator.generateOrderNo(MemberAccountChangeEnum.PLATFORM_DIVIDENDS.getPrefix());
                        memberInfo.setPlatformDividends(memberInfo.getPlatformDividends().add(message.getAmount()));
                        memberInfo.setDividendsLevel(memberInfo.getDividendsLevel() + 1);
                        break;
                    default:
                        log.info("[Rabbit消费执行分红和返佣队列消息] - 消费失败 未知的类型 摒弃该次消息");
                        return true;
                }
                // 添加一笔账变
                this.memberAccountChangeService.save(
                        MemberAccountChange.builder()
                                .mid(memberInfo.getId().toString())
                                .memberAccount(memberInfo.getMobileNumber())
                                .currentcy("INR")
                                .changeType(changeType)
                                .changeMode("add")
                                .orderNo(changeOrderNo)
                                .beforeChange(memberInfo.getBalance())
                                .amountChange(message.getAmount())
                                .afterChange(memberInfo.getBalance().add(message.getAmount()))
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build()
                );

                // 调整总余额和对应余额
                memberInfo.setBalance(memberInfo.getBalance().add(message.getAmount()))
                        .setUpdateTime(LocalDateTime.now());
                this.memberInfoMapper.updateById(memberInfo);

                // 将该订单状态置为已返佣
                if (!MemberAccountChangeEnum.PLATFORM_DIVIDENDS.getCode().equals(changeType)) {
                    this.memberAccountChangeService.updateCommissionFlagByOrderNo(
                            message.getFromMemberId(),
                            Integer.valueOf(
                                    MemberAccountChangeEnum.BUY_COMMISSION.getCode().equals(changeType) ?
                                            MemberAccountChangeEnum.RECHARGE.getCode()
                                            :
                                            MemberAccountChangeEnum.WITHDRAW.getCode()
                            ),
                            message.getOrderNo(),
                            CommissionFlagEnum.COMMISSION_YES.getCommissionFlag()
                    );
                }

                // 添加一笔返佣或分红的记录
                return this.commissionDividendsService.save(
                        CommissionDividends.builder()
                                .fromMember(message.getFromMemberId())
                                .toMember(message.getToMemberId())
                                .recordType(
                                        MemberAccountChangeEnum.BUY_COMMISSION.getCode().equals(changeType) ?
                                                1
                                                :
                                                (
                                                        MemberAccountChangeEnum.PLATFORM_DIVIDENDS.getCode().equals(changeType) ?
                                                                3 : 2
                                                )
                                )
                                .accountChangeOrderNo(changeOrderNo)
                                .orderNo(message.getOrderNo())
                                .rewardAmount(message.getAmount())
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build()
                );
            }
            log.info("[Rabbit消费分红和返佣队列消息] - 执行返佣或分红失败, 未获取到分布式锁 key::{}", key);
            return false;
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.info("[Rabbit消费分红和返佣队列消息] - 执行返佣或分红失败, commissionMessage: {}, ", message);
            return false;
        } finally {
            //释放锁
            if (lockFlag && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
