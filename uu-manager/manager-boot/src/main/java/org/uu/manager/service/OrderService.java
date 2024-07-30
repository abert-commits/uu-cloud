package org.uu.manager.service;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.OrderStatusOverviewListDTO;
import org.uu.common.pay.req.CommonDateLimitReq;

/**
 * @author admin
 * @date 2024/3/15 14:14
 */
public interface OrderService {
    RestResult<OrderStatusOverviewListDTO> getOrderStatusOverview(CommonDateLimitReq req);
}
