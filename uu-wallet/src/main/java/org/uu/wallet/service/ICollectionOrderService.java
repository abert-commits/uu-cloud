package org.uu.wallet.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CallBackDetailDTO;
import org.uu.common.pay.dto.CollectionOrderDTO;
import org.uu.common.pay.dto.CollectionOrderExportDTO;
import org.uu.common.pay.req.CollectionOrderIdReq;
import org.uu.common.pay.req.CollectionOrderListPageReq;
import org.uu.common.pay.vo.response.MemberAccountChangeDetailResponseVO;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.req.BuyOrderListReq;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.vo.BuyOrderDetailsVo;
import org.uu.wallet.vo.BuyOrderListVo;
import org.uu.wallet.vo.CollectionOrderInfoVo;
import org.uu.wallet.vo.ViewBuyOrderDetailsVo;

import java.util.List;
import java.util.Map;

/**
 * @author
 */
public interface ICollectionOrderService extends IService<CollectionOrder> {


    /**
     * 查询买入订单列表
     *
     * @param req
     * @return {@link RestResult}<{@link List}<{@link BuyOrderListVo}>>
     */
    RestResult<PageReturn<BuyOrderListVo>> buyOrderList(BuyOrderListReq req);

    /**
     * 查看买入订单详情
     *
     * @param platformOrder
     * @return {@link ViewBuyOrderDetailsVo}
     */
    ViewBuyOrderDetailsVo viewBuyOrderDetails(String platformOrder);

    PageReturn<CollectionOrderDTO> listRecordPage(CollectionOrderListPageReq req);

    PageReturn<CollectionOrderDTO> listPage(CollectionOrderListPageReq req);

    PageReturn<CollectionOrderExportDTO> listPageExport(CollectionOrderListPageReq req);

    CollectionOrderDTO listPageRecordTotal(CollectionOrderListPageReq req);

    CollectionOrderDTO pay(CollectionOrderIdReq req);

    Boolean manualCallback(CollectionOrderIdReq req) throws Exception;

    /*
     * 查询代收订单详情
     * */
    RestResult<CollectionOrderInfoVo> getCollectionOrderInfoByOrderNo(String merchantOrder);

    /*
     * 查询下拉列表数据(币种,支付类型)
     * */
    RestResult selectList();

    /*
     * 查询最接近给定数字的前10个元素
     * p1 代付池金额列表
     * p2 充值金额
     * p3 列表推荐个数
     * */
    List<Map.Entry<String, Integer>> findClosestValues(Map<String, Integer> map, int collectionAmount, int count);

    /**
     * 根据订单号获取买入订单
     *
     * @param platformOrder
     * @return {@link CollectionOrder}
     */
    CollectionOrder getCollectionOrderByPlatformOrder(String platformOrder);


    /**
     * 根据会员id 查看进行中的订单数量
     *
     * @param memberId
     */
    CollectionOrder countActiveBuyOrders(String memberId);

    /**
     * 根据会员id 获取待支付和支付超时的买入订单
     *
     * @param memberId
     * @return {@link CollectionOrder}
     */
    CollectionOrder getPendingBuyOrder(String memberId);

    /**
     * 获取买入订单详情
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link BuyOrderDetailsVo}>
     */
    RestResult<BuyOrderDetailsVo> getBuyOrderDetails(PlatformOrderReq platformOrderReq);

    /**
     * 根据IP获取买入订单
     *
     * @param ip
     * @return
     */
    List<CollectionOrder> getCollectOrderByByIp(String ip);

    /**
     * 标记订单为指定的tag
     *
     * @param riskTag
     * @param platformOrders
     */
    void taggingOrders(String riskTag, List<String> platformOrders);


    CallBackDetailDTO callBackDetail(CollectionOrderIdReq req);

    /**
     * 根据订单号查询买入订单
     * @param orderNo 订单号
     */
    RestResult<MemberAccountChangeDetailResponseVO> selectByOrderNo(String orderNo);
}
