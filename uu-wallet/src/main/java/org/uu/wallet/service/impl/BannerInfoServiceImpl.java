package org.uu.wallet.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BannerInfoDTO;
import org.uu.common.pay.dto.BannerInfoListPageDTO;
import org.uu.common.pay.req.BannerInfoReq;
import org.uu.common.pay.req.BannerPageReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.entity.BannerInfo;
import org.uu.wallet.mapper.BannerInfoMapper;
import org.uu.wallet.service.IBannerInfoService;
import org.uu.wallet.vo.BannerListVo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * Banner信息表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-02-28
 */
@Service
@Slf4j
public class BannerInfoServiceImpl extends ServiceImpl<BannerInfoMapper, BannerInfo> implements IBannerInfoService {

    @Value("${oss.baseUrl}")
    private String baseUrl;

    /**
     * 新增 Banner
     *
     * @param req
     * @return boolean
     */
    @Override
    public RestResult createBanner(BannerInfoReq req) {

        // 检查是否存在相同的排序值
        int count = lambdaQuery()
                .eq(BannerInfo::getSortOrder, req.getSortOrder())
                .eq(BannerInfo::getBannerType, req.getBannerType())
                .eq(BannerInfo::getDeleted, 0)
                .count();
        if (count > 0) {
            //排序值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        BannerInfo bannerInfo = new BannerInfo(); // 转换req到entity

        BeanUtils.copyProperties(req, bannerInfo);

        //拼接图片链接
        bannerInfo.setBannerImageUrl(baseUrl + bannerInfo.getBannerImageUrl());

        // 设置bannerInfo的属性
        if (save(bannerInfo)) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    /**
     * 根据ID查询Banner信息
     *
     * @param id
     * @return {@link BannerInfo}
     */
    @Override
    public RestResult<BannerInfoDTO> getBannerById(Long id) {

        BannerInfo bannerInfo = lambdaQuery()
                .eq(BannerInfo::getId, id)
                .eq(BannerInfo::getDeleted, 0)
                .one();

        if (bannerInfo != null) {
            BannerInfoDTO bannerInfoDTO = new BannerInfoDTO();
            BeanUtils.copyProperties(bannerInfo, bannerInfoDTO);

            return RestResult.ok(bannerInfoDTO);
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    /**
     * 更新 Banner
     *
     * @param id
     * @param req
     * @return boolean
     */
    @Override
    public RestResult updateBanner(Long id, BannerInfoReq req) {

        // 检查是否存在相同的排序值且不是当前正在更新的banner
        int count = lambdaQuery()
                .eq(BannerInfo::getSortOrder, req.getSortOrder())
                .eq(BannerInfo::getBannerType, req.getBannerType())
                .ne(BannerInfo::getId, id) // 排除当前正在更新的banner
                .eq(BannerInfo::getDeleted, 0)
                .count();
        if (count > 0) {
            //排序值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        // 检查bannerImageUrl是否以"http"开头
        String bannerImageUrl = req.getBannerImageUrl();


        if (bannerImageUrl != null && !bannerImageUrl.startsWith("https://")) {
            // 如果不是以"http"开头，则进行拼接
            bannerImageUrl = baseUrl + bannerImageUrl;
        }

        BannerInfo bannerInfo = getById(id);
        if (bannerInfo != null) {
            // 修改bannerInfo的属性
            bannerInfo.setBannerType(req.getBannerType());
            bannerInfo.setSortOrder(req.getSortOrder());
            bannerInfo.setRedirectUrl(req.getRedirectUrl());
            bannerInfo.setBannerImageUrl(bannerImageUrl);
            bannerInfo.setStatus(req.getStatus());
            bannerInfo.setLinkType(req.getLinkType());
            boolean update = updateById(bannerInfo);

            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    /**
     * 删除 Banner
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean deleteBanner(Long id) {
        return lambdaUpdate().eq(BannerInfo::getId, id).set(BannerInfo::getDeleted, 1).update();
    }

    /**
     * 禁用 Banner
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean disableBanner(Long id) {

        BannerInfo bannerInfo = getById(id);
        if (bannerInfo != null) {
            bannerInfo.setStatus(0); // 0为禁用状态
            return updateById(bannerInfo);
        }
        return false;
    }


    /**
     * 启用 Banner
     *
     * @param id
     * @return boolean
     */
    @Override
    public boolean enableBanner(Long id) {
        BannerInfo bannerInfo = getById(id);
        if (bannerInfo != null) {
            bannerInfo.setStatus(1); // 1为启用状态
            return updateById(bannerInfo);
        }
        return false;
    }


    /**
     * 分页查询 banner列表
     *
     * @param req
     * @return {@link RestResult}<{@link PageReturn}<{@link BannerInfoListPageDTO}>>
     */
    @Override
    public RestResult<PageReturn<BannerInfoListPageDTO>> listPage(BannerPageReq req) {
        // 获取当前用户id，如果获取失败则抛出异常
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error("分页查询 banner列表失败: 获取当前用户id失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        // 确保req不为null
        req = req != null ? req : new BannerPageReq();

        // 构建分页查询条件
        LambdaQueryChainWrapper<BannerInfo> lambdaQuery = lambdaQuery()
                .eq(BannerInfo::getDeleted, 0)
                .eq(BannerInfo::getBannerType, req.getBannerType())
                .orderByAsc(BannerInfo::getSortOrder);

        // 执行分页查询
        Page<BannerInfo> pageBannerInfo = new Page<>(req.getPageNo(), req.getPageSize());
        baseMapper.selectPage(pageBannerInfo, lambdaQuery.getWrapper());

        // 转换实体对象为DTO对象
        List<BannerInfoListPageDTO> bannerInfoListPageDTOList = pageBannerInfo.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageReturn<BannerInfoListPageDTO> bannerInfoListPageDTOPageReturn = new PageReturn<>();
        BeanUtil.copyProperties(PageUtils.flush(pageBannerInfo, pageBannerInfo.getRecords()), bannerInfoListPageDTOPageReturn);
        bannerInfoListPageDTOPageReturn.setList(bannerInfoListPageDTOList);

        // 日志记录
        log.info("分页查询 banner列表成功: 用户id: {}, req: {}, 返回数据: {}", currentUserId, req, bannerInfoListPageDTOPageReturn);

        return RestResult.ok(bannerInfoListPageDTOPageReturn);
    }

    private BannerInfoListPageDTO convertToDTO(BannerInfo bannerInfo) {
        BannerInfoListPageDTO bannerInfoListPageDTO = new BannerInfoListPageDTO();
        BeanUtil.copyProperties(bannerInfo, bannerInfoListPageDTO);

        // 设置更新时间和操作人
        bannerInfoListPageDTO.setUpdateTime(Optional.ofNullable(bannerInfoListPageDTO.getUpdateTime()).orElse(bannerInfo.getCreateTime()));
        bannerInfoListPageDTO.setUpdateBy(StringUtils.isBlank(bannerInfoListPageDTO.getUpdateBy()) ? bannerInfo.getCreateBy() : bannerInfoListPageDTO.getUpdateBy());

        return bannerInfoListPageDTO;
    }


    /**
     * 获取 Banner列表
     *
     * @return {@link RestResult}<{@link BannerListVo}>
     */
    @Override
    public RestResult<Map<String, List<BannerListVo>>> getBannerList() {
        List<BannerInfo> list = lambdaQuery()
                .eq(BannerInfo::getDeleted, 0)
                .eq(BannerInfo::getStatus, 1)
                .orderByAsc(BannerInfo::getSortOrder) // 添加排序
                .list();

        Map<String, List<BannerListVo>> groupedBanners = list.stream()
                .map(bannerInfo -> {
                    BannerListVo bannerListVo = new BannerListVo();
                    BeanUtils.copyProperties(bannerInfo, bannerListVo);
                    return bannerListVo;
                })
                .collect(Collectors.groupingBy(bannerListVo -> {
                    // 根据bannerType的值转换为英文标识符
                    switch (bannerListVo.getBannerType()) {
                        case "01":
                            return "bannerOne";
                        case "02":
                            return "bannerTwo";
                        default:
                            return "unknownType"; // 默认分组
                    }
                }));

        return RestResult.ok(groupedBanners);
    }
}
