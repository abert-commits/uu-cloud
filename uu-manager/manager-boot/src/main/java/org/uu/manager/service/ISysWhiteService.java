package org.uu.manager.service;


import org.uu.common.core.page.PageReturn;

import org.uu.common.core.result.RestResult;
import org.uu.manager.entity.SysWhite;
import com.baomidou.mybatisplus.extension.service.IService;

import org.uu.manager.req.SysWhiteReq;


/**
* @author 
*/
    public interface ISysWhiteService extends IService<SysWhite> {


     PageReturn<SysWhite> listPage(SysWhiteReq req);

    boolean getIp(String addr, String clientCode);

    boolean del(String id);

    /**
     * 添加ip白名单
     * @param req req
     * @return RestResult
     */
    RestResult<?> saveDeduplication(SysWhiteReq req);
}
