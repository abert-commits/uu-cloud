package org.uu.wallet.controller;


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
import org.uu.wallet.service.IBannerInfoService;
import org.uu.wallet.vo.BannerListVo;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Banner信息表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-02-28
 */
@RestController
@RequestMapping(value = {"/api/v1/banner", "/banner"})
@Validated
@RequiredArgsConstructor
@Api(description = "Banner管理控制器")
public class BannerInfoController {

    private final IBannerInfoService bannerInfoService;


    /**
     * 获取 Banner列表
     *
     * @return {@link RestResult}<{@link BannerListVo}>
     */
    @GetMapping("/getBannerList")
    @ApiOperation(value = "前台-获取Banner列表")
    public RestResult<Map<String, List<BannerListVo>>> getBannerList() {
        return bannerInfoService.getBannerList();
    }

    /**
     * 新增 Banner
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/createBanner")
    @ApiIgnore
    public RestResult createBanner(@RequestBody @ApiParam @Valid BannerInfoReq req) {
        return bannerInfoService.createBanner(req);
    }

    /**
     * 根据ID查询Banner信息
     *
     * @param id
     * @return {@link RestResult}<{@link BannerInfoDTO}>
     */
    @GetMapping("/{id}")
    @ApiIgnore
    public RestResult<BannerInfoDTO> getBannerById(@PathVariable Long id) {
        return bannerInfoService.getBannerById(id);
    }

    /**
     * 更新 Banner
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateBanner/{id}")
    @ApiIgnore
    public RestResult updateBanner(@PathVariable Long id, @RequestBody @ApiParam @Valid BannerInfoReq req) {
        return bannerInfoService.updateBanner(id, req);
    }

    /**
     * 删除 Banner
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteBanner(@PathVariable Long id) {
        return bannerInfoService.deleteBanner(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 禁用 Banner
     *
     * @param id
     * @return boolean
     */
    @ApiIgnore
    @PostMapping("/disable/{id}")
    public RestResult disableBanner(@PathVariable Long id) {
        return bannerInfoService.disableBanner(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 启用 Banner
     *
     * @param id
     * @return {@link RestResult}
     */
    @ApiIgnore
    @PostMapping("/enable/{id}")
    public RestResult enableBanner(@PathVariable Long id) {
        return bannerInfoService.enableBanner(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link List}<{@link BannerInfoListPageDTO}>>
     */
    @ApiIgnore
    @PostMapping("/listBanners")
    public RestResult<PageReturn<BannerInfoListPageDTO>> listBanners(@RequestBody(required = false) @ApiParam @Valid BannerPageReq pageRequest) {
        return bannerInfoService.listPage(pageRequest);
    }
}
