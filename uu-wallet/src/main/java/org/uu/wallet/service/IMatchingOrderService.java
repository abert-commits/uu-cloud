package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.wallet.entity.MatchingOrder;
import org.uu.wallet.entity.MemberInfo;

import java.util.List;
import java.util.Map;

/**
 * @author
 */
public interface IMatchingOrderService extends IService<MatchingOrder> {

    /**
     * 根据支付订单号获取匹配订单
     */
    MatchingOrder getMatchingOrderByCollection(String collectionOrder);


    MatchingOrderInfoDTO getInfo(MatchingOrderIdReq req);


    MatchingOrderDTO update(MatchingOrderReq req);


    MatchingOrderDTO getMatchingOrderTotal(MatchingOrderReq req);


    MatchingOrderDTO nopay(MatchingOrderAppealReq req);

    Map<String, String> getMatchMemberIdByPlatOrderIdList(List<String> platOrderIdList, boolean isBuy);

    /**
     * 根据买入订单、卖出订单批量汇总查询关联的撮合订单ID列表
     *
     * @param buyOrderIds
     * @param sellOrderIds
     * @return
     */
    Map<String, String> getMatchOrderIdsByPlatOrderId(List<String> buyOrderIds, List<String> sellOrderIds);

    /**
     * 标记订单为指定的tag
     *
     * @param riskTag
     * @param platformOrderTags
     */
    void taggingOrders(String riskTag, Map<String, String> platformOrderTags);

    Page<RelationOrderDTO> relationOrderList(RelationshipOrderReq req);

    boolean manualReview(MatchingOrderManualReq req, ISellService sellService);
}
