package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiMemberReconciliationDTO;
import org.uu.manager.entity.BiMemberReconciliation;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.entity.BiMerchantReconciliation;
import org.uu.manager.req.MerchantDailyReportReq;

import java.util.concurrent.ExecutionException;

/**
 * <p>
 * 会员对账报表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-06
 */
public interface IBiMemberReconciliationService extends IService<BiMemberReconciliation> {
    PageReturn<BiMemberReconciliationDTO> listPage(MerchantDailyReportReq req);
}
