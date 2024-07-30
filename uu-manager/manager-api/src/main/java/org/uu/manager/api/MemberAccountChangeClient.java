package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.C2cConfigDTO;
import org.uu.common.pay.dto.MemberAccountChangeDTO;
import org.uu.common.pay.dto.MemberAccountChangeExportDTO;
import org.uu.common.pay.req.C2cConfigReq;
import org.uu.common.pay.req.MemberAccountChangeReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "member-accountchange")
public interface MemberAccountChangeClient {


    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/memberAccounthange/listpage")
    RestResult<List<MemberAccountChangeDTO>> listpage(@RequestBody MemberAccountChangeReq req);


    @PostMapping("/api/v1/memberAccounthange/listpageForExport")
    RestResult<List<MemberAccountChangeExportDTO>> listpageForExport(@RequestBody MemberAccountChangeReq req);




}
