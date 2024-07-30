package org.uu.manager.service;

import org.uu.common.core.page.PageReturn;
import org.uu.manager.entity.SysLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.manager.req.RoleListPageReq;
import org.uu.manager.req.SysLogReq;
import org.uu.manager.vo.SysLogVo;
import org.uu.manager.vo.SysRoleVO;

/**
* @author 
*/
    public interface ISysLogService extends IService<SysLog> {

    PageReturn<SysLog> listPage(SysLogReq req);

    }
