package org.uu.job.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.uu.job.feign.KycAutoCompleteFeignClient;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import javax.annotation.Resource;

/**
 * @author lukas
 */
@Component("kycAutoCompletedProcessor")
@Slf4j
public class KycAutoCompletedProcessor  implements BasicProcessor {
    @Resource
    KycAutoCompleteFeignClient kycAutoCompleteFeignClient;


    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        kycAutoCompleteFeignClient.autoCompleteTransactionJob();
        return new ProcessResult(true, "return success");
    }
}
