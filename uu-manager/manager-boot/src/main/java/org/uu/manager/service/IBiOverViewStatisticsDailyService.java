package org.uu.manager.service;

import org.uu.common.pay.dto.BiOverViewStatisticsDailyDTO;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.manager.entity.BiOverViewStatisticsDaily;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 首页订单统计 服务类
 * </p>
 *
 * @author 
 * @since 2024-07-13
 */
public interface IBiOverViewStatisticsDailyService extends IService<BiOverViewStatisticsDaily> {

    boolean statisticsDaily(OrderEventReq req);

    /**
     * 商户日报表统计
     * @param req
     * @return
     */
    boolean statisticsMerchantDaily(OrderEventReq req);

    BiOverViewStatisticsDailyDTO getDataByDate(String start, String end);

}
