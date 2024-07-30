package org.uu.wallet.service;

import org.uu.common.core.dto.MemberLoginLogMessage;
import org.uu.wallet.entity.MemberOperationLogMessage;

public interface ProcessMemberLogService {

    /**
     * 处理会员登录日志记录
     *
     * @param memberLoginLogMessage
     * @return {@link Boolean}
     */
    Boolean processMemberLoginLog(MemberLoginLogMessage memberLoginLogMessage);


    /**
     * 处理会员操作日志记录
     *
     * @param memberOperationLogMessage
     * @return {@link Boolean}
     */
    Boolean processMemberOperationLog(MemberOperationLogMessage memberOperationLogMessage);
}
