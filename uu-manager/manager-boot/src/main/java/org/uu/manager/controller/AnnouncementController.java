package org.uu.manager.controller;

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
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.AnnouncementFeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/announcement")
@Validated
@RequiredArgsConstructor
@Api(description = "公告管理控制器")
public class AnnouncementController {


    private final AnnouncementFeignClient announcementFeignClient;


    /**
     * 获取公告链接
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/fetchAnnouncementLinkById/{id}")
    @ApiOperation(value = "获取公告链接")
    public RestResult<AnnouncementLinkDTO> fetchAnnouncementLinkById(@PathVariable Long id) {
        return announcementFeignClient.fetchAnnouncementLinkById(id);
    }

    /**
     * 新增 公告
     *
     * @param req
     * @return boolean
     */
    @PostMapping("/createAnnouncement")
    @SysLog(title = "公告管理控制器", content = "新增")
    @ApiOperation(value = "新增公告")
    public RestResult createAnnouncement(@RequestBody @ApiParam @Valid AnnouncementInfoReq req) {
        return announcementFeignClient.createAnnouncement(req);
    }

    /**
     * 根据ID查询公告信息
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @GetMapping("/getAnnouncementById/{id}")
    @ApiOperation(value = "根据ID查询公告信息")
    public RestResult<AnnouncementDTO> getAnnouncementById(@PathVariable Long id) {
        return announcementFeignClient.getAnnouncementById(id);
    }

    /**
     * 更新 公告
     *
     * @param id
     * @param req
     * @return boolean
     */
    @PostMapping("/updateAnnouncement/{id}")
    @SysLog(title = "公告管理控制器", content = "更新")
    @ApiOperation(value = "更新公告")
    public RestResult updateAnnouncement(@PathVariable Long id, @RequestBody @ApiParam @Valid AnnouncementInfoReq req) {
        return announcementFeignClient.updateAnnouncement(id, req);
    }

    /**
     * 删除 公告
     *
     * @param id
     * @return boolean
     */
    @DeleteMapping("/{id}")
    @SysLog(title = "公告管理控制器", content = "删除")
    @ApiOperation(value = "删除公告")
    public RestResult deleteAnnouncement(@PathVariable Long id) {
        return announcementFeignClient.deleteAnnouncement(id);
    }

    /**
     * 禁用 公告
     *
     * @param id
     * @return boolean
     */
    @PostMapping("/disable/{id}")
    @SysLog(title = "公告管理控制器", content = "禁用")
    @ApiOperation(value = "禁用公告")
    public RestResult disableAnnouncement(@PathVariable Long id) {
        return announcementFeignClient.disableAnnouncement(id);
    }

    /**
     * 启用 公告
     *
     * @param id
     * @return {@link RestResult}
     */
    @PostMapping("/enable/{id}")
    @SysLog(title = "公告管理控制器", content = "启用")
    @ApiOperation(value = "启用公告")
    public RestResult enableAnnouncement(@PathVariable Long id) {
        return announcementFeignClient.enableAnnouncement(id);
    }

    /**
     * 分页查询
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link List}<{@link AnnouncementListPageDTO}>>
     */
    @PostMapping("/listAnnouncements")
    @ApiOperation(value = "分页获取 公告列表 默认获取第一页 20条记录")
    public RestResult<PageReturn<AnnouncementListPageDTO>> listAnnouncements(@RequestBody(required = false) @ApiParam @Valid PageRequest pageRequest) {
        return announcementFeignClient.listAnnouncements(pageRequest);
    }
}
