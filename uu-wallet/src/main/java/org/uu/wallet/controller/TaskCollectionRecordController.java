package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskCollectionRecordDTO;
import org.uu.common.pay.req.TaskCollectionRecordReq;
import org.uu.wallet.req.TaskCollectionRecordPageReq;
import org.uu.wallet.service.ITaskCollectionRecordService;
import org.uu.wallet.vo.TaskCollectionRecordListVo;
import org.uu.wallet.webSocket.SendRewardMember;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 会员领取任务记录 前端控制器
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/taskCollectionRecord", "/taskCollectionRecord"})
@Api(description = "会员领取任务记录控制器")
//@ApiIgnore
public class TaskCollectionRecordController {


    private final ITaskCollectionRecordService taskCollectionRecordService;
    private final SendRewardMember sendRewardMember;

    @PostMapping("/listPage")
    @ApiIgnore
    @ApiOperation(value = "查询会员领取任务记录列表")
    public PageReturn<TaskCollectionRecordDTO> listPage(@RequestBody @ApiParam TaskCollectionRecordReq req) {
        PageReturn<TaskCollectionRecordDTO> payConfigPage = taskCollectionRecordService.listPage(req);
        return payConfigPage;
    }

    @PostMapping("/getStatisticsData")
    @ApiIgnore
    @ApiOperation(value = "获取会员领取统计数据")
    public TaskCollectionRecordDTO getStatisticsData() {
        TaskCollectionRecordDTO payConfigPage = taskCollectionRecordService.getStatisticsData();
        return payConfigPage;
    }

    @PostMapping("/getPageList")
    @ApiOperation(value = "前台-奖励明细分页查询")
    public RestResult<PageReturn<TaskCollectionRecordListVo>> getPageList(@RequestBody(required = false) @ApiParam @Valid TaskCollectionRecordPageReq req) {
        return taskCollectionRecordService.getPageList(req);
    }

    @GetMapping("/sendRewardMember")
    @ApiOperation(value = "前台-发送获奖会员")
    public RestResult<Boolean> sendRewardMember() {
        return RestResult.ok(sendRewardMember.send());
    }

}
