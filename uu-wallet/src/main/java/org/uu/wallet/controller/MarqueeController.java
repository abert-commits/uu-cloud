package org.uu.wallet.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MarqueeListPageDTO;
import org.uu.common.pay.req.MarqueeReq;
import org.uu.wallet.service.IMarqueeService;
import org.uu.wallet.vo.MarqueeVo;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * @author
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "Marquee跑马灯控制器")
@RequestMapping(value = {"/marquee"})
@Validated
public class MarqueeController {

    private final IMarqueeService marqueeService;

    /**
     * 获取 跑马灯列表
     *
     * @return {@link RestResult}<{@link MarqueeVo}>
     */
    @GetMapping("/getMarqueeList")
    @ApiOperation(value = "前台-跑马灯列表")
    public RestResult<List<MarqueeVo>> getMarqueeList() {
        return marqueeService.getMarqueeList();
    }

    /**
     * 首页跑马灯分页查询
     */
    @ApiIgnore
    @PostMapping("/listHomeMarquees")
    public RestResult<List<MarqueeListPageDTO>> listHomeMarquees(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        PageReturn<MarqueeListPageDTO> marquees = marqueeService.listHomeMarquees(pageRequest);
        return RestResult.page(marquees);
    }


    /**
     * 启用、禁用 跑马灯
     *
     * @param id
     * @return {@link RestResult}
     */
    @ApiIgnore
    @PostMapping("/changeStatusMarquee/{id}/{status}")
    public RestResult changeStatusMarquee(@PathVariable Long id, @PathVariable Integer status) {
        return marqueeService.changeStatusMarquee(id, status) ? RestResult.ok() : RestResult.failed();
    }


    /**
     * 新增 跑马灯
     */
    @PostMapping("/addMarquee")
    @ApiIgnore
    public RestResult addMarquee(@RequestBody @ApiParam @Valid MarqueeReq req) {
        return marqueeService.addMarquee(req);
    }

    /**
     * 根据ID查询跑马灯信息
     */
    @GetMapping("/getMarqueeById/{id}")
    @ApiIgnore
    public RestResult<MarqueeListPageDTO> getMarqueeById(@PathVariable Long id) {
        return marqueeService.getMarqueeById(id);
    }

    /**
     * 更新 跑马灯
     */
    @PostMapping("/updateMarquee/{id}")
    @ApiIgnore
    public RestResult updateMarquee(@PathVariable Long id, @RequestBody @ApiParam @Valid MarqueeReq req) {
        return marqueeService.updateMarquee(id, req);
    }

    /**
     * 删除 跑马灯
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteMarquee(@PathVariable Long id) {
        return marqueeService.deleteMarquee(id) ? RestResult.ok() : RestResult.failed();
    }
}
