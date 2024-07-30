package org.uu.wallet.service;

import org.uu.common.core.result.RestResult;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.vo.*;

import javax.servlet.http.HttpServletRequest;

public interface ISellService {

    /**
     * 获取取消卖出页面数据
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link CancelSellPageDataVo}>
     */
    RestResult<CancelSellPageDataVo> getCancelSellPageData(PlatformOrderReq platformOrderReq);

    /**
     * 查看卖出订单详情
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link MatchPoolSplittingVo}>
     */
    RestResult<SellOrderDetailsVo> getSellOrderDetails(PlatformOrderReq platformOrderReq);

    /**
     * 委托卖出
     *
     * @param request
     * @return {@link RestResult }
     */
    RestResult delegateSell(HttpServletRequest request);

    /**
     * 获取交易页面数据
     *
     * @return {@link RestResult }<{@link FetchTransactionPageDataVo }>
     */
    RestResult<FetchTransactionPageDataVo> fetchTransactionPageData();

    /**
     * 卖出页API
     */
    RestResult<SellListVo> fetchPageData();
}
