package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiMerchantDailyDTO;
import org.uu.manager.entity.BiMerchantDaily;
import org.uu.manager.entity.BiMerchantMonth;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.MerchantMonthReportReq;

import java.util.List;

/**
 * @author
 */
public interface IBiMerchantMonthService extends IService<BiMerchantMonth> {

    PageReturn<BiMerchantMonth> listPage(MerchantMonthReportReq req);
    PageReturn<BiMerchantDailyDTO> listPageForExport(MerchantMonthReportReq req);
}
