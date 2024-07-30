package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BiPaymentOrderDTO;
import org.uu.common.pay.dto.BiPaymentOrderExportDTO;
import org.uu.common.pay.dto.MemberOrderOverviewDTO;
import org.uu.common.pay.dto.OrderStatusOverviewDTO;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.common.pay.req.MemberInfoIdReq;
import org.uu.manager.entity.BiPaymentOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.PaymentOrderReportReq;

import java.util.List;

/**
 * @author
 */
public interface IBiPaymentOrderService extends IService<BiPaymentOrder> {

    /**
     * 查询代收日报表记录
     * @param req
     * @return
     */
    PageReturn<BiPaymentOrder> listPage(PaymentOrderReportReq req);


    RestResult<MemberOrderOverviewDTO> getMemberOrderOverview(CommonDateLimitReq req, RestResult<MemberOrderOverviewDTO> usdtData);
    PageReturn<BiPaymentOrderExportDTO> listPageForExport(PaymentOrderReportReq req);

    BiPaymentOrder getPaymentOrderStatusOverview(CommonDateLimitReq req);
}
