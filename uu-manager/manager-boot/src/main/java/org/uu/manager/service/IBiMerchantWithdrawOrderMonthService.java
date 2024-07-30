package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiMerchantWithdrawOrderDailyDTO;
import org.uu.manager.entity.BiMerchantWithdrawOrderMonth;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.MerchantMonthReportReq;

/**
 * @author
 */
public interface IBiMerchantWithdrawOrderMonthService extends IService<BiMerchantWithdrawOrderMonth> {

    PageReturn<BiMerchantWithdrawOrderMonth> listPage(MerchantMonthReportReq req);

    PageReturn<BiMerchantWithdrawOrderDailyDTO> listPageForExport(MerchantMonthReportReq req);
}
