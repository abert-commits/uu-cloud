package org.uu.wallet.strategy;

import org.uu.common.core.result.ApiResponse;
import org.uu.wallet.req.ApiRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 定义代收策略接口
 *
 * @author simon
 * @date 2024/07/15
 */
public interface CollectionStrategy {

    /**
     * 处理代收订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse }
     */
    ApiResponse processCollection(ApiRequest apiRequest, HttpServletRequest request);
}