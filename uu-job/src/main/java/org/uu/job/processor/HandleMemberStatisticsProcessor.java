package org.uu.job.processor;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.job.Enum.MemberTypeEnum;
import org.uu.manager.entity.BiMemberStatistics;
import org.uu.manager.mapper.BiMemberStatisticsMapper;
import org.uu.wallet.entity.MemberAccountChange;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.entity.UsdtBuyOrder;
import org.uu.wallet.mapper.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * 清洗会员统计数据
 *
 * @author Admin
 */
@Component("handleMemberStatisticsProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandleMemberStatisticsProcessor implements BasicProcessor {

    private final BiMemberStatisticsMapper biMemberStatisticsMapper;
    private final MemberInfoMapper memberInfoMapper;
    private final MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;
    private final MerchantCollectOrdersMapper merchantCollectOrdersMapper;

    private final RedisUtils redisUtils;

    /**
     * 批次大小
     */
    private static final int BATCH_SIZE = 1000;



    /**
     * 跨天时长(毫秒)
     */
    private static final int OVER_TIME = 30 * 60 * 1000;


    /**
     * dateType：today，interval
     * 1.当天执行重跑: {"dateType":"today","startTime":"2023-11-15 00:00:00", "endTime":"2023-11-15 23:59:59"}
     * 2.跨天数据处理 ：凌晨半个小时处理前天数据
     * 3.重跑一段时间数据：连续天数
     * 4.Job开关,停服开关控制,服务重启以免数据丢失
     * 5.自动跑今天数据
     *
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {

        try {
            // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
            OmsLogger omsLogger = context.getOmsLogger();
            omsLogger.info("清洗会员统计数据参数->{}", context.getJobParams());
            log.info("清洗会员统计数据参数->{}", context.getJobParams());
            String jobSwitch = (String) redisUtils.get(RedisConstants.JOB_SWITCH);
            if (!StringUtils.isEmpty(jobSwitch) && GlobalConstants.STATUS_OFF.equals(Integer.parseInt(jobSwitch))) {
                log.info("清洗会员统计数据Job未执行,Job开关已关闭.");
                return new ProcessResult(true, "Job switch turned off");
            }
            String dateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), GlobalConstants.DATE_FORMAT_DAY);
            biMemberStatisticsMapper.deleteByDateTime(dateStr);
            handleMemberStatistics(dateStr);
        } catch (Exception e) {
            log.error("HandlePaymentOrderProcessor.process" + e.getMessage());
        }

        return new ProcessResult(true, "return success");
    }

    private void handleMemberStatistics(String dateStr) throws ExecutionException, InterruptedException {
        updateBiMemberStatistics(dateStr);
    }

    @NotNull
    private void updateBiMemberStatistics(String dateStr) throws ExecutionException, InterruptedException {

        // 查询近7日活跃人数
        String sevenStartTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-8), GlobalConstants.DATE_FORMAT_DAY);
        String sevenEndTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
        log.info("统计近7日数据,开始时间->{},结束时间->{}", sevenStartTime, sevenEndTime);
        CompletableFuture<Long> sevenFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectActiveNum(sevenStartTime + " 00:00:00", sevenEndTime + " 23:59:59");
        });

        String yesterdayStartTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
        log.info("统计近昨日数据,开始时间->{},结束时间->{}", yesterdayStartTime, yesterdayStartTime);
        CompletableFuture<Long> yesterdayFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectActiveNum(yesterdayStartTime + " 00:00:00", yesterdayStartTime + " 23:59:59");
        });

        String thirtyStartTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-31), GlobalConstants.DATE_FORMAT_DAY);
        String thirtydayEndTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
        log.info("统计近30日数据,开始时间->{},结束时间->{}", thirtyStartTime, thirtydayEndTime);
        CompletableFuture<Long> thirtyFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectActiveNum(thirtyStartTime  + " 00:00:00", thirtydayEndTime + " 23:59:59");
        });

        // 查询会员人数
        CompletableFuture<List<MemberInfo>> memberInfoFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectSumNumInfo();
        });

        // 查询实名认证人数
        CompletableFuture<Long> realNameNumFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectRealNameNum();
        });

        // 查询参与买入人数
        CompletableFuture<Long> buyFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectBuyNum();
        });

        // 查询黑名单人数
        CompletableFuture<Long> blackMemberNum = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectblackMemberNum();
        });

        // 查询参与卖出人数
        CompletableFuture<Long> sellFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectSellNum();
        });

        // 查询参与买入卖出人数
        CompletableFuture<Long> buyAndSellFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectBuyAndSellNum();
        });

        // 查询参与usdt买入人数
        CompletableFuture<Long> buyUsdtFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectBuyUsdtNum();
        });

        // 查询参与充值人数
        CompletableFuture<Long> rechargeFuture = CompletableFuture.supplyAsync(() -> {
            return merchantCollectOrdersMapper.selectRechargeNum();
        });

        // 查询参与提现人数
        CompletableFuture<Long> withdrawFuture = CompletableFuture.supplyAsync(() -> {
            return merchantPaymentOrdersMapper.selectWithdrawFuture();
        });

        // 查询买入禁用人数
        CompletableFuture<Long> buyDisableFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectBuyDisableFuture();
        });

        // 查询卖出禁用人数
        CompletableFuture<Long> sellDisableFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectSellDisableFuture();
        });
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(sevenFuture, yesterdayFuture, thirtyFuture,
                memberInfoFuture,realNameNumFuture,buyFuture,sellFuture,buyAndSellFuture,buyUsdtFuture
        ,rechargeFuture,withdrawFuture,buyDisableFuture, sellDisableFuture,blackMemberNum);
        allFutures.get();
        List<MemberInfo> memberInfoList = memberInfoFuture.get();
        BiMemberStatistics biMemberStatistics = new BiMemberStatistics();
        // 商户会员人数
        Optional<MemberInfo> memberInfoOptional = memberInfoList.stream().filter(m ->!StringUtils.isEmpty(m.getMemberType()) && m.getMemberType().equals(MemberTypeEnum.MERCHANT_MEMBER.getCode())).findFirst();
        if(memberInfoOptional.isPresent()){
            biMemberStatistics.setMerchantMemberNum(memberInfoOptional.get().getBalance().longValue());
        }

        // 钱包会员人数
        Optional<MemberInfo> walletInfoOptional = memberInfoList.stream().filter(m ->!StringUtils.isEmpty(m.getMemberType()) && m.getMemberType().equals(MemberTypeEnum.WALLET_MEMBER.getCode())).findFirst();
        if(walletInfoOptional.isPresent()){
            biMemberStatistics.setWalletMemberNum(walletInfoOptional.get().getBalance().longValue());
        }
        // 总会员人数
        BigDecimal totalNum =memberInfoList.stream().map(MemberInfo::getBalance).reduce(BigDecimal.ZERO,BigDecimal::add);
        if(ObjectUtils.isNotEmpty(totalNum)){
            biMemberStatistics.setMemberTotalNum(totalNum.longValue());
        }
        biMemberStatistics.setYesterdayActiveNum(yesterdayFuture.get());
        biMemberStatistics.setSevenActiveNum(sevenFuture.get());
        biMemberStatistics.setThirtyActiveNum(thirtyFuture.get());
        biMemberStatistics.setRealNameNum(realNameNumFuture.get());
        biMemberStatistics.setBuyNum(buyFuture.get());
        biMemberStatistics.setSellNum(sellFuture.get());
        biMemberStatistics.setBuyAndSellNum(buyAndSellFuture.get());
        biMemberStatistics.setBuyUsdtActiveNum(buyUsdtFuture.get());
        biMemberStatistics.setRechargeActiveNum(rechargeFuture.get());
        biMemberStatistics.setWithdrawNum(withdrawFuture.get());
        biMemberStatistics.setDisableBuyNum(buyDisableFuture.get());
        biMemberStatistics.setDisableSellNum(sellDisableFuture.get());
        biMemberStatistics.setBlackMemberNum(blackMemberNum.get());
        biMemberStatistics.setDateTime(dateStr);
        biMemberStatistics.setCreateTime(LocalDateTime.now());
        biMemberStatistics.setUpdateTime(LocalDateTime.now());
        biMemberStatisticsMapper.insert(biMemberStatistics);
    }
}
