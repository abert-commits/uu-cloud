package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uu.job.rabbitmq.UsdtLoadBalanceTaskMessageSender;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.entity.TaskInfo;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDateTime;


@Component("usdtLoadBalanceProcessor")
@Slf4j
@RequiredArgsConstructor
public class UsdtLoadBalanceProcessor implements BasicProcessor {

    @Autowired
    private UsdtLoadBalanceTaskMessageSender usdtLoadBalanceTaskMessageSender;

    /**
     * 定时任务 USDT资金归集
     *
     * @author Simon
     * @date 2024/07/12
     */
    @Override
    public ProcessResult process(TaskContext context) {

        log.info("定时任务执行: USDT资金归集, 当前时间: {}", LocalDateTime.now());
        Long l = System.currentTimeMillis();

        TaskInfo taskInfo = new TaskInfo(String.valueOf(l), TaskTypeEnum.USDT_LOAD_BALANCE.getCode(), l);

        //发送MQ USDT资金归集
        usdtLoadBalanceTaskMessageSender.sendUsdtLoadBalanceMessage(taskInfo);

        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
