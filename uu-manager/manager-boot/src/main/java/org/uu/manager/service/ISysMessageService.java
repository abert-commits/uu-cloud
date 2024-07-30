package org.uu.manager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.manager.entity.SysMessage;
import org.uu.manager.req.SysMessageIdReq;
import org.uu.manager.req.SysMessageReq;
import org.uu.manager.req.SysMessageSendReq;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author admin
 * @since 2024-05-06
 */
public interface ISysMessageService extends IService<SysMessage> {
    PageReturn<SysMessage> listPage(SysMessageReq req);

    RestResult deleted(SysMessageIdReq req);

    RestResult read(SysMessageIdReq req);

    RestResult sendMessage(SysMessageSendReq sysMessage);

    Integer unReadMessageCount(String userId);

}
