package org.uu.job.processor;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.entity.BiMerchantDaily;
import org.uu.manager.entity.BiMerchantStatistics;
import org.uu.manager.mapper.BiMerchantDailyMapper;
import org.uu.manager.mapper.BiMerchantStatisticsMapper;
import org.uu.manager.mapper.BiMerchantStatisticsMapper;
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
 * 清洗商户统计数据
 *
 * @author Admin
 */
@Component("handleMerchantStatisticsProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandleMerchantStatisticsProcessor implements BasicProcessor {

    private final BiMerchantStatisticsMapper biMerchantStatisticsMapper;
    private final MemberInfoMapper memberInfoMapper;
    private final MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;
    private final MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    private final MerchantInfoMapper merchantInfoMapper;

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
            omsLogger.info("清洗商户统计数据参数->{}", context.getJobParams());
            log.info("清洗商户统计数据参数->{}", context.getJobParams());
            String jobSwitch = (String) redisUtils.get(RedisConstants.JOB_SWITCH);
            if (!StringUtils.isEmpty(jobSwitch) && GlobalConstants.STATUS_OFF.equals(Integer.parseInt(jobSwitch))) {
                log.info("清洗商户统计数据Job未执行,Job开关已关闭.");
                return new ProcessResult(true, "Job switch turned off");
            }
            String dateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), GlobalConstants.DATE_FORMAT_DAY);
            // dateType为空代表自动
            biMerchantStatisticsMapper.deleteByDateTime(dateStr);
            handleMerchantStatistics(dateStr);
        } catch (Exception e) {
            log.error("HandlePaymentOrderProcessor.process" + e.getMessage());
        }

        return new ProcessResult(true, "return success");
    }

    private void handleMerchantStatistics(String dateStr) throws ExecutionException, InterruptedException {
        updateBiMerchantStatistics(dateStr);
    }

    private void updateBiMerchantStatistics(@NotNull String dateStr) throws ExecutionException, InterruptedException {

        String yesterdayStartTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
        log.info("统计近昨日数据,开始时间->{},结束时间->{}", yesterdayStartTime, yesterdayStartTime);
        CompletableFuture<List<MemberInfo>> yesterdayFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMerchantActiveNum(yesterdayStartTime + " 00:00:00", yesterdayStartTime + " 23:59:59");
        });

        String sevenStartTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-8), GlobalConstants.DATE_FORMAT_DAY);
        String sevenEndTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
        log.info("统计近7日数据,开始时间->{},结束时间->{}", sevenStartTime, sevenEndTime);
        CompletableFuture<List<MemberInfo>> sevenFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMerchantActiveNum(sevenStartTime + " 00:00:00", sevenEndTime + " 23:59:59");
        });

        String thirtyStartTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-31), GlobalConstants.DATE_FORMAT_DAY);
        String thirtydayEndTime = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
        log.info("统计近30日数据,开始时间->{},结束时间->{}", thirtyStartTime, thirtydayEndTime);
        CompletableFuture<List<MemberInfo>> thirtyFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMerchantActiveNum(thirtyStartTime + " 00:00:00", thirtydayEndTime + " 23:59:59");
        });

        // 查询会员信息
        CompletableFuture<List<MemberInfo>> memberInfoFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMemberInfoInfo();
        });

        // 查询会员信息
        CompletableFuture<List<MerchantInfo>> merchantInfoFuture = CompletableFuture.supplyAsync(() -> {
            return merchantInfoMapper.selectList(null);
        });

        // 查询实名认证人数
        CompletableFuture<List<MemberInfo>> realNameNumFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMerchantRealNameNum();
        });

        // 查询参与买入人数
        CompletableFuture<List<MemberInfo>> merchantBuyNumFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMerchantBuyNum();
        });

        // 查询参与卖出人数
        CompletableFuture<List<MemberInfo>> merchantSellFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMerchantSellNum();
        });

        // 查询参与充值人数
        CompletableFuture<List<MemberInfo>> rechargeFuture = CompletableFuture.supplyAsync(() -> {
            return merchantCollectOrdersMapper.selectMerchantRechargeNum();
        });

        // 查询参与提现人数
        CompletableFuture<List<MemberInfo>> merchantWithdrawNumFuture = CompletableFuture.supplyAsync(() -> {
            return merchantPaymentOrdersMapper.selectMerchantWithdrawNum();
        });

        // 查询代收手续费
        CompletableFuture<List<MemberInfo>> payFeeFuture = CompletableFuture.supplyAsync(() -> {
            return merchantCollectOrdersMapper.selectCostByDate(dateStr);
        });

        // 查询代付手续费
        CompletableFuture<List<MemberInfo>> withdrawFeeFuture = CompletableFuture.supplyAsync(() -> {
            return merchantPaymentOrdersMapper.selectCostByDate(dateStr);
        });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(yesterdayFuture, merchantInfoFuture, sevenFuture, thirtyFuture, realNameNumFuture, merchantBuyNumFuture
                , merchantSellFuture, rechargeFuture, merchantWithdrawNumFuture, payFeeFuture, memberInfoFuture, withdrawFeeFuture);
        allFutures.get();


        for (MerchantInfo item : merchantInfoFuture.get()) {


            BiMerchantStatistics biMerchantStatistics = new BiMerchantStatistics();
            biMerchantStatistics.setMerchantName(item.getUsername());
            biMerchantStatistics.setMerchantCode(item.getCode());
            biMerchantStatistics.setMerchantType(item.getMerchantType());
            biMerchantStatistics.setCreateTime(LocalDateTime.now());
            biMerchantStatistics.setUpdateTime(LocalDateTime.now());
            biMerchantStatistics.setDateTime(dateStr);
            // 临时用 frozenAmount、memberType字段接收一下

            List<MemberInfo> merchantInfoList = memberInfoFuture.get();
            MemberInfo info;
            Optional<MemberInfo> merchantInfoOpt = merchantInfoList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(item.getCode())).findFirst();
            if (merchantInfoOpt.isPresent()) {
                info = merchantInfoOpt.get();


                biMerchantStatistics.setMemberNum(info.getNum().longValue());
                biMerchantStatistics.setMemberBalance(info.getBalance());
                List<MemberInfo> yesterdayList = yesterdayFuture.get();
                Optional<MemberInfo> yesterdayOpt = yesterdayList.stream().filter(m -> m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                yesterdayOpt.ifPresent(memberInfo -> biMerchantStatistics.setYesterdayActiveNum(memberInfo.getNum().longValue()));


                List<MemberInfo> sevenList = sevenFuture.get();
                Optional<MemberInfo> sevenOpt = sevenList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                sevenOpt.ifPresent(memberInfo -> biMerchantStatistics.setSevenActiveNum(memberInfo.getNum().longValue()));

                List<MemberInfo> thirtyList = thirtyFuture.get();
                Optional<MemberInfo> thirtyOpt = thirtyList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                thirtyOpt.ifPresent(memberInfo -> biMerchantStatistics.setThirtyActiveNum(memberInfo.getNum().longValue()));

                List<MemberInfo> realNameList = realNameNumFuture.get();
                Optional<MemberInfo> realNameOpt = realNameList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                realNameOpt.ifPresent(memberInfo -> biMerchantStatistics.setRealNameNum(memberInfo.getNum().longValue()));

                List<MemberInfo> buyList = merchantBuyNumFuture.get();
                Optional<MemberInfo> buyOpt = buyList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                buyOpt.ifPresent(memberInfo -> biMerchantStatistics.setBuyNum(memberInfo.getNum().longValue()));

                List<MemberInfo> sellList = merchantSellFuture.get();
                Optional<MemberInfo> sellOpt = sellList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                sellOpt.ifPresent(memberInfo -> biMerchantStatistics.setSellNum(memberInfo.getNum().longValue()));

                List<MemberInfo> rechargeList = rechargeFuture.get();
                Optional<MemberInfo> rechargeOpt = rechargeList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                rechargeOpt.ifPresent(memberInfo -> biMerchantStatistics.setRechargeNum(memberInfo.getNum().longValue()));

                List<MemberInfo> withdrawList = merchantWithdrawNumFuture.get();
                Optional<MemberInfo> withdrawOpt = withdrawList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();
                withdrawOpt.ifPresent(memberInfo -> biMerchantStatistics.setWithdrawNum(memberInfo.getNum().longValue()));

                List<MemberInfo> payFeeList = payFeeFuture.get();
                Optional<MemberInfo> payFeeOpt = payFeeList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();

                List<MemberInfo> withdrawFeeList = withdrawFeeFuture.get();
                Optional<MemberInfo> withdrawFeeOpt = withdrawFeeList.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(info.getMerchantCode())).findFirst();

                if (payFeeOpt.isPresent() && withdrawFeeOpt.isPresent()) {
                    biMerchantStatistics.setCost(payFeeOpt.get().getBalance().add(withdrawFeeOpt.get().getBalance()));
                }

            }
            biMerchantStatisticsMapper.insert(biMerchantStatistics);
        }


    }
}
