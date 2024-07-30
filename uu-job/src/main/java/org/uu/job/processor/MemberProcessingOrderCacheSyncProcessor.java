package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.StringUtils;
import org.uu.job.feign.JobTaskFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;


@Component("memberProcessingOrderCacheSyncProcessor")
@Slf4j
@RequiredArgsConstructor
public class MemberProcessingOrderCacheSyncProcessor implements BasicProcessor {

    @Autowired
    private JobTaskFeignClient jobTaskFeignClient;

    /**
     * 定时任务 同步会员进行中的订单缓存
     *
     * @param context
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {
        String jobParams = context.getJobParams();
        Integer cmd = StringUtils.isEmpty(jobParams) ? null : Integer.parseInt(jobParams);
        log.info("定时任务执行: 同步历史数据缓存, 调用同步会员进行中的订单缓存接口");
        long start = System.currentTimeMillis();
        RestResult result = jobTaskFeignClient.syncMemberProcessingOrderCache();
        log.info("定时任务执行: 同步历史数据缓存, 调用同步会员进行中的订单缓存接口返回:{}, cost:{}", result, System.currentTimeMillis() - start);
        if (cmd!=null && cmd == 1) {
            log.info("定时任务执行: 同步历史数据缓存, 调用同步卖出匹配订单历史数据缓存接口");
            start = System.currentTimeMillis();
            result = jobTaskFeignClient.syncHistoryMatchSellOrder();
            log.info("定时任务执行: 同步历史数据缓存, 调用同步卖出匹配订单历史数据缓存接口返回:{}, cost:{}", result, System.currentTimeMillis() - start);
        }

        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
