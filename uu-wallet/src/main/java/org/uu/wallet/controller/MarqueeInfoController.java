package org.uu.wallet.controller;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import lombok.RequiredArgsConstructor;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.uu.common.core.result.RestResult;
//import org.uu.wallet.vo.MarqueeVo;
//
//import java.util.List;
//
///**
// * <p>
// * 跑马灯 前端控制器
// * </p>
// *
// * @author
// * @since
// */
//@RestController
//@RequestMapping(value = {"/api/v1/marquee", "/marquee"})
//@Validated
//@RequiredArgsConstructor
//@Api(description = "跑马灯管理控制器")
public class MarqueeInfoController {
//
//    private final IMarqueeInfoService marqueeInfoService;
//
//    /**
//     * 获取跑马灯列表
//     *
//     * @return {@link RestResult}<{@link List}<{@link MarqueeVo}>>
//     */
//    @GetMapping("/getMarqueeList")
//    @ApiOperation(value = "前台-获取Banner列表")
//    public RestResult<List<MarqueeVo>> getMarqueeList() {
//        return marqueeInfoService.getMarqueeList();
//    }
//
//    /**
//     * 获取跑马灯详情
//     *
//     * @param id 跑马灯ID
//     * @return {@link RestResult}<{@link MarqueeVo}>
//     */
//    @GetMapping("/{id}")
//    @ApiOperation(value = "获取跑马灯详情")
//    public RestResult<MarqueeVo> getMarquee(@PathVariable Long id) {
//        return marqueeInfoService.getMarqueeById(id);
//    }
//
//    /**
//     * 创建新的跑马灯
//     *
//     * @param marqueeVo 跑马灯信息
//     * @return {@link RestResult}<{@link MarqueeVo}>
//     */
//    @PostMapping("/create")
//    @ApiOperation(value = "创建新的跑马灯")
//    public RestResult<MarqueeVo> createMarquee(@RequestBody MarqueeVo marqueeVo) {
//        return marqueeInfoService.createMarquee(marqueeVo);
//    }
//
//    /**
//     * 更新跑马灯
//     *
//     * @param id        跑马灯ID
//     * @param marqueeVo 跑马灯信息
//     * @return {@link RestResult}<{@link MarqueeVo}>
//     */
//    @PostMapping("/update/{id}")
//    @ApiOperation(value = "更新跑马灯")
//    public RestResult<MarqueeVo> updateMarquee(@PathVariable Long id, @RequestBody MarqueeVo marqueeVo) {
//        return marqueeInfoService.updateMarquee(id, marqueeVo);
//    }
//
//    /**
//     * 删除跑马灯
//     *
//     * @param id 跑马灯ID
//     * @return {@link RestResult}<{@link Boolean}>
//     */
//    @PostMapping("/delete/{id}")
//    @ApiOperation(value = "删除跑马灯")
//    public RestResult<Boolean> deleteMarquee(@PathVariable Long id) {
//        return marqueeInfoService.deleteMarquee(id);
//    }
}
