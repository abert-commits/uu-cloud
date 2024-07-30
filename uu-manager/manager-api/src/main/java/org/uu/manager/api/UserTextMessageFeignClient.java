package org.uu.manager.api;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "user-log")
public interface UserTextMessageFeignClient {

    @PostMapping("/api/v1/userVerificationCodes/listPage")
    RestResult<List<UserVerificationCodeslistPageDTO>> listPage(@RequestBody UserTextMessageReq req);
    @PostMapping("/api/v1/memberLoginLogs/listPage")
    RestResult<List<MemberLoginLogsDTO>> memberLoginLogsListPage(MemberLoginLogsReq req);
    @PostMapping("/api/v1/memberOperationLogs/listPage")
    RestResult<List<MemberOperationLogsDTO>> memberOperationLogsListPage(MemberOperationLogsReq memberOperationLogsReq);
}
