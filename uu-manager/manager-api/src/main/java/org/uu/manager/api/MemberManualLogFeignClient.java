package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberLoginLogsDTO;
import org.uu.common.pay.dto.MemberManualLogDTO;
import org.uu.common.pay.dto.MemberOperationLogsDTO;
import org.uu.common.pay.dto.UserVerificationCodeslistPageDTO;
import org.uu.common.pay.req.MemberLoginLogsReq;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.common.pay.req.MemberOperationLogsReq;
import org.uu.common.pay.req.UserTextMessageReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "member-manual-log")
public interface MemberManualLogFeignClient {

    @PostMapping("/api/v1/memberManualLog/listPage")
    RestResult<List<MemberManualLogDTO>> listPage(@RequestBody MemberManualLogsReq req);
    @PostMapping("/api/v1/memberManualLog/listPage")
    RestResult<List<MemberLoginLogsDTO>> memberLoginLogsListPage(MemberLoginLogsReq req);
    @PostMapping("/api/v1/memberManualLog/listPage")
    RestResult<List<MemberOperationLogsDTO>> memberOperationLogsListPage(MemberOperationLogsReq memberOperationLogsReq);
}
