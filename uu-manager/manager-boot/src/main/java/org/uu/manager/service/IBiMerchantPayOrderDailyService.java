package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiMerchantPayOrderExportDTO;
import org.uu.common.pay.dto.MerchantOrderOverviewDTO;
import org.uu.manager.entity.BiMerchantPayOrderDaily;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.req.MerchantMonthReportReq;

/**
 * @author
 */
public interface IBiMerchantPayOrderDailyService extends IService<BiMerchantPayOrderDaily> {
    PageReturn<BiMerchantPayOrderDaily> listPage(MerchantMonthReportReq req);

    PageReturn<BiMerchantPayOrderExportDTO> listPageForExport(MerchantMonthReportReq req);

    MerchantOrderOverviewDTO getMerchantOrderOverview(MerchantDailyReportReq req);
}
