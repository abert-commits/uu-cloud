package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.MemberOperationLogsDTO;
import org.uu.common.pay.req.MemberOperationLogsReq;
import org.uu.wallet.entity.MemberOperationLogs;

/**
 * <p>
 * 会员操作日志表 服务类
 * </p>
 *
 * @author 
 * @since 2024-01-13
 */
public interface IMemberOperationLogsService extends IService<MemberOperationLogs> {

    PageReturn<MemberOperationLogsDTO> listPage(MemberOperationLogsReq memberOperationLogsReq);
}
