package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.MatchingOrderAppealReq;
import org.uu.common.pay.req.MatchingOrderIdReq;
import org.uu.common.pay.req.MatchingOrderReq;
import org.uu.common.pay.req.MemberBlackReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "correlation-member")
public interface CorrelationMemberClient {


    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/correlationMember/listPage")
    RestResult<List<CorrelationMemberDTO>> listPage(@RequestBody MemberBlackReq req);
}
