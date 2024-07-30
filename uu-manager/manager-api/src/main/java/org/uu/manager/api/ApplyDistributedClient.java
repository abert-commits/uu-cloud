package org.uu.manager.api;

import io.swagger.annotations.ApiParam;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.ApplyDistributedDTO;

import org.uu.common.pay.req.ApplyDistributedListPageReq;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "Apply-Distributed")
public interface ApplyDistributedClient {


    /**
     *
     * @param
     * @return
     */
   // @Headers({"Content-Type: application/json","Accept: application/json"})
    @PostMapping("/api/v1/applyDistributed/listpage")
    RestResult<List<ApplyDistributedDTO>> listpage(@RequestBody ApplyDistributedListPageReq req);

    /**
     *  下发申请接口
     * @param req
     * @return
     */

    //@Headers({"Content-Type: application/json","Accept: application/json"})
    @PostMapping("/api/v1/applyDistributed/appliy")
    RestResult<ApplyDistributedDTO> appliy(@RequestBody ApplyDistributedListPageReq req);


    /**
     * 修改商户提现usdt地址
     * @param
     * @param
     * @return
     */

    //@Headers({"Content-Type: application/json","Accept: application/json"})
    @PostMapping("/api/v1/applyDistributed/distribute")
    RestResult<ApplyDistributedDTO> distribute(@RequestParam("id") Long id);

    @PostMapping("/api/v1/applyDistributed/nodistribute")
    RestResult<ApplyDistributedDTO> nodistribute(@RequestParam("id") Long id);

    @PostMapping("/api/v1/applyDistributed/listRecordPage")
    RestResult<List<ApplyDistributedDTO>> listRecordPage(@RequestBody ApplyDistributedListPageReq req);

    @PostMapping("/api/v1/applyDistributed/listRecordTotal")
    RestResult<ApplyDistributedDTO> listRecordTotal(@RequestBody ApplyDistributedListPageReq req);


}
