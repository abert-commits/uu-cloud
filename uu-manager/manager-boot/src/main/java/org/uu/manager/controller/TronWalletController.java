package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TronWalletAddressDTO;
import org.uu.common.pay.req.UsdtAddrPageReq;
import org.uu.manager.api.TronWalletClient;

import javax.annotation.Resource;
import java.util.List;

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
@Api(description = "波场钱包地址")
public class TronWalletController {

    @Resource
    TronWalletClient tronWalletClient;

    @PostMapping("/addressListPage")
    @ApiOperation(value = "分页查询波场钱包地址列表")
    public RestResult<List<TronWalletAddressDTO>> addressListPage(@RequestBody @ApiParam UsdtAddrPageReq req) {
        return tronWalletClient.addressListPage(req);
    }

}
