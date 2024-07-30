package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.PaymentOrderDTO;
import org.uu.common.pay.req.PaymentOrderReq;
import org.uu.wallet.service.IReportPaymentOrderService;
import org.uu.wallet.vo.PaymentOrderListVo;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/reportPaymentOrder", "/reportPaymentOrder"})
@Api(description = "代付订单控制器")
@ApiIgnore
public class ReportPaymentOrderController {

    private final IReportPaymentOrderService reportPaymentOrderService;



    @PostMapping("/listDay")
    @ApiOperation(value = "查看代付订单")
    public RestResult listDay(@RequestBody(required = false) @ApiParam PaymentOrderReq paymentOrder) {
        PageReturn<PaymentOrderDTO> roleVOPage = reportPaymentOrderService.listDayPage(paymentOrder);
        return RestResult.ok(roleVOPage);
    }
    @PostMapping("/listMoth")
    @ApiOperation(value = "查看代付订单")
    public RestResult listMoth(@RequestBody(required = false) @ApiParam PaymentOrderReq paymentOrder) {
        PageReturn<PaymentOrderDTO> roleVOPage = reportPaymentOrderService.listMothPage(paymentOrder);
        return RestResult.ok(roleVOPage);
    }

    @PostMapping("/listDayTotal")
    @ApiOperation(value = "查看代付订单")
    public RestResult listDayTotal(@RequestBody(required = false) @ApiParam PaymentOrderReq paymentOrder) {
        PageReturn<PaymentOrderDTO> roleVOPage = reportPaymentOrderService.listMothPage(paymentOrder);
        return RestResult.ok(roleVOPage);
    }

    @PostMapping("/listMothTotal")
    @ApiOperation(value = "查看代付订单")
    public RestResult listMothTotal(@RequestBody(required = false) @ApiParam PaymentOrderReq paymentOrder) {
        PageReturn<PaymentOrderDTO> roleVOPage = reportPaymentOrderService.listMothTotal(paymentOrder);
        return RestResult.ok(roleVOPage);
    }


}
