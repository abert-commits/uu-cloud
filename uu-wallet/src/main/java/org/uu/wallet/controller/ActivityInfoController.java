package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.wallet.service.IActivityInfoService;
import org.uu.wallet.service.IAnnouncementService;
import org.uu.wallet.vo.ActivityInfoVo;
import org.uu.wallet.vo.AnnouncementVo;

import javax.validation.Valid;

/**
 * <p>
 * activity_info 前端控制器
 * </p>
 *
 * @author
 * @since 2024-02-29
 */
@RestController
@RequestMapping("/activity")
@Validated
@RequiredArgsConstructor
//@Api(description = "前台-活动管理控制器")
public class ActivityInfoController {

    private final IActivityInfoService activityInfoService;

    /**
     * 前台 获取 活动列表 分页
     *
     * @return {@link RestResult}<{@link ActivityInfoVo}>
     */
    @PostMapping("/getAnnouncementList")
    @ApiOperation(value = "前台-获取活动列表")
    public RestResult<PageReturn<ActivityInfoVo>> getActivityInfoList(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return activityInfoService.getActivityInfoList(pageRequest);
    }

    /**
     * 前台 根据ID查询活动信息
     *
     * @param id 活动ID
     * @return {@link RestResult}<{@link ActivityInfoVo}>
     */
    @GetMapping("/detail")
    @ApiOperation(value = "前台-获取活动详情")
    public RestResult<ActivityInfoVo> findActivityInfoDetail(@RequestParam Long id) {
        return activityInfoService.findActivityInfoDetail(id);
    }

}
