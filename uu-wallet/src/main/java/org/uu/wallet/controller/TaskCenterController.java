package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.result.RestResult;
import org.uu.wallet.service.ITaskCenterService;
import org.uu.wallet.service.ITaskRulesContentService;
import org.uu.wallet.vo.TaskCenterVo;
import org.uu.wallet.vo.TaskRuleDetailsVo;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/taskCenter")
@Api(description = "前台-任务中心控制器")
@Validated
public class TaskCenterController {

    @Autowired
    private ITaskRulesContentService taskRulesContentService;


    @Autowired
    private ITaskCenterService taskCenterService;


    //获取任务中心页面数据
    @GetMapping("/fetchTaskCenterDetails")
    @ApiOperation(value = "前台-获取任务中心页面数据")
    public RestResult<TaskCenterVo> fetchTaskCenterDetails() {
        //获取任务规则详情
        return taskCenterService.fetchTaskCenterDetails();
    }


    //获取任务规则详情
    @GetMapping("/getTaskRuleDetails")
    @ApiOperation(value = "前台-获取任务规则详情")
    public RestResult<TaskRuleDetailsVo> getTaskRuleDetails() {
        //获取任务规则详情
        return taskRulesContentService.getTaskRuleDetails();
    }
}
