package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BiMerchantDailyDTO;
import org.uu.manager.entity.BiMerchantDaily;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.MerchantDailyReportReq;

import java.util.List;

/**
 * @author
 */
public interface IBiMerchantDailyService extends IService<BiMerchantDaily> {

    PageReturn<BiMerchantDaily> listPage(MerchantDailyReportReq req);

    PageReturn<BiMerchantDailyDTO> listPageForExport(MerchantDailyReportReq req);
}
