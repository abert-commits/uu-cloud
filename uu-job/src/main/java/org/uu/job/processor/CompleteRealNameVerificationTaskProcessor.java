package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.job.rabbitmq.CompleteRealNameVerificationTaskMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDateTime;


@Component("completeRealNameVerificationTaskProcessor")
@Slf4j
@RequiredArgsConstructor
public class CompleteRealNameVerificationTaskProcessor implements BasicProcessor {

    @Autowired
    private CompleteRealNameVerificationTaskMessageSender completeRealNameVerificationTaskMessageSender;

    /**
     * 定时任务 完成实名认证任务
     *
     * @author Simon
     * @date 2023/12/01
     */
    @Override
    public ProcessResult process(TaskContext context) {

        log.info("定时任务执行: 完成实名认证任务, 当前时间: {}", LocalDateTime.now());

        //发送MQ 完成实名认证任务
        completeRealNameVerificationTaskMessageSender.sendClearDailyTransactionDataMessage("completeRealNameVerificationTask");

        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
