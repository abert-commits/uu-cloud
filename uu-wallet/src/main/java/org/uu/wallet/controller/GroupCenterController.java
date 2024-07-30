package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.vo.request.MyGroupRequestVO;
import org.uu.common.pay.vo.response.GroupCenterResponseVO;
import org.uu.common.pay.vo.response.MyGroupFilterBoxResponseVO;
import org.uu.common.pay.vo.response.MyGroupResponseVO;
import org.uu.wallet.service.GroupCenterService;
import java.util.List;

/**
 * <p>
 *  团队中心 前端控制器
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@RestController
@RequiredArgsConstructor
@Api(description = "团队中心控制器")
@RequestMapping(value = "/groupCenter")
public class GroupCenterController {
    private final GroupCenterService groupCenterService;

    @ApiOperation("团队中心-首页")
    @PostMapping(value = "/index")
    @ApiImplicitParam(name = "days", required = false, value = "天数(小于等于0查询全部, 默认值为-1)", defaultValue = "-1")
    public RestResult<GroupCenterResponseVO> index(@RequestParam(value = "days", defaultValue = "-1") Integer days) {
        return this.groupCenterService.index(days);
    }

    @ApiOperation("团队中心-我的团队")
    @PostMapping(value = "/myGroup")
    public RestResult<MyGroupResponseVO> myGroup(@RequestBody MyGroupRequestVO requestVO) {
        return this.groupCenterService.myGroup(requestVO);
    }

    @ApiOperation("团队中心-我的团队-Channel筛选框")
    @PostMapping(value = "filterBox")
    public RestResult<List<MyGroupFilterBoxResponseVO>> filterBox() {
        return this.groupCenterService.filterBox();
    }
}
