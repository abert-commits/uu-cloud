package org.uu.job.processor;

import lombok.extern.slf4j.Slf4j;
import org.uu.job.feign.LinkKycPartnerFeignClient;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import javax.annotation.Resource;


/**
 * @author lukas
 */
@Component("linkKycPartnerProcessor")
@Slf4j
public class LinkKycPartnerProcessor implements BasicProcessor {

    @Resource
    LinkKycPartnerFeignClient kycPartnerFeignClient;

    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {
        kycPartnerFeignClient.linKycPartner();
        return new ProcessResult(true, "return success");
    }
}
