package org.uu.wallet.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.AnnouncementDTO;
import org.uu.common.pay.dto.AnnouncementLinkDTO;
import org.uu.common.pay.dto.AnnouncementListPageDTO;
import org.uu.common.pay.req.AnnouncementInfoReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.entity.Announcement;
import org.uu.wallet.mapper.AnnouncementMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.service.IAnnouncementService;
import org.uu.wallet.vo.AnnouncementVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * Banner信息表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-02-29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements IAnnouncementService {

    @Value("${oss.baseUrl}")
    private String baseUrl;

    private final ArProperty arProperty;

    /**
     * 前台-获取公告列表
     *
     * @return {@link RestResult}<{@link List}<{@link AnnouncementVo}>>
     */
    @Override
    public RestResult<PageReturn<AnnouncementVo>> getAnnouncementList(PageRequest req) {

        if (req == null) {
            req = new PageRequest();
        }

        Page<Announcement> pageAnnouncement = new Page<>();
        pageAnnouncement.setCurrent(req.getPageNo());
        pageAnnouncement.setSize(req.getPageSize());

        LambdaQueryChainWrapper<Announcement> lambdaQuery = lambdaQuery();

        lambdaQuery.eq(Announcement::getStatus, 1);

        //获取未删除的条目 并根据 序号进行排序 (数字小排前面)
        lambdaQuery.eq(Announcement::getDeleted, 0).orderByAsc(Announcement::getSortOrder);

        baseMapper.selectPage(pageAnnouncement, lambdaQuery.getWrapper());

        List<Announcement> records = pageAnnouncement.getRecords();

        PageReturn<Announcement> flush = PageUtils.flush(pageAnnouncement, records);

        //IPage＜实体＞转 IPage＜Vo＞
        List<AnnouncementVo> announcementVoList = new ArrayList<>();
        List<Announcement> announcementList = flush.getList();
        if (!CollectionUtils.isEmpty(announcementList)) {
            announcementVoList = announcementList.stream()
                    .filter(Objects::nonNull)
                    .map(item -> AnnouncementVo.builder()
                            .id(item.getId())
                            .announcementTitle(item.getAnnouncementTitle())
                            .announcementContent(item.getAnnouncementContent())
                            .coverImageUrl(item.getActivityPoster())
                            .createTime(item.getCreateTime())
                            .build())
                    .collect(Collectors.toList());
        }

        PageReturn<AnnouncementVo> announcementVoListPageReturn = new PageReturn<>();
        announcementVoListPageReturn.setPageNo(flush.getPageNo());
        announcementVoListPageReturn.setPageSize(flush.getPageSize());
        announcementVoListPageReturn.setTotal(flush.getTotal());
        announcementVoListPageReturn.setList(announcementVoList);

        return RestResult.ok(announcementVoListPageReturn);
    }

    /**
     * 新增 公告
     *
     * @param req
     * @return boolean
     */
    @Override
    public RestResult createAnnouncement(AnnouncementInfoReq req) {

        // 检查是否存在相同的排序值
        int count = lambdaQuery()
                .eq(Announcement::getSortOrder, req.getSortOrder())
                .eq(Announcement::getDeleted, 0)
                .count();
        if (count > 0) {
            //排序值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        Announcement announcement = new Announcement();

        BeanUtils.copyProperties(req, announcement);
        //拼接图片链接
        if (StringUtils.isNotEmpty(announcement.getActivityPoster())) {
            announcement.setActivityPoster(baseUrl + announcement.getActivityPoster());
        }

        // 设置bannerInfo的属性
        if (save(announcement)) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 根据id获取公告信息
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementDTO}>
     */
    @Override
    public RestResult<AnnouncementDTO> getAnnouncementById(Long id) {

        Announcement announcement = lambdaQuery()
                .eq(Announcement::getId, id)
                .eq(Announcement::getDeleted, 0)
                .one();

        if (announcement != null) {
            AnnouncementDTO announcementDTO = new AnnouncementDTO();
            BeanUtils.copyProperties(announcement, announcementDTO);

            return RestResult.ok(announcementDTO);
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    /**
     * 更新 公告信息
     *
     * @param id
     * @param req
     * @return {@link RestResult}
     */
    @Override
    public RestResult updateAnnouncement(Long id, AnnouncementInfoReq req) {
        // 检查是否存在相同的排序值且不是当前正在更新的banner
        long duplicatedCount = lambdaQuery()
                .eq(Announcement::getSortOrder, req.getSortOrder())
                .ne(Announcement::getId, id)
                .eq(Announcement::getDeleted, 0)
                .count();

        if (duplicatedCount > 0) {
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        return Optional.ofNullable(getById(id))
                .map(announcement -> {
                    updateAnnouncementInfo(announcement, req);
                    boolean update = updateById(announcement);
                    return update ? RestResult.ok() : RestResult.failed();
                })
                .orElse(RestResult.failure(ResultCode.DATA_NOT_FOUND));
    }

    private void updateAnnouncementInfo(Announcement announcement, AnnouncementInfoReq req) {
        announcement.setAnnouncementTitle(req.getAnnouncementTitle());
        announcement.setAnnouncementContent(req.getAnnouncementContent());
        announcement.setSortOrder(req.getSortOrder());
        announcement.setStatus(req.getStatus());

        String announcementImageUrl = req.getActivityPoster();
        if (announcementImageUrl != null && !announcementImageUrl.startsWith("https://")) {
            announcementImageUrl = baseUrl + announcementImageUrl;
        }
        announcement.setActivityPoster(announcementImageUrl);
    }


    /**
     * 删除公告
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean deleteAnnouncement(Long id) {
        return lambdaUpdate().eq(Announcement::getId, id).set(Announcement::getDeleted, 1).update();
    }


    /**
     * 禁用公告
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean disableAnnouncement(Long id) {

        Announcement announcement = getById(id);
        if (announcement != null) {
            announcement.setStatus(0); // 0为禁用状态
            return updateById(announcement);
        }
        return false;
    }


    /**
     * 启用公告
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean enableAnnouncement(Long id) {

        Announcement announcement = getById(id);
        if (announcement != null) {
            announcement.setStatus(1); // 1为启用状态
            return updateById(announcement);
        }
        return false;
    }

    /**
     * 分页查询 公告列表
     *
     * @param req
     * @return {@link RestResult}<{@link PageReturn}<{@link AnnouncementListPageDTO}>>
     */
    @Override
    public RestResult<PageReturn<AnnouncementListPageDTO>> listAnnouncements(PageRequest req) {


        //获取当前用户id
        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId == null) {
            log.error("分页查询 公告列表失败: 获取当前用户id失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        if (req == null) {
            req = new PageRequest();
        }

        Page<Announcement> pageAnnouncement = new Page<>();
        pageAnnouncement.setCurrent(req.getPageNo());
        pageAnnouncement.setSize(req.getPageSize());

        LambdaQueryChainWrapper<Announcement> lambdaQuery = lambdaQuery();


        //获取未删除的条目 并根据 序号进行排序 (数字小排前面)
        lambdaQuery.eq(Announcement::getDeleted, 0).orderByAsc(Announcement::getSortOrder);

        baseMapper.selectPage(pageAnnouncement, lambdaQuery.getWrapper());

        List<Announcement> records = pageAnnouncement.getRecords();

        PageReturn<Announcement> flush = PageUtils.flush(pageAnnouncement, records);

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<AnnouncementListPageDTO> AnnouncementListPageDTOList = new ArrayList<>();

        for (Announcement Announcement : flush.getList()) {

            AnnouncementListPageDTO announcementListPageDTO = new AnnouncementListPageDTO();


            BeanUtil.copyProperties(Announcement, announcementListPageDTO);

            //最后更新时间
            if (announcementListPageDTO.getUpdateTime() == null) {
                announcementListPageDTO.setUpdateTime(announcementListPageDTO.getCreateTime());
            }

            //操作人
            if (StringUtils.isEmpty(announcementListPageDTO.getUpdateBy())) {
                announcementListPageDTO.setUpdateBy(Announcement.getCreateBy());
            }
            AnnouncementListPageDTOList.add(announcementListPageDTO);
        }

        PageReturn<AnnouncementListPageDTO> announcementListPageDTOPageReturn = new PageReturn<>();
        announcementListPageDTOPageReturn.setPageNo(flush.getPageNo());
        announcementListPageDTOPageReturn.setPageSize(flush.getPageSize());
        announcementListPageDTOPageReturn.setTotal(flush.getTotal());
        announcementListPageDTOPageReturn.setList(AnnouncementListPageDTOList);

        log.info("分页查询 公告列表成功: 用户id: {}, req: {}, 返回数据: {}", currentUserId, req, announcementListPageDTOPageReturn);

        return RestResult.ok(announcementListPageDTOPageReturn);
    }


    /**
     * 前台-获取公告详情页
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementVo}>
     */
    @Override
    public RestResult<AnnouncementVo> findAnnouncementDetail(Long id) {
        Announcement announcement = lambdaQuery()
                .eq(Announcement::getId, id)
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getDeleted, 0)
                .one();

        if (announcement != null) {
            AnnouncementVo announcementVo = new AnnouncementVo();
            BeanUtils.copyProperties(announcement, announcementVo);

            return RestResult.ok(announcementVo);
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    /**
     * 获取公告链接
     *
     * @param id
     * @return {@link RestResult}<{@link AnnouncementLinkDTO}>
     */
    @Override
    public RestResult<AnnouncementLinkDTO> fetchAnnouncementLinkById(Long id) {

        AnnouncementLinkDTO announcementLinkDTO = new AnnouncementLinkDTO();
        announcementLinkDTO.setAnnouncementLink(arProperty.getAnnouncementLink() + id);
        return RestResult.ok(announcementLinkDTO);
    }
}
