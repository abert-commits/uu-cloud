package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberAccountChangeDTO;
import org.uu.common.pay.dto.MemberBlackDTO;
import org.uu.common.pay.req.MemberAccountChangeReq;
import org.uu.common.pay.req.MemberBlackReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "member-Black")
public interface MemberBlackClient {


    @PostMapping("/api/v1/memberBlack/listPage")
    RestResult<List<MemberBlackDTO>> listPage(MemberBlackReq req);

    @PostMapping("/api/v1/memberBlack/removeBlack")
    RestResult removeBlack(MemberBlackReq req);
}
