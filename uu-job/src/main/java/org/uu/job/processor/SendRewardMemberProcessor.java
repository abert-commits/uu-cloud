package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.job.feign.TaskCollectionRecordFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;


@Component("sendRewardMemberProcessor")
@Slf4j
@RequiredArgsConstructor
public class SendRewardMemberProcessor implements BasicProcessor {

    @Autowired
    private TaskCollectionRecordFeignClient taskCollectionRecordFeignClient;

    /**
     * 定时任务 发送获奖会员
     *
     * @param context
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {

        log.info("定时任务执行: 发送获奖会员, 调用发送接口");
        long start = System.currentTimeMillis();
        RestResult<Boolean> sendResult = taskCollectionRecordFeignClient.sendRewardMember();
        log.info("定时任务执行: 发送获奖会员, 调用发送接口返回:{}, cost:{}", sendResult, System.currentTimeMillis() - start);

        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
