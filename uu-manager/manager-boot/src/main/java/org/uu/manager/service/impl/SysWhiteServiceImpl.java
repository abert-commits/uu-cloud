package org.uu.manager.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.ControlSwitchDTO;
import org.uu.common.pay.req.ControlSwitchIdReq;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.api.ControlSwitchClient;
import org.uu.manager.entity.SysLog;
import org.uu.manager.entity.SysWhite;
import org.uu.manager.mapper.SysWhiteMapper;
import org.uu.manager.req.SysLogReq;
import org.uu.manager.req.SysWhiteReq;
import org.uu.manager.service.ISysWhiteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.uu.manager.util.PageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
* @author 
*/
    @Service
    @RequiredArgsConstructor
    public class SysWhiteServiceImpl extends ServiceImpl<SysWhiteMapper, SysWhite> implements ISysWhiteService {
        private final ControlSwitchClient controlSwitchClient;

    private final SysWhiteMapper sysWhiteMapper;
    @Override
    public PageReturn<SysWhite> listPage(SysWhiteReq req) {

        Page<SysWhite> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<SysWhite> lambdaQuery = lambdaQuery();


        if (!StringUtils.isEmpty(req.getIp())) {
            lambdaQuery.eq(SysWhite::getIp, req.getIp());
        }
        lambdaQuery.orderByDesc(SysWhite::getCreateTime);
        //lambdaQuery.ge(SysWhite::getCreateTime, req.getCreateTime());
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<SysWhite> records = page.getRecords();
        return PageUtils.flush(page, records);

    }

    @Override
    public boolean getIp(String addr, String clientCode) {
        // 判断是否开启白名单验证开关
        ControlSwitchIdReq req = new ControlSwitchIdReq();
        req.setSwitchId(7L);
        RestResult<ControlSwitchDTO> detail = controlSwitchClient.detail(req);
        if (ObjectUtils.isNotEmpty(detail)
                && ObjectUtils.isNotEmpty(detail.getData())
                && detail.getData().getStatus() == 0
                && ObjectUtils.isNotEmpty(clientCode)
                && clientCode.equals("1")
        ) {
            return true;
        }
        LambdaQueryChainWrapper<SysWhite> lambdaQueryChainWrapper = lambdaQuery().eq(SysWhite::getIp, addr).eq(SysWhite::getStatus, 1);
        if(!Objects.isNull(clientCode)){
            lambdaQueryChainWrapper.eq(SysWhite::getClientType, clientCode);
        }
        List<SysWhite> sysWhite = lambdaQueryChainWrapper.list();
        for (SysWhite item : sysWhite) {
            if (item != null && !StringUtils.isEmpty(item.getIp())){
                return true;
            }
        }

        return false;

    }

    @Override
    public boolean del(String id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public RestResult<?> saveDeduplication(SysWhiteReq req) {
        SysWhite sysWhite = new SysWhite();
        BeanUtils.copyProperties(req, sysWhite);
        // 查重
        String addr = sysWhite.getIp();
        String clientCode = sysWhite.getClientType();
        LambdaQueryChainWrapper<SysWhite> lambdaQuery = lambdaQuery().eq(SysWhite::getIp, addr);

        if(!Objects.isNull(clientCode)){
            lambdaQuery.eq(SysWhite::getClientType, clientCode);
        }

        List<SysWhite> ipList = lambdaQuery.list();

        if (!ipList.isEmpty()) {
            return RestResult.failed("重复添加！");
        }
        try{
            baseMapper.insert(sysWhite);
        }catch (RuntimeException e){
            return RestResult.failed("添加失败！");
        }
        return RestResult.ok(sysWhite);
    }


}
