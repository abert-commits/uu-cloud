package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BannerInfoDTO;
import org.uu.common.pay.dto.BannerInfoListPageDTO;
import org.uu.common.pay.req.BannerInfoReq;
import org.uu.common.pay.req.BannerPageReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.BannerInfoFeignClient;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * Banner信息表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-02-28
 */
@RestController
@RequestMapping("/banner")
@Api(description = "Banner管理控制器")
@Validated
@RequiredArgsConstructor
public class BannerInfoController {

    private final BannerInfoFeignClient bannerInfoFeignClient;

    /**
     * 新增 Banner
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/createBanner")
    @ApiOperation(value = "新增 Banner")
    @SysLog(title = "Banner管理控制器", content = "新增")
    public RestResult createBanner(@RequestBody @ApiParam @Valid BannerInfoReq req) {
        return bannerInfoFeignClient.createBanner(req);
    }

    /**
     * 根据ID查询Banner信息
     *
     * @param id
     * @return {@link RestResult}<{@link BannerInfoDTO}>
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询Banner信息")
    public RestResult<BannerInfoDTO> getBannerById(@PathVariable Long id) {
        return bannerInfoFeignClient.getBannerById(id);
    }

    /**
     * 更新 Banner
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateBanner/{id}")
    @ApiOperation(value = "更新 Banner信息")
    @SysLog(title = "Banner管理控制器", content = "更新")
    public RestResult updateBanner(@PathVariable Long id, @RequestBody @ApiParam @Valid BannerInfoReq req) {
        return bannerInfoFeignClient.updateBanner(id, req);
    }

    /**
     * 删除 Banner
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除 Banner")
    @SysLog(title = "Banner管理控制器", content = "删除")
    public RestResult deleteBanner(@PathVariable Long id) {
        return bannerInfoFeignClient.deleteBanner(id);
    }

    /**
     * 禁用 Banner
     *
     * @param id
     * @return boolean
     */
    @PostMapping("/disable/{id}")
    @ApiOperation(value = "禁用 Banner")
    @SysLog(title = "Banner管理控制器", content = "禁用")
    public RestResult disableBanner(@PathVariable Long id) {
        return bannerInfoFeignClient.disableBanner(id);
    }

    /**
     * 启用 Banner
     *
     * @param id
     * @return {@link RestResult}
     */
    @PostMapping("/enable/{id}")
    @ApiOperation(value = "启用 Banner")
    @SysLog(title = "Banner管理控制器", content = "启用")
    public RestResult enableBanner(@PathVariable Long id) {
        return bannerInfoFeignClient.enableBanner(id);
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link List}<{@link BannerInfoListPageDTO}>>
     */
    @PostMapping("/listBanners")
    @ApiOperation(value = "分页获取 Banner列表 默认获取第一页 20条记录")
    public RestResult<PageReturn<BannerInfoListPageDTO>> listBanners(@RequestBody(required = false) @ApiParam @Valid BannerPageReq pageRequest) {
        return bannerInfoFeignClient.listBanners(pageRequest);
    }
}
