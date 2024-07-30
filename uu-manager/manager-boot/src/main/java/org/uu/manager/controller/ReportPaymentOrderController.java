//package org.uu.manager.controller;
//
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.page.PageReturn;
//import org.uu.common.core.result.RestResult;
//import org.uu.wallet.entity.PaymentOrder;
//import org.uu.wallet.req.PaymentOrderReq;
//import org.uu.wallet.service.IReportPaymentOrderService;
//import org.uu.wallet.vo.CollectionOrderInfoVo;
//import org.uu.wallet.vo.PaymentOrderListVo;
//import org.springframework.web.bind.annotation.*;
//
//
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//@RequestMapping(value = {"/api/v1/reportPaymentOrder", "/reportPaymentOrder"})
//@Api(description = "代付订单控制器")
//public class ReportPaymentOrderController {
//
//    private final IReportPaymentOrderService reportPaymentOrderService;
//
//    @PostMapping("/save")
//    @ApiOperation(value = "保存代付订单")
//    public RestResult<PaymentOrder> save(@RequestBody @ApiParam PaymentOrder paymentOrder) {
//        reportPaymentOrderService.save(paymentOrder);
//        return RestResult.ok(paymentOrder);
//    }
//
//    @PostMapping("/update")
//    @ApiOperation(value = "更新代付订单")
//    public RestResult update(@RequestBody @ApiParam PaymentOrder paymentOrder) {
//        boolean su = reportPaymentOrderService.updateById(paymentOrder);
//        return RestResult.ok();
//    }
//
//    @PostMapping("/list")
//    @ApiOperation(value = "查看代付订单")
//    public RestResult list(@RequestBody(required = false) @ApiParam PaymentOrderReq paymentOrder) {
//        PageReturn<PaymentOrderListVo> roleVOPage = reportPaymentOrderService.listPage(paymentOrder);
//        return RestResult.ok(roleVOPage);
//    }
//
//    @GetMapping("/paymentOrderInfo")
//    @ApiOperation(value = "查询代付订单详情")
//    public RestResult<CollectionOrderInfoVo> paymentOrderInfo(@ApiParam(name = "商户订单号") String merchantOrder) {
//        return reportPaymentOrderService.getPaymentOrderInfoByOrderNo(merchantOrder);
//    }
//
//}
