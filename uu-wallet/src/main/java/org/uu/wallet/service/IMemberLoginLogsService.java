package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.MemberLoginLogsDTO;
import org.uu.common.pay.req.MemberLoginLogsReq;
import org.uu.wallet.entity.MemberLoginLogs;

/**
 * <p>
 * 会员登录日志表 服务类
 * </p>
 *
 * @author 
 * @since 2024-01-13
 */
public interface IMemberLoginLogsService extends IService<MemberLoginLogs> {

    PageReturn<MemberLoginLogsDTO> listPage(MemberLoginLogsReq req);
}
