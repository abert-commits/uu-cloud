package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtBuyOrderDTO;
import org.uu.common.pay.dto.UsdtBuyOrderExportDTO;
import org.uu.common.pay.dto.UsdtBuyOrderInfoDTO;
import org.uu.common.pay.dto.UsdtBuySuccessOrderDTO;
import org.uu.common.pay.req.UsdtBuyOrderGetInfoReq;
import org.uu.common.pay.req.UsdtBuyOrderIdReq;
import org.uu.common.pay.req.UsdtBuyOrderReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.ChangeModeEnum;
import org.uu.wallet.Enum.CurrenceEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.entity.UsdtBuyOrder;
import org.uu.wallet.service.*;
import org.uu.wallet.util.AmountChangeUtil;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @author
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "usdt买入订单")
@RequestMapping(value = {"/api/v1/usdtBuyOrder", "/usdtBuyOrder"})
@ApiIgnore
public class UsdtBuyOrderController {
    private final IUsdtBuyOrderService usdtBuyOrderService;


    @PostMapping("/listpage")
    @ApiOperation(value = "usdt订单买入列表")
    public RestResult<List<UsdtBuyOrderDTO>> listpage(@RequestBody @ApiParam UsdtBuyOrderReq req) {
        PageReturn<UsdtBuyOrderDTO> payConfigPage = usdtBuyOrderService.listPage(req);
        return RestResult.page(payConfigPage);
    }

    @PostMapping("/listpageForExport")
    @ApiOperation(value = "usdt订单买入列表")
    public RestResult<List<UsdtBuyOrderExportDTO>> listpageForExport(@RequestBody UsdtBuyOrderReq req) {
        PageReturn<UsdtBuyOrderExportDTO> payConfigPage = usdtBuyOrderService.listpageForExport(req);
        return RestResult.page(payConfigPage);
    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "查看")
    public RestResult<UsdtBuyOrderInfoDTO> getInfo(@RequestBody @ApiParam UsdtBuyOrderGetInfoReq req) {
        UsdtBuyOrder usdtBuyOrder = new UsdtBuyOrder();
        usdtBuyOrder.setId(req.getId());
        usdtBuyOrder = usdtBuyOrderService.getById(usdtBuyOrder);
        UsdtBuyOrderInfoDTO usdtBuyOrderInfoDTO = new UsdtBuyOrderInfoDTO();
        usdtBuyOrderInfoDTO.setUsdtProof(usdtBuyOrder.getUsdtProof());
        usdtBuyOrderInfoDTO.setId(usdtBuyOrder.getId());
        return RestResult.ok(usdtBuyOrderInfoDTO);
    }


    @PostMapping("/pay")
    @ApiOperation(value = "支付")
    public RestResult<UsdtBuyOrderDTO> pay(@RequestBody @ApiParam UsdtBuyOrderIdReq req) {
        return usdtBuyOrderService.pay(req);
    }

    @PostMapping("/nopay")
    @ApiOperation(value = "未支付")
    public RestResult<UsdtBuyOrderDTO> nopay(@RequestBody @ApiParam UsdtBuyOrderIdReq req) {
        UsdtBuyOrder usdtBuyOrder = new UsdtBuyOrder();
        usdtBuyOrder.setId(req.getId());

        usdtBuyOrder = usdtBuyOrderService.getById(usdtBuyOrder);
        if (usdtBuyOrder.getStatus().equals(OrderStatusEnum.SUCCESS.getCode())) {
            return RestResult.failed();
        }
        usdtBuyOrder.setStatus(OrderStatusEnum.WAS_CANCELED.getCode());
        usdtBuyOrder.setRemark(req.getRemark());
        usdtBuyOrderService.updateById(usdtBuyOrder);
        UsdtBuyOrderDTO usdtBuyOrderDTO = new UsdtBuyOrderDTO();
        BeanUtils.copyProperties(usdtBuyOrder, usdtBuyOrderDTO);
        return RestResult.ok(usdtBuyOrderDTO);
    }

    @PostMapping("/successOrderListPage")
    @ApiOperation(value = "usdt交易成功订单")
    public RestResult<List<UsdtBuySuccessOrderDTO>> successOrderListPage(@RequestBody UsdtBuyOrderReq req) {
        PageReturn<UsdtBuySuccessOrderDTO> usdtBuySuccessOrderDTOPageReturn = usdtBuyOrderService.successOrderListPage(req);
        return RestResult.page(usdtBuySuccessOrderDTOPageReturn);
    }

}
