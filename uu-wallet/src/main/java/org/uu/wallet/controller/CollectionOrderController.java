package org.uu.wallet.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.dto.CallBackDetailDTO;
import org.uu.common.pay.dto.CollectionOrderDTO;
import org.uu.common.pay.dto.CollectionOrderExportDTO;
import org.uu.common.pay.dto.CollectionOrderInfoDTO;
import org.uu.common.pay.req.CollectionOrderGetInfoReq;
import org.uu.common.pay.req.CollectionOrderIdReq;
import org.uu.common.pay.req.CollectionOrderListPageReq;
import org.uu.common.pay.req.PaidParamReq;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MatchingOrder;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.mapper.MatchingOrderMapper;
import org.uu.wallet.req.BuyOrderListReq;
import org.uu.wallet.service.ICollectionOrderService;
import org.uu.wallet.service.IKycCenterService;
import org.uu.wallet.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author Admin
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/collectionOrder", "/collectionOrder"})
@Api(description = "代收订单控制器")
@ApiIgnore
public class CollectionOrderController {

    private final ICollectionOrderService collectionOrderService;

    private final IKycCenterService kycCenterService;

    @PostMapping("/buyOrderList")
    @ApiOperation(value = "查询买入订单列表")
    public RestResult<PageReturn<BuyOrderListVo>> buyOrderList(@RequestBody(required = false) @ApiParam @Valid BuyOrderListReq buyOrderListReq) {
        return collectionOrderService.buyOrderList(buyOrderListReq);
    }

    @GetMapping("/viewBuyOrderDetails")
    @ApiOperation(value = "查看买入订单详情")
    public RestResult<ViewBuyOrderDetailsVo> viewOrderDetails(@ApiParam("订单号") @Valid String platformOrder) {
        ViewBuyOrderDetailsVo viewBuyOrderDetailsVo = collectionOrderService.viewBuyOrderDetails(platformOrder);
        return RestResult.ok(viewBuyOrderDetailsVo);
    }

    @PostMapping("/save")
    @ApiOperation(value = "保存代收订单")
    public RestResult<MerchantInfo> save(@RequestBody @ApiParam CollectionOrderVo collectionOrderVo) {
        CollectionOrder collectionOrder = new CollectionOrder();
        BeanUtils.copyProperties(collectionOrderVo, collectionOrder);
        collectionOrderService.save(collectionOrder);
        return RestResult.ok();
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新代收订单")
    public RestResult update(@RequestBody @ApiParam CollectionOrder collectionOrder) {
        boolean su = collectionOrderService.updateById(collectionOrder);
        return RestResult.ok();
    }

    @PostMapping("/listRecordPage")
    @ApiOperation(value = "查询代收订单列表")
    public RestResult<List<CollectionOrderDTO>> listRecordPage(@RequestBody(required = false) @ApiParam CollectionOrderListPageReq collectionOrderReq) {
        PageReturn<CollectionOrderDTO> collectionOrderPage = collectionOrderService.listRecordPage(collectionOrderReq);
        return RestResult.page(collectionOrderPage);
    }


    @PostMapping("/pay")
    @ApiOperation(value = "支付接口")
    public RestResult<CollectionOrderDTO> pay(@RequestBody(required = false) @ApiParam CollectionOrderIdReq req) {
        CollectionOrderDTO collectionOrderPage = collectionOrderService.pay(req);
        return RestResult.ok(collectionOrderPage);
    }



    @PostMapping("/listPage")
    @ApiOperation(value = "")
    public RestResult<List<CollectionOrderDTO>> listPage(@RequestBody(required = false) @ApiParam CollectionOrderListPageReq collectionOrderReq) {
        PageReturn<CollectionOrderDTO> collectionOrderPage = collectionOrderService.listPage(collectionOrderReq);
        return RestResult.page(collectionOrderPage);
    }

    @PostMapping("/listPageExport")
    @ApiOperation(value = "")
    public RestResult<List<CollectionOrderExportDTO>> listPageExport(@RequestBody(required = false) @ApiParam CollectionOrderListPageReq collectionOrderReq) {
        PageReturn<CollectionOrderExportDTO> collectionOrderPage = collectionOrderService.listPageExport(collectionOrderReq);
        return RestResult.page(collectionOrderPage);
    }

    @PostMapping("/manualCallback")
    @ApiOperation(value = "买入手动回调")
    public RestResult<Boolean> manualCallback(@RequestBody @ApiParam CollectionOrderIdReq collectionOrderReq) throws Exception {

        Boolean result = collectionOrderService.manualCallback(collectionOrderReq);
        return result ? RestResult.ok() : RestResult.failed(ResultCode.CALL_BACK_FAILED);
    }


    @PostMapping("/listPageRecordTotal")
    @ApiOperation(value = "查询代收订单列表")
    public RestResult<CollectionOrderDTO> listPageRecordTotal(@RequestBody(required = false) @ApiParam CollectionOrderListPageReq collectionOrderReq) {
        CollectionOrderDTO collectionOrderDTO = collectionOrderService.listPageRecordTotal(collectionOrderReq);
        return RestResult.ok(collectionOrderDTO);
    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "查询代收订单列表")
    public RestResult<CollectionOrderInfoDTO> getInfo(@RequestBody(required = false) @ApiParam CollectionOrderGetInfoReq req) {
        CollectionOrder collectionOrder = new CollectionOrder();
         collectionOrder.setId(req.getId());
        collectionOrder = collectionOrderService.getById(collectionOrder);
        CollectionOrderInfoDTO collectionOrderInfoDTO = new CollectionOrderInfoDTO();
        BeanUtils.copyProperties(collectionOrder,collectionOrderInfoDTO);
        if(collectionOrder.getOrderStatus().equals(OrderStatusEnum.WAS_CANCELED.getCode())){
            collectionOrderInfoDTO.setRemark(collectionOrder.getCancellationReason());
        }
        return RestResult.ok(collectionOrderInfoDTO);
    }


    @GetMapping("/collectionOrderInfo")
    @ApiOperation(value = "查询代收订单详情")
    public RestResult<CollectionOrderInfoVo> collectionOrderInfo(@ApiParam(name = "商户订单号") String merchantOrder) {
        return collectionOrderService.getCollectionOrderInfoByOrderNo(merchantOrder);
    }

    @GetMapping("/selectList")
    @ApiOperation(value = "查询币种,支付类型列表")
    public RestResult<selectListVo> selectList() {
        return collectionOrderService.selectList();
    }


    @PostMapping("/paid")
    @ApiOperation(value = "已支付")
    public KycRestResult paid(@RequestBody @ApiParam PaidParamReq req) {
        return kycCenterService.paid(req);
    }

    @PostMapping("/unPaid")
    @ApiOperation(value = "未支付")
    public KycRestResult unPaid(@RequestBody @ApiParam PaidParamReq req) {
        return kycCenterService.unPaid(req);
    }

    @PostMapping("/callBackDetail")
    @ApiOperation(value = "回调详情")
    public KycRestResult<CallBackDetailDTO> callBackDetail(@RequestBody @ApiParam CollectionOrderIdReq req) {
        CallBackDetailDTO  dto = collectionOrderService.callBackDetail(req);
        return KycRestResult.ok(dto);
    }
}
