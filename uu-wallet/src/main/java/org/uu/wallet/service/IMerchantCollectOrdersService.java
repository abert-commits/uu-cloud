package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtBuySuccessOrderDTO;
import org.uu.common.pay.req.UsdtBuyOrderReq;
import org.uu.wallet.dto.MerchantCollectionOrderStatusDTO;
import org.uu.wallet.entity.MerchantCollectOrders;
import org.uu.wallet.req.MerchantCollectionOrderStatusReq;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户代收订单表 服务类
 * </p>
 *
 * @author
 * @since 2024-01-05
 */
public interface IMerchantCollectOrdersService extends IService<MerchantCollectOrders> {


    /**
     * 根据商户订单号 获取订单信息
     *
     * @return {@link MerchantCollectOrders}
     */
    MerchantCollectOrders getOrderInfoByOrderNumber(String merchantOrder);


    /**
     * 取消充值订单
     *
     * @param platformOrder 平台订单号
     * @return {@link Boolean}
     */
    Boolean cancelPayment(String platformOrder);

    /**
     * 支付超时处理
     *
     * @param orderNo
     * @return boolean
     */
    boolean handlePaymentTimeout(String orderNo);

    /**
     * 根据U地址查询待支付的订单 USDT
     *
     * @param uAddress
     * @return {@link List }<{@link MerchantCollectOrders }>
     */
    List<MerchantCollectOrders> getPendingOrdersByUAddress(String uAddress);

    /**
     * 根据U地址查询待支付的订单 TRX
     *
     * @param uAddress
     * @return {@link List }<{@link MerchantCollectOrders }>
     */
    List<MerchantCollectOrders> getPendingOrdersByUAddressTRX(String uAddress);

    PageReturn<UsdtBuySuccessOrderDTO> merchantSuccessOrdersPage(UsdtBuyOrderReq req);

    /**
     * 商户代收订单状态
     *
     * @param requestVO 请求实体
     */
    RestResult<MerchantCollectionOrderStatusDTO> merchantCollectionOrderStatus(MerchantCollectionOrderStatusReq requestVO);

    Map<String, List<MerchantCollectOrders>> collectionMap(String merchantCode);
}
