package org.uu.manager.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.common.pay.dto.MarqueeListPageDTO;
import org.uu.common.pay.req.MarqueeReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MarqueeFeignClient;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * Announcement 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/marquee")
@Validated
@RequiredArgsConstructor
@Api(description = "跑马灯管理控制器")
public class MarqueeController {


    private final MarqueeFeignClient marqueeFeignClient;


    /**
     * 新增 跑马灯
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/addMarquee")
    @SysLog(title = "跑马灯管理控制器", content = "新增")
    @ApiOperation(value = "新增跑马灯")
    public RestResult addMarquee(@RequestBody @ApiParam @Valid MarqueeReq req) {
        return marqueeFeignClient.addMarquee(req);
    }

    /**
     * 根据ID查询跑马灯
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/getMarqueeById/{id}")
    @ApiOperation(value = "根据ID查询跑马灯")
    public RestResult<MarqueeListPageDTO> getMarqueeById(@PathVariable Long id) {
        return marqueeFeignClient.getMarqueeById(id);
    }

    /**
     * 更新 跑马灯
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateMarquee/{id}")
    @SysLog(title = "跑马灯管理控制器", content = "更新")
    @ApiOperation(value = "更新跑马灯")
    public RestResult updateMarquee(@PathVariable Long id, @RequestBody @ApiParam @Valid MarqueeReq req) {
        return marqueeFeignClient.updateMarquee(id, req);
    }

    /**
     * 删除 跑马灯
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @SysLog(title = "跑马灯管理控制器", content = "删除")
    @ApiOperation(value = "删除跑马灯")
    public RestResult deleteMarquee(@PathVariable Long id) {
        return marqueeFeignClient.deleteMarquee(id);
    }

    /**
     * 禁用、开启 跑马灯
     *
     * @param id
     * @return boolean
     */
    @PostMapping("/changeStatusMarquee/{id}/{status}")
    @SysLog(title = "跑马灯管理控制器", content = "禁用/开启")
    @ApiOperation(value = "禁用、开启跑马灯")
    public RestResult changeStatusMarquee(@PathVariable Long id, @PathVariable Integer status) {
        return marqueeFeignClient.changeStatusMarquee(id, status);
    }


    /**
     * 分页查询跑马灯
     */
    @PostMapping("/listMarquees")
    @ApiOperation(value = "分页获取 跑马灯列表 默认获取第一页 20条记录")
    public RestResult<List<MarqueeListPageDTO>> listMarquees(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return marqueeFeignClient.listHomeMarquees(pageRequest);
    }
}
