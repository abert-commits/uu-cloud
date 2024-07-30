package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtAddressDTO;
import org.uu.common.pay.dto.UsdtAddressMemberDTO;
import org.uu.common.pay.dto.UsdtAddressMerchantDTO;
import org.uu.common.pay.req.UsdtAddrPageReq;
import org.uu.wallet.service.ITronAddressService;
import org.uu.wallet.service.LoadBalanceService;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 波场用户钱包 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@RestController
@RequestMapping("/tron-address")
public class TronAddressController {
    @Resource
    private ITronAddressService tronAddressService;
    @Resource
    private LoadBalanceService loadBalanceService;

    @PostMapping("/addressListPage")
    @ApiOperation(value = "USDT地址管理分页列表")
    public RestResult<UsdtAddressDTO> addressListPage(@RequestBody @ApiParam UsdtAddrPageReq req) {
        PageReturn<UsdtAddressDTO> usdtAddressDTOPageReturn = tronAddressService.addressListPage(req);
        return RestResult.page(usdtAddressDTOPageReturn);
    }

    @PostMapping("/addressExportPage")
    @ApiOperation(value = "USDT地址管理导出会员列表")
    public RestResult<UsdtAddressMemberDTO> addressExportPage(@RequestBody @ApiParam UsdtAddrPageReq req) {
        PageReturn<UsdtAddressMemberDTO> usdtAddressDTOPageReturn = tronAddressService.addressExportPage(req);
        return RestResult.page(usdtAddressDTOPageReturn);
    }

    @PostMapping("/addressMerchantExportPage")
    @ApiOperation(value = "USDT地址管理导出商户列表")
    public RestResult<UsdtAddressMerchantDTO> addressMerchantExportPage(@RequestBody @ApiParam UsdtAddrPageReq req) {
        PageReturn<UsdtAddressMerchantDTO> usdtAddressDTOPageReturn = tronAddressService.addressMerchantExportPage(req);
        return RestResult.page(usdtAddressDTOPageReturn);
    }

    @PostMapping("/collectFundsForAccounts")
    @ApiOperation(value = "批量归集地址")
    public RestResult collectFundsForAccounts(@RequestBody @ApiParam List<String> addressList) {
        return loadBalanceService.collectFundsForAccounts(addressList) ? RestResult.ok() : RestResult.failed();
    }

}
