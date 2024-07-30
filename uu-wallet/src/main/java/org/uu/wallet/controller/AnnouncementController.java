package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.common.pay.dto.AnnouncementLinkDTO;
import org.uu.common.pay.dto.AnnouncementListPageDTO;
import org.uu.common.pay.req.AnnouncementInfoReq;
import org.uu.wallet.service.IAnnouncementService;
import org.uu.wallet.vo.AnnouncementVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * Announcement 前端控制器
 * </p>
 *
 * @author
 * @since 2024-02-29
 */
@RestController
@RequestMapping(value = {"/api/v1/announcement", "/announcement"})
@Validated
@RequiredArgsConstructor
@Api(description = "前台-活动管理控制器")
public class AnnouncementController {


    private final IAnnouncementService announcementService;



    /**
     * 获取活动链接
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/fetchAnnouncementLinkById/{id}")
    @ApiIgnore
    public RestResult<AnnouncementLinkDTO> fetchAnnouncementLinkById(@PathVariable Long id) {
        return announcementService.fetchAnnouncementLinkById(id);
    }


    /**
     * 前台 获取 活动列表 分页
     *
     * @return {@link RestResult}<{@link AnnouncementVo}>
     */
    @PostMapping("/getAnnouncementList")
    @ApiOperation(value = "前台-获取活动列表")
    public RestResult<PageReturn<AnnouncementVo>> getAnnouncementList(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return announcementService.getAnnouncementList(pageRequest);
    }

    /**
     * 前台 根据ID查询活动信息
     *
     * @param id 活动ID
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/detail")
    @ApiOperation(value = "前台-获取活动详情")
    public RestResult<AnnouncementVo> findAnnouncementDetail(@RequestParam Long id) {
        return announcementService.findAnnouncementDetail(id);
    }

    /**
     * 新增 活动
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/createAnnouncement")
    @ApiIgnore
    public RestResult createAnnouncement(@RequestBody @ApiParam @Valid AnnouncementInfoReq req) {
        return announcementService.createAnnouncement(req);
    }

    /**
     * 根据ID查询活动信息
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/getAnnouncementById/{id}")
    @ApiIgnore
    public RestResult<AnnouncementDTO> getAnnouncementById(@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }

    /**
     * 更新 活动
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateAnnouncement/{id}")
    @ApiIgnore
    public RestResult updateAnnouncement(@PathVariable Long id, @RequestBody @ApiParam @Valid AnnouncementInfoReq req) {
        return announcementService.updateAnnouncement(id, req);
    }

    /**
     * 删除 活动
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @ApiIgnore
    public RestResult deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 禁用 活动
     *
     * @param id
     * @return boolean
     */
    @ApiIgnore
    @PostMapping("/disable/{id}")
    public RestResult disableAnnouncement(@PathVariable Long id) {
        return announcementService.disableAnnouncement(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 启用 活动
     *
     * @param id
     * @return {@link RestResult}
     */
    @ApiIgnore
    @PostMapping("/enable/{id}")
    public RestResult enableAnnouncement(@PathVariable Long id) {
        return announcementService.enableAnnouncement(id) ? RestResult.ok() : RestResult.failed();
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link List}<{@link AnnouncementListPageDTO}>>
     */
    @ApiIgnore
    @PostMapping("/listAnnouncements")
    public RestResult<PageReturn<AnnouncementListPageDTO>> listAnnouncements(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return announcementService.listAnnouncements(pageRequest);
    }
}
