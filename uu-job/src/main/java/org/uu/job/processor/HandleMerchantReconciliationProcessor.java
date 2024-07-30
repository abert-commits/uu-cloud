package org.uu.job.processor;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.entity.BiMerchantReconciliation;
import org.uu.manager.mapper.BiMerchantReconciliationMapper;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * 清洗商户对账数据
 *
 * @author Admin
 */
@Component("handleMerchantReconciliationProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandleMerchantReconciliationProcessor implements BasicProcessor {

    private final BiMerchantReconciliationMapper biMerchantReconciliationMapper;
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
            omsLogger.info("清洗商户对账数据参数->{}", context.getJobParams());
            log.info("清洗商户对账数据参数->{}", context.getJobParams());
            String jobSwitch = (String) redisUtils.get(RedisConstants.JOB_SWITCH);
            if (!StringUtils.isEmpty(jobSwitch) && GlobalConstants.STATUS_OFF.equals(Integer.parseInt(jobSwitch))) {
                log.info("清洗商户对账数据Job未执行,Job开关已关闭.");
                return new ProcessResult(true, "Job switch turned off");
            }
            String dateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), GlobalConstants.DATE_FORMAT_DAY);
            // dateType为空代表自动
            biMerchantReconciliationMapper.deleteByDateTime(dateStr);
            handleMerchantReconciliation(dateStr);
        } catch (Exception e) {
            log.error("HandlePaymentOrderProcessor.process" + e.getMessage());
        }

        return new ProcessResult(true, "return success");
    }

    private void handleMerchantReconciliation(String dateStr) throws ExecutionException, InterruptedException {
        int pageNo = 1;
        long totalSize = 0;
        Page<MerchantInfo> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(BATCH_SIZE);
        // startTime <= time < endTime
        page = updateBiMerchantReconciliation(dateStr, page);
        List<MerchantInfo> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            log.info("查询代收记录数为空,直接返回");
            return;
        }
        if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE > 0) {
            totalSize = (page.getTotal() / BATCH_SIZE) + 1;
            log.info("总记录数大于批次,且余数大于0,totalSize->{}", totalSize);
        } else if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE <= 0) {
            totalSize = (page.getTotal() / BATCH_SIZE);
            log.info("总记录数大于批次,且余数等于0,totalSize->{}", totalSize);
        }
        for (int i = 0; i < totalSize; i++) {
            pageNo++;
            page.setCurrent(pageNo);
            page.setSize(BATCH_SIZE);
            updateBiMerchantReconciliation(dateStr, page);
        }
    }

    @NotNull
    private Page<MerchantInfo> updateBiMerchantReconciliation(String dateStr, Page<MerchantInfo> page) throws ExecutionException, InterruptedException {

        LambdaQueryChainWrapper<MerchantInfo> lambdaQuery2 = new LambdaQueryChainWrapper<>(merchantInfoMapper);
        page = merchantInfoMapper.selectPage(page, lambdaQuery2.getWrapper());
        log.info("HandleMerchantReconciliationProcessor总记录数->{}, 日期->{}", page.getRecords().size(), dateStr);
        List<MerchantInfo> records = page.getRecords();


        for (MerchantInfo merchantInfo : records) {
            BiMerchantReconciliation biPaymentOrder = new BiMerchantReconciliation();
            biPaymentOrder.setDateTime(dateStr);
            biPaymentOrder.setMerchantType(merchantInfo.getMerchantType());
            biPaymentOrder.setMerchantBalance(merchantInfo.getBalance());
            biPaymentOrder.setMerchantUp(merchantInfo.getTransferUpAmount());
            biPaymentOrder.setMerchantDown(merchantInfo.getTransferDownAmount());
            biPaymentOrder.setPayMoney(merchantInfo.getTotalPayAmount());
            biPaymentOrder.setWithdrawMoney(merchantInfo.getTotalWithdrawAmount());
            biPaymentOrder.setCreateTime(LocalDateTime.now());
            biPaymentOrder.setUpdateTime(LocalDateTime.now());
            biMerchantReconciliationMapper.insert(biPaymentOrder);
        }




        return page;
    }
}
