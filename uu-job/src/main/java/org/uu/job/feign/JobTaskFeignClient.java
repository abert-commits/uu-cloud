package org.uu.job.feign;

import io.swagger.annotations.ApiOperation;
import org.uu.common.core.result.RestResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "uu-wallet", contextId = "job-task")
public interface JobTaskFeignClient {


    /**
     * 同步会员进行中的订单缓存
     *
     * @return
     */
    @GetMapping("/api/v1/jobTask/syncMemberProcessingOrderCache")
    @ApiOperation(value = "同步会员进行中的订单缓存")
    RestResult syncMemberProcessingOrderCache();

    @GetMapping("/api/v1/jobTask/syncHistoryMatchSellOrder")
    @ApiOperation(value = "同步卖出匹配订单历史数据")
    RestResult syncHistoryMatchSellOrder();

    @GetMapping("/api/v1/jobTask/processHistoryNewUserTask")
    @ApiOperation(value = "处理新人任务历史数据")
    RestResult processHistoryNewUserTask(@RequestParam(value = "taskType") String taskType);
}
