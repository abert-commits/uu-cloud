package org.uu.manager.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.OrderStatusOverviewListDTO;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.manager.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin
 * @date 2024/3/15 14:12
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/orderInfoAdmin", "/orderInfoAdmin"})
@Api(description = "订单控制器")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/getOrderStatusOverview")
    @ApiOperation(value = "订单状态统计")
    public RestResult<OrderStatusOverviewListDTO> getOrderStatusOverview(@RequestBody CommonDateLimitReq req){
        return orderService.getOrderStatusOverview(req);
    }
}
