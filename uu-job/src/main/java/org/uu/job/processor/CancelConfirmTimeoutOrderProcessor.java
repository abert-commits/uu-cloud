package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.StringUtils;
import org.uu.job.feign.HandleOrderTimeoutFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;


@Component("cancelConfirmTimeoutOrderProcessor")
@Slf4j
@RequiredArgsConstructor
public class CancelConfirmTimeoutOrderProcessor implements BasicProcessor {

    @Autowired
    private HandleOrderTimeoutFeignClient handleOrderTimeoutFeignClient;

    /**
     * 定时任务 取消会员确认超时的订单
     *
     * @param context
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {
        long start = System.currentTimeMillis();
        String jobParams = context.getJobParams();
        log.info("定时任务执行: 取消会员确认超时的订单, 调用发送接口, jobParams:{}", jobParams);
        Integer startDays = StringUtils.isEmpty(jobParams) ? null : Integer.parseInt(jobParams);
        RestResult result = handleOrderTimeoutFeignClient.cancelConfirmTimeoutOrder(startDays);
        log.info("定时任务执行: 取消会员确认超时的订单, 调用发送接口返回:{}, cost:{}", result, System.currentTimeMillis() - start);

        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
