package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiMerchantWithdrawOrderDailyDTO;
import org.uu.common.pay.dto.MerchantOrderOverviewDTO;
import org.uu.manager.entity.BiMerchantWithdrawOrderDaily;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.req.MerchantMonthReportReq;
import org.uu.manager.req.WithdrawDailyOrderReportReq;

/**
 * @author
 */
public interface IBiMerchantWithdrawOrderDailyService extends IService<BiMerchantWithdrawOrderDaily> {

    PageReturn<BiMerchantWithdrawOrderDaily> listPage(MerchantMonthReportReq req);

    MerchantOrderOverviewDTO getMerchantOrderOverview(MerchantDailyReportReq req);

    PageReturn<BiMerchantWithdrawOrderDailyDTO> listPageForExport(MerchantMonthReportReq req);
}
