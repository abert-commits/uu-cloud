package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.manager.entity.BiWithdrawOrderDaily;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.WithdrawDailyOrderReportReq;

import java.util.List;

/**
 * @author
 */
public interface IBiWithdrawOrderDailyService extends IService<BiWithdrawOrderDaily> {

    PageReturn<BiWithdrawOrderDaily> listPage(WithdrawDailyOrderReportReq req);


    MemberOrderOverviewDTO getMemberOrderOverview(CommonDateLimitReq req);

    PageReturn<BiWithdrawOrderDailyExportDTO> listPageForExport(WithdrawDailyOrderReportReq req);

    BiWithdrawOrderDaily getWithdrawOrderStatusOverview(CommonDateLimitReq req);
}
