package org.uu.wallet.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.common.pay.req.PaymentOrderIdReq;
import org.uu.common.pay.req.PaymentOrderListPageReq;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.req.SellOrderListReq;
import org.uu.wallet.vo.CollectionOrderInfoVo;
import org.uu.wallet.vo.SellOrderListVo;
import org.uu.wallet.vo.ViewSellOrderDetailsVo;

import java.util.List;

/**
 * @author
 */
public interface IPaymentOrderService extends IService<PaymentOrder> {

    PageReturn<PaymentOrderListPageDTO> listPage(PaymentOrderListPageReq req);
    PageReturn<PaymentOrderExportDTO> listPageExport(PaymentOrderListPageReq req);


    PageReturn<PaymentOrderListPageDTO> listRecordPage(PaymentOrderListPageReq req);

    PaymentOrderListPageDTO listRecordTotalPage(PaymentOrderListPageReq req);

    RestResult<CollectionOrderInfoVo> getPaymentOrderInfoByOrderNo(String merchantOrder);

    /**
     * 根据订单号获取订单信息
     *
     * @param orderNo
     * @return {@link PaymentOrder}
     */
    PaymentOrder getPaymentOrderByOrderNo(String orderNo);

    /**
     * 查询卖出订单列表
     *
     * @param req
     * @return {@link List}<{@link SellOrderListVo}>
     */
    List<SellOrderListVo> sellOrderList(SellOrderListReq req);

    /**
     * 查看卖出订单详情
     *
     * @param platformOrder
     * @return {@link ViewSellOrderDetailsVo}
     */
    ViewSellOrderDetailsVo viewSellOrderDetails(String platformOrder);


    /**
     * 获取卖出订单列表
     *
     * @param sellOrderListReq
     * @param memberInfo
     * @return {@link List}<{@link SellOrderListVo}>
     */
    List<SellOrderListVo> getPaymentOrderOrderList(SellOrderListReq sellOrderListReq, MemberInfo memberInfo);


    /**
     * 根据匹配订单号获取卖出订单列表
     *
     * @param matchOrder
     * @return {@link List}<{@link SellOrderListVo}>
     */
    List<SellOrderListVo> getPaymentOrderListByMatchOrder(String matchOrder);


    /**
     * 根据匹配订单号获取卖出订单列表
     *
     * @param matchOrder
     * @return {@link List}<{@link PaymentOrder}>
     */
    List<PaymentOrder> getPaymentOrderByByMatchOrder(String matchOrder);

    Boolean manualCallback(PaymentOrderIdReq req) throws Exception;


    /**
     * 查看该母订单是否已结束(查看母订单状态和该母订单下的子订单是否有未结束的订单)
     *
     * @param matchOrder
     * @return {@link Boolean}
     */
    Boolean existsActiveSubOrders(String matchOrder);

    /**
     * 获取usdt概览相关数据
     * @return
     */
    MemberOrderOverviewDTO getUsdtData(CommonDateLimitReq req);


    /**
     * 根据IP获取卖出订单
     *
     * @param ip
     * @return
     */
    List<PaymentOrder> getPaymentOrderByByIp(String ip);

    /**
     * 标记订单为指定的tag
     *
     * @param riskTag
     * @param platformOrders
     */
    void taggingOrders(String riskTag, List<String> platformOrders);

    /**
     * 获取详情信息
     * @param id
     * @return
     */
    RestResult<PaymentOrderInfoDTO> getInfoById(Long id);

    CallBackDetailDTO callBackDetail(PaymentOrderIdReq req);

}
