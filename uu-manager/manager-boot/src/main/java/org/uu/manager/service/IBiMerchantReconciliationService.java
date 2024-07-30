package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.BiMerchantReconciliationDTO;
import org.uu.manager.entity.BiMemberReconciliation;
import org.uu.manager.entity.BiMerchantReconciliation;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.MerchantDailyReportReq;

/**
 * <p>
 * 商户对账报表 服务类
 * </p>
 *
 * @author
 * @since 2024-03-06
 */
public interface IBiMerchantReconciliationService extends IService<BiMerchantReconciliation> {

    PageReturn<BiMerchantReconciliationDTO> listPage(MerchantDailyReportReq req);
}
