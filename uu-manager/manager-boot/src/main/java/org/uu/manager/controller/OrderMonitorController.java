package org.uu.manager.controller;


import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.OrderStatusOverviewListDTO;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.common.pay.req.OrderMonitorReq;
import org.uu.manager.entity.BiMerchantDaily;
import org.uu.manager.entity.OrderMonitor;
import org.uu.manager.service.IOrderMonitorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 2024-04-04
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/orderMonitor", "/orderMonitor"})
@Api(description = "订单监控控制器")
public class OrderMonitorController {
    private final IOrderMonitorService  orderMonitorService;

    @PostMapping("/getOrderMonitorList")
    @ApiOperation(value = "获取订单监控")
    public RestResult<List<OrderMonitor>> getOrderMonitorList(@RequestBody OrderMonitorReq req){
        List<OrderMonitor> list = orderMonitorService.getOrderMonitorList(req);
        return RestResult.ok(list);
    }




    @PostMapping("/getAllOrderMonitorListByDay")
    @ApiOperation(value = "获取订单监控每天全部")
    public RestResult<Map<String,List<OrderMonitor>>> getAllOrderMonitorListByDay(@RequestBody @ApiParam OrderMonitorReq req){
        List<OrderMonitor> list = orderMonitorService.getAllOrderMonitorListByDay(req);
        Map<String, List<OrderMonitor>> map = list.stream()
                .collect(Collectors.groupingBy(OrderMonitor::getCode));
        return RestResult.ok(map);
    }

    @PostMapping("/clean")
    @ApiOperation(value = "清理15天以后数据")
    public RestResult clean(@RequestBody @ApiParam OrderMonitorReq req){
        LambdaQueryChainWrapper<OrderMonitor> lambdaQuery = orderMonitorService.lambdaQuery();
        lambdaQuery.le(OrderMonitor::getCreateTime,LocalDateTime.now().minusDays(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Boolean r = orderMonitorService.remove(lambdaQuery.getWrapper());
        return RestResult.ok();
    }

}
