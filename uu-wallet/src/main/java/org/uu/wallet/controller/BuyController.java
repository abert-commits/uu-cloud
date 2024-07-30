package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.web.exception.BizException;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.MemberAuthenticationStatusEnum;
import org.uu.wallet.Enum.MemberOperationModuleEnum;
import org.uu.wallet.annotation.LogMemberOperation;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.req.*;
import org.uu.wallet.service.*;
import org.uu.wallet.vo.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

import static org.uu.common.core.result.ResultCode.*;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/buyCenter")
@Api(description = "前台-买入控制器")
@Validated
@Slf4j
public class BuyController {

    private final IBuyService buyService;
    private final IUsdtBuyOrderService usdtBuyOrderService;
    private final ICollectionOrderService collectionOrderService;
    private final IAppealOrderService appealOrderService;

    @GetMapping("/getPaymentType")
    @ApiOperation(value = "前台-获取支付类型")
    public RestResult<List<PaymentTypeVo>> getPaymentType() {
        //获取支付类型
        return buyService.getPaymentType();
    }

    @PostMapping("/buyList")
    @ApiOperation(value = "前台-买入金额列表")
    public RestResult<PageReturn<BuyListVo>> buyList(@RequestBody(required = false) @ApiParam @Valid BuyListReq buyListReq) {
        //查询买入列表数据
        return RestResult.ok(buyService.getBuyList(buyListReq));
    }

    @PostMapping("/buy")
    @ApiOperation(value = "前台-买入下单接口")
    @LogMemberOperation(value = MemberOperationModuleEnum.BUY_ORDER)
    public RestResult buy(@RequestBody @ApiParam @Valid BuyReq buyReq, HttpServletRequest request) {
        //买入处理
        return buyService.buyProcessor(buyReq, request);
    }

    @GetMapping("/getPaymentPageData")
    @ApiOperation(value = "前台-获取支付页面数据")
    public RestResult<BuyVo> getPaymentPageData() {
        //获取支付页面数据
        return buyService.getPaymentPageData();
    }


    @GetMapping("/getUsdtPaymentPageData")
    @ApiOperation(value = "前台-获取USDT支付页面数据")
    public RestResult<UsdtBuyVo> getUsdtPaymentPageData() {
        //获取支付页面数据
        return buyService.getUsdtPaymentPageData();
    }

    @PostMapping("/getBuyOrderList")
    @ApiOperation(value = "前台-获取买入订单列表")
    public RestResult<PageReturn<BuyOrderListVo>> getBuyOrderList(@RequestBody(required = false) @ApiParam @Valid BuyOrderListReq buyOrderListReq) {
        //获取买入订单列表
        return collectionOrderService.buyOrderList(buyOrderListReq);
    }

    @PostMapping("/getBuyOrderDetails")
    @ApiOperation(value = "前台-获取买入订单详情")
    public RestResult<BuyOrderDetailsVo> getBuyOrderDetails(@RequestBody(required = false) @ApiParam @Valid PlatformOrderReq platformOrderReq) {
        //获取买入订单详情
        return collectionOrderService.getBuyOrderDetails(platformOrderReq);
    }


    @GetMapping("/getUsdtBuyPageData")
    @ApiOperation(value = "前台-获取USDT买入页面数据")
    public RestResult<UsdtBuyPageDataVo> getUsdtBuyPageData() {
        //获取USDT买入页面数据
        return usdtBuyOrderService.getUsdtBuyPageData();
    }

    @PostMapping("/usdtBuy")
    @ApiOperation(value = "前台-USDT买入下单接口")
    @LogMemberOperation(value = MemberOperationModuleEnum.USDT_BUY_ORDER)
    public RestResult usdtBuy(@RequestBody @ApiParam @Valid UsdtBuyReq usdtBuyReq) {
        //USDT买入处理
        return buyService.usdtBuyProcessor(usdtBuyReq);
    }

    @PostMapping("/buyCompleted")
    @ApiOperation(value = "前台-完成支付")
    @LogMemberOperation(value = MemberOperationModuleEnum.COMPLETE_PAYMENT)
    public RestResult buyCompleted(
            @NotBlank(message = "Order number cannot be empty")
            @Pattern(regexp = "^[A-Za-z0-9]{5}\\d{1,30}$", message = "Order number format is incorrect")
            @ApiParam(value = "订单号", required = true) @RequestParam("platformOrder") @Valid String platformOrder,

            @ApiParam(value = "凭证截图文件", required = true) @RequestParam("voucherImage") String voucherImage
    ) {
        //完成支付处理
        return buyService.buyCompletedProcessor(platformOrder, voucherImage);
    }

    @PostMapping("/getUsdtPurchaseRecords")
    @ApiOperation(value = "前台-获取USDT全部买入记录")
    public RestResult<PageReturn<UsdtBuyOrderVo>> getUsdtPurchaseRecords(@RequestBody(required = false) @ApiParam @Valid PageRequestHome pageRequestHome) {
        //查询所有USDT买入记录
        return usdtBuyOrderService.findAllUsdtPurchaseRecords(pageRequestHome);
    }

    @PostMapping("/getUsdtPurchaseOrderDetails")
    @ApiOperation(value = "前台-获取USDT买入订单详情")
    public RestResult<UsdtPurchaseOrderDetailsVo> getUsdtPurchaseOrderDetails(@RequestBody(required = false) @ApiParam @Valid PlatformOrderReq platformOrderReq) {
        //获取买入订单详情
        return usdtBuyOrderService.getUsdtPurchaseOrderDetails(platformOrderReq);
    }

//    @PostMapping("/usdtBuyCompleted")
//    @ApiOperation(value = "前台-USDT完成转账")
//    @LogMemberOperation(value = MemberOperationModuleEnum.USDT_COMPLETE_TRANSFER)
//    public RestResult usdtBuyCompleted(
//            @NotBlank(message = "Order number cannot be empty")
//            @Pattern(regexp = "^[A-Za-z0-9]{5}\\d{1,30}$", message = "Order number format is incorrect")
//            @ApiParam(value = "订单号", required = true) @RequestParam("platformOrder") @Valid String platformOrder,
//            @ApiParam(value = "凭证截图文件", required = true) @RequestParam("voucherImage") String voucherImage
//    ) {
//        //USDT完成转账处理
//        return usdtBuyOrderService.usdtBuyCompleted(platformOrder, voucherImage);
//    }


    @PostMapping("/cancelPurchaseOrder")
    @ApiOperation(value = "前台-取消买入订单")
    @LogMemberOperation(value = MemberOperationModuleEnum.CANCEL_BUY_ORDER)
    public RestResult cancelPurchaseOrder(@RequestBody @ApiParam @Valid CancelOrderReq cancelOrderReq) {
        return buyService.cancelPurchaseOrder(cancelOrderReq);
    }



    @PostMapping("/viewBuyOrderAppealDetails")
    @ApiOperation(value = "前台-查看买入订单申诉详情")
    public RestResult<AppealDetailsVo> viewBuyOrderAppealDetails(@RequestBody @ApiParam @Valid PlatformOrderReq platformOrderReq) {
        //查看买入订单申诉详情
        return appealOrderService.viewAppealDetails(platformOrderReq, "1");
    }

}
