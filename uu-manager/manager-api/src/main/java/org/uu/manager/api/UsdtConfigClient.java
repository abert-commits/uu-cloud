package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtAddressDTO;
import org.uu.common.pay.dto.UsdtAddressMemberDTO;
import org.uu.common.pay.dto.UsdtAddressMerchantDTO;
import org.uu.common.pay.dto.UsdtConfigDTO;
import org.uu.common.pay.req.*;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "usdt-config")
public interface UsdtConfigClient {


    /**
     * @param
     * @return
     */
    @PostMapping("/api/v1/usdtConfig/listpage")
    RestResult<List<UsdtConfigDTO>> listpage(@RequestBody UsdtConfigPageReq req);

    /**
     * USDT地址管理分页列表
     */
    @PostMapping("/tron-address/addressListPage")
    RestResult<List<UsdtAddressDTO>> addressListPage(@RequestBody UsdtAddrPageReq req);

    /**
     * USDT地址管理会员导出列表
     */
    @PostMapping("/tron-address/addressExportPage")
    RestResult<List<UsdtAddressMemberDTO>> addressExportPage(@RequestBody UsdtAddrPageReq req);

    /**
     * USDT地址管理商户导出列表
     */
    @PostMapping("/tron-address/addressMerchantExportPage")
    RestResult<List<UsdtAddressMerchantDTO>> addressMerchantExportPage(@RequestBody UsdtAddrPageReq req);

    /**
     * 批量归集地址
     */
    @PostMapping("/tron-address/collectFundsForAccounts")
    RestResult collectFundsForAccounts(@RequestBody List<String> usdtAddressList);

    /**
     * @param req
     * @return
     */
    @PostMapping("/api/v1/usdtConfig/create")
    RestResult<UsdtConfigDTO> create(@RequestBody UsdtConfigCreateReq req);


    @PostMapping("/api/v1/usdtConfig/update")
    RestResult<UsdtConfigDTO> update(@RequestBody UsdtConfigReq req);


    @PostMapping("/api/v1/usdtConfig/changeStatus")
    RestResult<UsdtConfigDTO> changeStatus(@RequestBody UsdtConfigQueryReq req);

    @PostMapping("/api/v1/usdtConfig/getInfo")
    RestResult<UsdtConfigDTO> getInfo(@RequestBody UsdtConfigIdReq req);


    @PostMapping("/api/v1/usdtConfig/delete")
    RestResult delete(@RequestBody UsdtConfigIdReq req);


}
