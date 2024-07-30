package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiMerchantPayOrderExportDTO;
import org.uu.manager.entity.BiMerchantPayOrderMonth;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.MerchantMonthReportReq;

/**
* @author 
*/
    public interface IBiMerchantPayOrderMonthService extends IService<BiMerchantPayOrderMonth> {

    PageReturn<BiMerchantPayOrderMonth> listPage(MerchantMonthReportReq req);

    PageReturn<BiMerchantPayOrderExportDTO> listPageForExport(MerchantMonthReportReq req);
}
