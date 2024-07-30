package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtBuyOrderDTO;
import org.uu.common.pay.dto.UsdtBuyOrderExportDTO;
import org.uu.common.pay.dto.UsdtBuySuccessOrderDTO;
import org.uu.common.pay.req.UsdtBuyOrderIdReq;
import org.uu.common.pay.req.UsdtBuyOrderReq;
import org.uu.wallet.entity.UsdtBuyOrder;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.vo.UsdtBuyOrderVo;
import org.uu.wallet.vo.UsdtBuyPageDataVo;
import org.uu.wallet.vo.UsdtPurchaseOrderDetailsVo;

import java.util.List;

/**
 * @author
 */
public interface IUsdtBuyOrderService extends IService<UsdtBuyOrder> {

    /**
     * 根据会员id 查询usdt买入记录
     *
     * @param memberId
     * @return {@link PageReturn}<{@link UsdtBuyOrderVo}>
     */
    List<UsdtBuyOrderVo> findPagedUsdtPurchaseRecords(String memberId);

    /**
     * 查询全部USDT买入记录
     *
     * @param pageRequestHome
     * @return {@link List}<{@link UsdtBuyOrderVo}>
     */
    RestResult<PageReturn<UsdtBuyOrderVo>> findAllUsdtPurchaseRecords(PageRequestHome pageRequestHome);


    PageReturn<UsdtBuyOrderDTO> listPage(UsdtBuyOrderReq req);


    PageReturn<UsdtBuyOrderExportDTO> listpageForExport(UsdtBuyOrderReq req);

    /**
     * 根据订单号获取USDT买入订单
     *
     * @param platformOrder
     * @return {@link UsdtBuyOrder}
     */
    UsdtBuyOrder getUsdtBuyOrderByPlatformOrder(String platformOrder);

    /**
     * 获取USDT买入页面数据
     *
     * @return {@link RestResult}<{@link UsdtBuyPageDataVo}>
     */
    RestResult<UsdtBuyPageDataVo> getUsdtBuyPageData();

    /**
     * 根据会员id 查看进行中的USDT订单数量
     *
     * @param memberId
     */
    UsdtBuyOrder countActiveUsdtBuyOrders(String memberId);

    /**
     * 获取会员待支付的USDT买入订单
     *
     * @param memberId
     * @return {@link UsdtBuyOrder}
     */
    UsdtBuyOrder getPendingUsdtBuyOrder(Long memberId);


    /**
     * 获取USDT买入订单详情
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link UsdtPurchaseOrderDetailsVo}>
     */
    RestResult<UsdtPurchaseOrderDetailsVo> getUsdtPurchaseOrderDetails(PlatformOrderReq platformOrderReq);

    PageReturn<UsdtBuySuccessOrderDTO> successOrderListPage(UsdtBuyOrderReq req);


    RestResult<UsdtBuyOrderDTO> pay(UsdtBuyOrderIdReq req);
}
