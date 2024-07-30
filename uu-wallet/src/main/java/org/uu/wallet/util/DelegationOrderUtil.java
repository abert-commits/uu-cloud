package org.uu.wallet.util;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.bo.ActiveKycPartnersBO;
import org.uu.wallet.bo.DelegationOrderBO;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.service.IKycPartnersService;
import org.uu.wallet.service.IMemberInfoService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 委托订单工具类
 *
 * @author simon
 * @date 2024/07/12
 */
@Component
@Slf4j
public class DelegationOrderUtil {

    @Autowired
    private DelegationOrderRedisUtil delegationOrderRedisUtil;

    @Autowired
    private IMemberInfoService memberInfoService;

    @Autowired
    private IKycPartnersService kycPartnersService;

    @Autowired
    private RedissonUtil redissonUtil;

    /**
     * kyc断开链接 关闭委托
     *
     * @param memberInfo
     */
    @Transactional(propagation = Propagation.NEVER)//如果调用该方法前还存在事务 就抛异常
    public boolean closeDelegation(MemberInfo memberInfo) {

        try {
            //避免未完成mysql操作 暂停3秒再执行
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //获取当前正在链接的kyc
        ActiveKycPartnersBO activeKycPartnersBO = kycPartnersService.getActiveKycPartnersByMemberId(String.valueOf(memberInfo.getId()));

        if (activeKycPartnersBO == null || activeKycPartnersBO.getUpiPartners() == null || activeKycPartnersBO.getUpiPartners().isEmpty()) {
            //用户没有在连接的kyc 关闭委托
            log.info("kyc断开链接 关闭委托, 当前会员没有可连接的kyc, 会员id: {}", memberInfo.getId());

            DelegationOrderBO delegationOrderBO = new DelegationOrderBO();
            //会员ID
            delegationOrderBO.setMemberId(String.valueOf(memberInfo.getId()));
            //委托时间
            delegationOrderBO.setDelegationTime(LocalDateTime.now());
            //委托金额
            delegationOrderBO.setAmount(memberInfo.getBalance());

            //将委托信息从redis删除
            delegationOrderRedisUtil.removeOrder(delegationOrderBO);

            //更新会员信息
            LambdaUpdateWrapper<MemberInfo> lambdaUpdateWrapperMemberInfo = new LambdaUpdateWrapper<>();
            // 指定更新条件，会员id
            lambdaUpdateWrapperMemberInfo.eq(MemberInfo::getId, memberInfo.getId());
            // 关闭委托订单状态
            lambdaUpdateWrapperMemberInfo.set(MemberInfo::getDelegationStatus, 0);
            // 这里传入的 null 表示不更新实体对象的其他字段
            return memberInfoService.update(null, lambdaUpdateWrapperMemberInfo);
        } else {
            log.info("kyc断开链接 关闭委托, 当前会员还存在可连接的kyc信息, kyc信息: {}, 会员id: {}", activeKycPartnersBO, memberInfo.getId());
        }
        return true;
    }


    /**
     * 禁用会员 关闭委托
     *
     * @param memberInfo
     * @return boolean
     */
    public boolean disableMemberAndCloseOrders(MemberInfo memberInfo) {

        //禁用会员 关闭委托
        log.info("禁用会员 关闭委托, 会员id: {}", memberInfo.getId());

        DelegationOrderBO delegationOrderBO = new DelegationOrderBO();
        //会员ID
        delegationOrderBO.setMemberId(String.valueOf(memberInfo.getId()));
        //委托时间
        delegationOrderBO.setDelegationTime(LocalDateTime.now());
        //委托金额
        delegationOrderBO.setAmount(memberInfo.getBalance());

        //将委托信息从redis删除
        delegationOrderRedisUtil.removeOrder(delegationOrderBO);

        //更新会员信息
        LambdaUpdateWrapper<MemberInfo> lambdaUpdateWrapperMemberInfo = new LambdaUpdateWrapper<>();
        // 指定更新条件，会员id
        lambdaUpdateWrapperMemberInfo.eq(MemberInfo::getId, memberInfo.getId());
        // 关闭委托订单状态
        lambdaUpdateWrapperMemberInfo.set(MemberInfo::getDelegationStatus, 0);
        // 这里传入的 null 表示不更新实体对象的其他字段
        return memberInfoService.update(null, lambdaUpdateWrapperMemberInfo);
    }


    /**
     * 会员余额变动 委托状态在开启中 重新委托
     *
     * @param memberId       会员id
     * @param currentBalance 会员当前余额
     */
    public void redelegate(Long memberId, BigDecimal currentBalance) {

        //分布式锁key ar-wallet-delegateSell
        String key = "uu-wallet-delegateSell";//与委托用同一把锁
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                // 将最新的余额进行委托
                DelegationOrderBO delegationOrderBO = new DelegationOrderBO();
                delegationOrderBO.setMemberId(String.valueOf(memberId));
                delegationOrderBO.setDelegationTime(LocalDateTime.now());
                delegationOrderBO.setAmount(currentBalance);

                //将最新的余额进行委托
                delegationOrderRedisUtil.addOrder(delegationOrderBO);
                log.info("重新委托处理成功, 会员id: {}, 当前会员余额: {}", memberId, currentBalance);
            }
        } catch (Exception e) {
            //手动回滚
            log.error("重新委托处理失败: 会员id: {}, e: {}", memberId, e.getMessage());
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
