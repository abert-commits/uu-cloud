package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.common.pay.dto.AnnouncementLinkDTO;
import org.uu.common.pay.dto.AnnouncementListPageDTO;
import org.uu.common.pay.req.AnnouncementInfoReq;
import org.uu.wallet.entity.Announcement;
import org.uu.wallet.vo.AnnouncementVo;

import java.util.List;

/**
 * <p>
 * Banner信息表 服务类
 * </p>
 *
 * @author
 * @since 2024-02-29
 */
public interface IAnnouncementService extends IService<Announcement> {

    /**
     * 前台-获取公告列表
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link List}<{@link AnnouncementVo}>>
     */
    RestResult<PageReturn<AnnouncementVo>> getAnnouncementList(PageRequest pageRequest);

    /**
     * 新增 公告
     *
     * @param req
     * @return boolean
     */
    RestResult createAnnouncement(AnnouncementInfoReq req);

    /**
     * 根据id获取公告信息
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    RestResult<AnnouncementDTO> getAnnouncementById(Long id);

    /**
     * 更新 公告信息
     *
     * @param id
     * @param req
     * @return {@link RestResult}
     */
    RestResult updateAnnouncement(Long id, AnnouncementInfoReq req);

    /**
     * 删除公告
     *
     * @param id
     * @return boolean
     */
    boolean deleteAnnouncement(Long id);

    /**
     * 禁用公告
     *
     * @param id
     * @return boolean
     */
    boolean disableAnnouncement(Long id);

    /**
     * 启用公告
     *
     * @param id
     * @return boolean
     */
    boolean enableAnnouncement(Long id);

    /**
     * 分页查询 公告列表
     *
     * @param pageRequest
     * @return {@link RestResult}<{@link PageReturn}<{@link AnnouncementListPageDTO}>>
     */
    RestResult<PageReturn<AnnouncementListPageDTO>> listAnnouncements(PageRequest pageRequest);

    /**
     * 前台-获取公告详情页
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementVo}>
     */
    RestResult<AnnouncementVo> findAnnouncementDetail(Long id);

    /**
     * 获取公告链接
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementLinkDTO}>
     */
    RestResult<AnnouncementLinkDTO> fetchAnnouncementLinkById(Long id);
}
