package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.entity.*;
import org.uu.wallet.req.*;
import org.uu.wallet.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

public interface IBuyService {

    /**
     * 获取买入金额列表
     *
     * @param buyListReq
     * @return {@link List}<{@link BuyListVo}>
     */
    PageReturn<BuyListVo> getBuyList(BuyListReq buyListReq);

    /**
     * 买入处理
     *
     * @param buyReq
     * @return {@link Boolean}
     */
    RestResult buyProcessor(BuyReq buyReq, HttpServletRequest request);

    /**
     * 买入订单校验
     *
     * @param buyReq
     * @param memberInfo
     * @param tradeConfig
     * @return {@link RestResult}
     */
    RestResult orderValidation(BuyReq buyReq, MemberInfo memberInfo, TradeConfig tradeConfig);

    /**
     * 生成买入订单
     *
     * @param buyReq
     * @param buyMemberInfo
     * @param buyplatformOrder
     * @param merchantPaymentOrder
     * @param realIP
     * @param kycPartners
     * @return {@link Boolean}
     */
    Boolean createBuyOrder(BuyReq buyReq, MemberInfo buyMemberInfo, String buyplatformOrder, MerchantPaymentOrders merchantPaymentOrder, String realIP, KycPartners kycPartners);

    /**
     * USDT买入处理
     *
     * @param usdtBuyReq
     * @return {@link RestResult}
     */
    RestResult usdtBuyProcessor(UsdtBuyReq usdtBuyReq);

    /**
     * USDT买入订单校验
     *
     * @param usdtBuyReq
     * @param usdtBuyMemberInfo
     * @param tradeConfig
     * @return {@link RestResult}
     */
    RestResult usdtOrderValidation(UsdtBuyReq usdtBuyReq, MemberInfo usdtBuyMemberInfo, TradeConfig tradeConfig);

    /**
     * 生成USDT买入订单
     *
     * @param usdtBuyReq
     * @param usdtBuyMemberInfo
     * @param tronAddress
     * @param platformOrder
     * @param calculatedArbAmount
     * @param tradeConfig
     * @return {@link Boolean}
     */
    Boolean createUsdtOrder(UsdtBuyReq usdtBuyReq, MemberInfo usdtBuyMemberInfo, TronAddress tronAddress, String platformOrder, BigDecimal calculatedArbAmount, TradeConfig tradeConfig, BigDecimal currencyExchangeRate);

    /**
     * 取消买入订单处理
     *
     * @param cancelOrderReq
     * @return {@link RestResult}
     */
    RestResult cancelPurchaseOrder(CancelOrderReq cancelOrderReq);

    /**
     * 完成支付 处理
     *
     * @param platformOrder
     * @param voucherImage
     * @return {@link RestResult}<{@link List}<{@link BuyListVo}>>
     */
    RestResult<List<BuyListVo>> buyCompletedProcessor(String platformOrder, String voucherImage);

    /**
     * 获取支付页面数据
     *
     * @return {@link RestResult}<{@link BuyVo}>
     */
    RestResult<BuyVo> getPaymentPageData();

    /**
     * 获取USDT支付页面数据
     *
     * @return {@link RestResult}<{@link UsdtBuyVo}>
     */
    RestResult<UsdtBuyVo> getUsdtPaymentPageData();

    /**
     * 获取支付类型
     *
     * @return {@link RestResult}<{@link List}<{@link PaymentTypeVo}>>
     */
    RestResult<List<PaymentTypeVo>> getPaymentType();
}
