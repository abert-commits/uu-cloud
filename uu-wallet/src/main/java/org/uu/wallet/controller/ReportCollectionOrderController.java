package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CollectionOrderDTO;
import org.uu.common.pay.req.CollectionOrderReq;
import org.uu.wallet.service.IReportCollectionOrderService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/reportCollectionOrder", "/reportCollectionOrder"})
@Api(description = "代收订单报表控制器控制器")
@ApiIgnore
public class ReportCollectionOrderController {

    private final IReportCollectionOrderService reportCollectionOrderService;



    @PostMapping("/dayList")
    @ApiOperation(value = "查询代收订单列表")
    public RestResult<List<CollectionOrderDTO>> dayList(@RequestBody(required = false) @ApiParam CollectionOrderReq collectionOrderReq) {
        PageReturn<CollectionOrderDTO> collectionOrderPage = reportCollectionOrderService.listDayPage(collectionOrderReq);
        return RestResult.page(collectionOrderPage);
    }

    @PostMapping("/mothList")
    @ApiOperation(value = "查询代收订单列表")
    public RestResult<List<CollectionOrderDTO>> mothList(@RequestBody(required = false) @ApiParam CollectionOrderReq collectionOrderReq) {
        PageReturn<CollectionOrderDTO> collectionOrderPage = reportCollectionOrderService.listMothPage(collectionOrderReq);
        return RestResult.page(collectionOrderPage);
    }

    @PostMapping("/dayListTotal")
    @ApiOperation(value = "查询代收订单列表")
    public RestResult<List<CollectionOrderDTO>> dayListTotal(@RequestBody(required = false) @ApiParam CollectionOrderReq collectionOrderReq) {
        PageReturn<CollectionOrderDTO> collectionOrderPage = reportCollectionOrderService.listDayPage(collectionOrderReq);
        return RestResult.page(collectionOrderPage);
    }

    @PostMapping("/mothListTotal")
    @ApiOperation(value = "查询代收订单列表")
    public RestResult<List<CollectionOrderDTO>> mothListTotal(@RequestBody(required = false) @ApiParam CollectionOrderReq collectionOrderReq) {
        PageReturn<CollectionOrderDTO> collectionOrderPage = reportCollectionOrderService.listMothPage(collectionOrderReq);
        return RestResult.page(collectionOrderPage);
    }



}
