package org.uu.manager.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskCollectionRecordDTO;
import org.uu.common.pay.req.TaskCollectionRecordReq;
import org.uu.manager.api.TaskCollectionRecordClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

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
public class TaskCollectionRecordController {

    private final TaskCollectionRecordClient taskManagerClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "查询会员领取任务记录列表")
    public RestResult<List<TaskCollectionRecordDTO>> listPage(@RequestBody @ApiParam TaskCollectionRecordReq req) {
        PageReturn<TaskCollectionRecordDTO> payConfigPage = taskManagerClient.listPage(req);
        return RestResult.page(payConfigPage);
    }

    @PostMapping("/getStatisticsData")
    @ApiOperation(value = "获取会员领取统计数据")
    public RestResult<TaskCollectionRecordDTO> getStatisticsData() {
        TaskCollectionRecordDTO recordDTO = taskManagerClient.getStatisticsData();
        return RestResult.ok(recordDTO);
    }

}
