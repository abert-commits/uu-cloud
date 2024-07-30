package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.wallet.rabbitmq.RabbitMQService;

import javax.annotation.Resource;

/**
 * <p>
 * 首页订单统计 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-07-13
 */
@RestController
@RequestMapping("/biOverViewStatistics")
public class BiOverViewStatisticsDailyController {

    @Resource
    RabbitMQService rabbitMQService;

    @PostMapping("/query")
    @ApiOperation(value = "")
    public void query() {
        OrderEventReq req = new OrderEventReq();
        req.setEventId("6");
        req.setParams("{\"commission\":\"2\",\"amount\":\"100\"}");
        rabbitMQService.sendStatisticProcess(req);
    }
}
