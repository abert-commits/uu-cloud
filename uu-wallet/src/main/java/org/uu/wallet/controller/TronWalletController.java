package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TronWalletAddressDTO;
import org.uu.wallet.service.ITronWalletService;

/**
 * <p>
 * 波场钱包地址表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-07-12
 */
@RestController
@RequestMapping("/tron-wallet")
public class TronWalletController {

    @Autowired
    private ITronWalletService tronWalletService;

    @PostMapping("/addressListPage")
    @ApiOperation(value = "波场钱包地址分页列表")
    public RestResult<TronWalletAddressDTO> addressListPage(@RequestBody @ApiParam PageRequest req) {
        PageReturn<TronWalletAddressDTO> usdtAddressDTOPageReturn = tronWalletService.addressListPage(req);
        return RestResult.page(usdtAddressDTOPageReturn);
    }

}
