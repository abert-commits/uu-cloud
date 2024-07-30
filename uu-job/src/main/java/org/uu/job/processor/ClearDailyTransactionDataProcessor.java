package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.job.rabbitmq.ClearDailyTransactionDataMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDateTime;


@Component("clearDailyTransactionDataProcessor")
@Slf4j
@RequiredArgsConstructor
public class ClearDailyTransactionDataProcessor implements BasicProcessor {

    @Autowired
    private ClearDailyTransactionDataMessageSender clearDailyTransactionDataMessageSender;

    /**
     * 定时任务 清空会员每日交易数据
     *
     * @author Simon
     * @date 2023/12/01
     */
    @Override
    public ProcessResult process(TaskContext context) {

        log.info("定时任务执行: 清空会员每日交易数据, 当前时间: {}", LocalDateTime.now());

        //发送MQ 执行清空会员每日交易数据
        clearDailyTransactionDataMessageSender.sendClearDailyTransactionDataMessage("clearDailyTransactionData");


        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
