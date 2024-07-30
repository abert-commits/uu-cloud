//package org.uu.manager.controller;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.uu.common.core.page.PageReturn;
//import org.uu.common.core.result.RestResult;
//
//import org.springframework.beans.BeanUtils;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RequiredArgsConstructor
//@RestController
//@RequestMapping(value = {"/api/v1/reportCollectionOrder", "/reportCollectionOrder"})
//@Api(description = "代收订单控制器")
//public class ReportCollectionOrderController {
//
//    private final IReportCollectionOrderService reportCollectionOrderService;
//
//    @PostMapping("/save")
//    @ApiOperation(value = "保存代收订单")
//    public RestResult<MerchantInfo> save(@RequestBody @ApiParam CollectionOrderVo collectionOrderVo) {
//        CollectionOrder collectionOrder = new CollectionOrder();
//        BeanUtils.copyProperties(collectionOrderVo, collectionOrder);
//        reportCollectionOrderService.save(collectionOrder);
//        return RestResult.ok();
//    }
//
//    @PostMapping("/update")
//    @ApiOperation(value = "更新代收订单")
//    public RestResult update(@RequestBody @ApiParam CollectionOrder collectionOrder) {
//        boolean su = reportCollectionOrderService.updateById(collectionOrder);
//        return RestResult.ok();
//    }
//
//    @PostMapping("/list")
//    @ApiOperation(value = "查询代收订单列表")
//    public RestResult<PageReturn> list(@RequestBody(required = false) @ApiParam CollectionOrderReq collectionOrderReq) {
//        PageReturn<CollectionOrderListVo> collectionOrderPage = reportCollectionOrderService.listPage(collectionOrderReq);
//        return RestResult.ok(collectionOrderPage);
//    }
//
//    @GetMapping("/collectionOrderInfo")
//    @ApiOperation(value = "查询代收订单详情")
//    public RestResult<CollectionOrderInfoVo> collectionOrderInfo(@ApiParam(name = "商户订单号") String merchantOrder) {
//        return reportCollectionOrderService.getCollectionOrderInfoByOrderNo(merchantOrder);
//    }
//
//    @GetMapping("/selectList")
//    @ApiOperation(value = "查询币种,支付类型列表")
//    public RestResult<selectListVo> selectList() {
//        return reportCollectionOrderService.selectList();
//    }
//}
