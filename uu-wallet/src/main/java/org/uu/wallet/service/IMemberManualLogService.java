package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.MemberManualLogDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.pay.req.MemberManualLogsReq;

/**
 * <p>
 * 会员手动操作记录 服务类
 * </p>
 *
 * @author 
 * @since 2024-02-29
 */
public interface IMemberManualLogService extends IService<MemberManualLogDTO> {

    PageReturn<MemberManualLogDTO> listPage(MemberManualLogsReq req);
}
