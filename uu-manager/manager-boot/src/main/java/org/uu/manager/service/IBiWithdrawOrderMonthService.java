package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiWithdrawOrderDailyExportDTO;
import org.uu.manager.entity.BiWithdrawOrderMonth;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.WithdrawDailyOrderReportReq;
import org.uu.manager.req.WithdrawMonthOrderReportReq;

import java.util.List;

/**
* @author 
*/
    public interface IBiWithdrawOrderMonthService extends IService<BiWithdrawOrderMonth> {

    PageReturn<BiWithdrawOrderMonth> listPage(WithdrawMonthOrderReportReq req);
    PageReturn<BiWithdrawOrderDailyExportDTO> listPageForExport(WithdrawMonthOrderReportReq req);
}
