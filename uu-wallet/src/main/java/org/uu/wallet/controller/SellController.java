package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.Enum.MemberOperationModuleEnum;
import org.uu.wallet.annotation.LogMemberOperation;
import org.uu.wallet.req.CheckUpiIdDuplicateReq;
import org.uu.wallet.req.CollectioninfoIdReq;
import org.uu.wallet.req.FrontendCollectionInfoReq;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.service.IAppealOrderService;
import org.uu.wallet.service.ICollectionInfoService;
import org.uu.wallet.service.ISellService;
import org.uu.wallet.vo.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sellCenter")
@Api(description = "前台-卖出控制器")
@Validated
@Slf4j
public class SellController {

    private final ISellService sellService;
    private final IAppealOrderService appealOrderService;
    private final ICollectionInfoService collectionInfoService;

    @GetMapping("/fetchPageData")
    @ApiOperation(value = "前台-卖出页面接口")
    public RestResult<SellListVo> fetchPageData() {
        return sellService.fetchPageData();
    }

    @PostMapping("/delegateSell")
    @ApiOperation(value = "前台-委托卖出")
    @LogMemberOperation(value = MemberOperationModuleEnum.DELEGATE_SELL)
    public RestResult delegateSell(HttpServletRequest request) {
        //委托卖出处理
        return sellService.delegateSell(request);
    }

    @GetMapping("/getAllPaymentInfo")
    @ApiOperation(value = "前台-获取当前用户所有收款信息")
    public RestResult<AllCollectionInfoVo> getAllPaymentInfo() {
        return collectionInfoService.getAllPaymentInfo();
    }

    @PostMapping("/getSellOrderDetails")
    @ApiOperation(value = "前台-查看卖出订单详情")
    public RestResult<SellOrderDetailsVo> getSellOrderDetails(@RequestBody @ApiParam @Valid PlatformOrderReq platformOrderReq) {
        //查看卖出订单详情
        return sellService.getSellOrderDetails(platformOrderReq);
    }

    @GetMapping("/fetchTransactionPageData")
    @ApiOperation(value = "前台-获取交易页面数据")
    public RestResult<FetchTransactionPageDataVo> fetchTransactionPageData() {
        return sellService.fetchTransactionPageData();
    }

    @PostMapping("/getCancelSellPageData")
    @ApiOperation(value = "前台-获取取消卖出页面数据")
    public RestResult<CancelSellPageDataVo> getCancelSellPageData(@RequestBody @ApiParam @Valid PlatformOrderReq platformOrderReq) {
        //获取取消卖出页面数据
        return sellService.getCancelSellPageData(platformOrderReq);
    }

    @PostMapping("/viewSellOrderAppealDetails")
    @ApiOperation(value = "前台-查看卖出订单申诉详情")
    public RestResult<AppealDetailsVo> viewSellOrderAppealDetails(@RequestBody @ApiParam @Valid PlatformOrderReq platformOrderReq) {
        //查看买入订单申诉详情
        return appealOrderService.viewAppealDetails(platformOrderReq, "2");
    }

    @PostMapping("/normalCollectionInfo")
    @ApiOperation(value = "前台-获取当前用户在正常收款的UPI收款信息")
    public RestResult<PageReturn<CollectionInfoVo>> currentNormalCollectionInfo(@RequestBody(required = false) @ApiParam @Valid PageRequestHome pageRequestHome) {
        return collectionInfoService.currentCollectionInfo(pageRequestHome);
    }

    @PostMapping("/createcollectionInfo")
    @ApiOperation(value = "前台-添加收款信息")
    @LogMemberOperation(value = MemberOperationModuleEnum.ADD_PAYMENT_INFO)
    public RestResult createcollectionInfo(@RequestBody @ApiParam @Valid FrontendCollectionInfoReq frontendCollectionInfoReq) {
        //添加收款信息处理
        return collectionInfoService.createcollectionInfoProcessing(frontendCollectionInfoReq);
    }


    @PostMapping("/setDefaultCollectionInfo")
    @ApiOperation(value = "前台-设置默认收款信息")
    @LogMemberOperation(value = MemberOperationModuleEnum.SET_DEFAULT_COLLECTION_INFO)
    public RestResult setDefaultCollectionInfo(@RequestBody @ApiParam @Valid CollectioninfoIdReq collectioninfoIdReq) {
        //设置默收款信息处理
        return collectionInfoService.setDefaultCollectionInfoReq(collectioninfoIdReq);
    }


    @PostMapping("/checkUpiIdDuplicate")
    @ApiOperation(value = "前台-校验UPI_ID是否重复")
    public RestResult<CheckUpiIdDuplicateVo> checkUpiIdDuplicate(@RequestBody @ApiParam @Valid CheckUpiIdDuplicateReq checkUpiIdDuplicateReq) {
        //校验UPI_ID是否重复
        return collectionInfoService.checkUpiIdDuplicate(checkUpiIdDuplicateReq);
    }
}
