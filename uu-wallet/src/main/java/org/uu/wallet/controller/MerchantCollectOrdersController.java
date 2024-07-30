package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtBuySuccessOrderDTO;
import org.uu.common.pay.req.UsdtBuyOrderReq;
import org.uu.wallet.service.IMerchantCollectOrdersService;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 商户代收订单表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-01-05
 */
@RestController
@RequestMapping("/merchant-collect-orders")
public class MerchantCollectOrdersController {

    @Resource
    private IMerchantCollectOrdersService merchantCollectOrdersService;

    @PostMapping("/merchantSuccessOrderPage")
    @ApiOperation(value = "商户会员usdt交易成功订单")
    public RestResult<List<UsdtBuySuccessOrderDTO>> merchantSuccessOrderPage(@RequestBody UsdtBuyOrderReq req) {
        PageReturn<UsdtBuySuccessOrderDTO> usdtBuySuccessOrderDTOPageReturn = merchantCollectOrdersService.merchantSuccessOrdersPage(req);
        return RestResult.page(usdtBuySuccessOrderDTOPageReturn);
    }
}
