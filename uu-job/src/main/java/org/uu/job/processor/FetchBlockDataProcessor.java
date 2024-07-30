package org.uu.job.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uu.job.rabbitmq.FetchBlockDataTaskMessageSender;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.entity.TaskInfo;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDateTime;


@Component("fetchBlockDataProcessor")
@Slf4j
@RequiredArgsConstructor
public class FetchBlockDataProcessor implements BasicProcessor {

    @Autowired
    private FetchBlockDataTaskMessageSender fetchBlockDataTaskMessageSender;

    /**
     * 定时任务 USDT自动上分
     *
     * @author Simon
     * @date 2023/12/01
     */
    @Override
    public ProcessResult process(TaskContext context) {

        log.info("定时任务执行: 区块数据拉取, 当前时间: {}", LocalDateTime.now());
        Long l = System.currentTimeMillis();

        TaskInfo taskInfo = new TaskInfo(String.valueOf(l), TaskTypeEnum.FETCH_BLOCK_DATA.getCode(), l);

        //发送MQ 区块数据拉取
        fetchBlockDataTaskMessageSender.sendFetchBlockDataMessage(taskInfo);

        // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        return new ProcessResult(true, "return success");
    }

}
