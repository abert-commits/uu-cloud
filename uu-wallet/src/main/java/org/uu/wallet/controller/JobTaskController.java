package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.service.OrderChangeEventService;
import org.uu.wallet.util.RedisUtil;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.annotation.Resource;

/**
 * <p>
 * 定时任务执行 控制器
 * </p>
 *
 * @author
 * @since 2024-04-18
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/jobTask", "/jobTask"})
@Api(description = "定时任务执行控制器")
@ApiIgnore
public class JobTaskController {

    @Resource
    private OrderChangeEventService orderChangeEventService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private IMemberInfoService memberInfoService;




    @GetMapping("/processHistoryNewUserTask")
    @ApiOperation(value = "处理新人任务历史数据")
    public RestResult processHistoryNewUserTask(@RequestParam(value = "taskType") String taskType) {
        memberInfoService.processHistoryNewUserTask(taskType);
        return RestResult.ok();
    }

}
