package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.ApplyDistributedDTO;
import org.uu.common.pay.dto.C2cConfigDTO;
import org.uu.common.pay.req.ApplyDistributedReq;
import org.uu.common.pay.req.C2cConfigReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "c2c-config")
public interface C2cConfigClient {


    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/c2cConfig/listpage")
    RestResult<List<C2cConfigDTO>> listpage(@RequestBody C2cConfigReq req);

    /**
     *
     * @param req
     * @return
     */
    @PostMapping("/api/v1/c2cConfig/update")
    RestResult<C2cConfigDTO> update(@RequestBody C2cConfigReq req);


    /**
     * 详情
     * @param
     * @param
     * @return
     */
    @PostMapping("/api/v1/c2cConfig/detaill")
    RestResult<C2cConfigDTO> detaill(@RequestBody C2cConfigReq req);


    @PostMapping("/api/v1/c2cConfig/delete")
    RestResult<C2cConfigDTO> delete(@RequestBody C2cConfigReq req);


}
