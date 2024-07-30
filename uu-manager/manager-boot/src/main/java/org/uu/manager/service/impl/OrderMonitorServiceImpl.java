package org.uu.manager.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.common.pay.req.OrderMonitorReq;
import org.uu.manager.entity.MerchantInfo;
import org.uu.manager.entity.OrderMonitor;
import org.uu.manager.mapper.OrderMonitorMapper;
import org.uu.manager.service.IOrderMonitorService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2024-04-04
 */
@RequiredArgsConstructor
@Service
public class OrderMonitorServiceImpl extends ServiceImpl<OrderMonitorMapper, OrderMonitor> implements IOrderMonitorService {

    @Override
    public List<OrderMonitor> getOrderMonitorList(OrderMonitorReq req) {
        LambdaQueryChainWrapper<OrderMonitor> lambdaQuery = lambdaQuery();
        List<OrderMonitor> list = lambdaQuery().like(OrderMonitor::getStatisticalTime, req.getCreateTime()).eq(OrderMonitor::getCode,req.getCode()).list();
        return list;
    }
    @Override
    public List<OrderMonitor> getAllOrderMonitorListByDay(OrderMonitorReq req){
        LambdaQueryChainWrapper<OrderMonitor> lambdaQuery = lambdaQuery();
        List<OrderMonitor> list = lambdaQuery().like(OrderMonitor::getStatisticalTime, req.getCreateTime()).list();
        return list;
    }




}
