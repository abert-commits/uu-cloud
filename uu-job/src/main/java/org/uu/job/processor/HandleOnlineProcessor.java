package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.mapper.BiMerchantDailyMapper;
import org.uu.manager.mapper.BiMerchantMonthMapper;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;



/**
 * 清洗商户报表数据
 * @author Admin
 */
@Component("handleOnlineProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandleOnlineProcessor implements BasicProcessor {

    private final BiMerchantDailyMapper biMerchantDailyMapper;
    private final CollectionOrderMapper collectionOrderMapper;
    private final BiMerchantMonthMapper biMerchantMonthMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final MerchantInfoMapper merchantInfoMapper;

    private final RedisUtils redisUtils;

    /**
     * 批次大小
     */
    private static final int BATCH_SIZE = 1000;

    private static final String TODAY = "today";


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
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {

        try {
            // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
            OmsLogger omsLogger = context.getOmsLogger();
            omsLogger.info("统计在线人数参数->{}", context.getJobParams());
            log.info("统计在线人数参数->{}", context.getJobParams());
            String jobSwitch = (String) redisUtils.get(RedisConstants.JOB_SWITCH);
            if(!StringUtils.isEmpty(jobSwitch) && GlobalConstants.STATUS_OFF.equals(Integer.parseInt(jobSwitch))){
                log.info("统计在线人数Job未执行,Job开关已关闭.");
                return new ProcessResult(true, "Job switch turned off");
            }

        } catch (Exception e) {
            log.error("HandlePaymentOrderProcessor.process" + e.getMessage());
        }

        return new ProcessResult(true, "return success");
    }


}
