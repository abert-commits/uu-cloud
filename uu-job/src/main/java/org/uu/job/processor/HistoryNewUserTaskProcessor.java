package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.job.feign.JobTaskFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;


@Component("historyNewUserTaskProcessor")
@Slf4j
@RequiredArgsConstructor
public class HistoryNewUserTaskProcessor implements BasicProcessor {

    @Autowired
    private JobTaskFeignClient jobTaskFeignClient;

    /**
     * 定时任务 处理新人任务历史数据
     *
     * @param context
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {
        String taskType = context.getJobParams();
        log.info("定时任务执行: 处理新人任务历史数据, taskType:{}, 调用接口", taskType);
        long start = System.currentTimeMillis();
        RestResult result = jobTaskFeignClient.processHistoryNewUserTask(taskType);
        log.info("定时任务执行: 处理新人任务历史数据, taskType:{}, 调用接口返回:{}, cost:{}", taskType, result, System.currentTimeMillis() - start);

        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
