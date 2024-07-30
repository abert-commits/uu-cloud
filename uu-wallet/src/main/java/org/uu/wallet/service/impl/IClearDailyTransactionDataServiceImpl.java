package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.service.IClearDailyTransactionDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class IClearDailyTransactionDataServiceImpl implements IClearDailyTransactionDataService {

    @Autowired
    private MemberInfoMapper memberInfoMapper;


    /**
     * 处理清空每日交易数据
     *
     * @return {@link Boolean}
     */
    @Transactional
    @Override
    public Boolean clearDailyTradeData() {
        try {
            LambdaUpdateWrapper<MemberInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper
                    .set(MemberInfo::getTodayBuySuccessCount, 0)
                    .set(MemberInfo::getTodayBuySuccessAmount, BigDecimal.ZERO)
                    .set(MemberInfo::getTodaySellSuccessCount, 0)
                    .set(MemberInfo::getTodaySellSuccessAmount, BigDecimal.ZERO);

            memberInfoMapper.update(null, updateWrapper);

            log.info("处理清空每日交易数据 处理成功, 当前时间: {}", LocalDateTime.now());
            return true;
        } catch (Exception e) {
            log.error("处理清空每日交易数据 处理失败, 当前时间: {}", LocalDateTime.now());
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 抛出异常 触发MQ重试机制
            throw e; // Re-throw the exception to ensure the transaction rolls back
        }
    }
}
