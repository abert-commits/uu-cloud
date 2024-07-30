package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiPaymentOrderExportDTO;
import org.uu.manager.entity.BiPaymentOrderMonth;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.PaymentMonthOrderReportReq;

import java.util.List;

/**
 * @author
 */
public interface IBiPaymentOrderMonthService extends IService<BiPaymentOrderMonth> {

    PageReturn<BiPaymentOrderMonth> listPage(PaymentMonthOrderReportReq req);
    PageReturn<BiPaymentOrderExportDTO> listPageForExport(PaymentMonthOrderReportReq req);
}
