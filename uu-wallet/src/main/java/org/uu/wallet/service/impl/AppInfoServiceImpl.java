package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.AppInfoDTO;
import org.uu.common.pay.req.AppInfoPageReq;
import org.uu.common.pay.req.AppInfoReq;
import org.uu.wallet.entity.AppInfo;
import org.uu.wallet.mapper.AppInfoMapper;
import org.uu.wallet.service.IAppInfoService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * app信息维护表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-25
 */
@Service
public class AppInfoServiceImpl extends ServiceImpl<AppInfoMapper, AppInfo> implements IAppInfoService {
    @Value("${oss.baseUrl}")
    private String baseUrl;

    @Override
    public PageReturn<AppInfoDTO> appInfoPage(AppInfoPageReq req) {
        Page<AppInfo> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(AppInfo::getCreateTime)
                .ge(Objects.nonNull(req.getCreateTimeStart()), AppInfo::getCreateTime, req.getCreateTimeStart())
                .le(Objects.nonNull(req.getCreateTimeEnd()), AppInfo::getCreateTime, req.getCreateTimeEnd())
                .page(page);

        List<AppInfoDTO> resultList = page.getRecords().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    AppInfoDTO dto = new AppInfoDTO();
                    BeanUtils.copyProperties(item, dto);
                    dto.setUpdateTime(Objects.nonNull(item.getUpdateTime()) ? item.getUpdateTime() : item.getCreateTime());
                    return dto;
                })
                .collect(Collectors.toList());

        return PageUtils.flush(page, resultList);
    }

    @Override
    public RestResult<AppInfoDTO> getAppInfoByDevice(Integer device) {
        AppInfo appInfo = lambdaQuery()
                .eq(AppInfo::getDevice, device)
                .orderByDesc(AppInfo::getAppVersion)
                .last("LIMIT 1")
                .one();

        if (Objects.isNull(appInfo)) {
            return RestResult.ok(new AppInfoDTO());
        }
        AppInfoDTO dto = new AppInfoDTO();
        BeanUtils.copyProperties(appInfo, dto);
        dto.setUpdateTime(Objects.nonNull(appInfo.getUpdateTime()) ? appInfo.getUpdateTime() : appInfo.getCreateTime());

        return RestResult.ok(dto);
    }

    @Override
    public RestResult addAppInfo(AppInfoReq req) {
        // 检查是否存在相同的值
        int count = lambdaQuery()
                .eq(AppInfo::getAppVersion, req.getAppVersion())
                .eq(AppInfo::getDevice, req.getDevice())
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(req, appInfo);


        if (StringUtils.isNotEmpty(appInfo.getDownloadUrl())) {
            appInfo.setDownloadUrl(baseUrl + appInfo.getDownloadUrl());
        }

        if (baseMapper.insert(appInfo) > 0) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    @Override
    public RestResult updateAppInfo(Long id, AppInfoReq req) {
        // 检查是否存在相同的值且不是当前正在更新
        int count = lambdaQuery()
                .eq(AppInfo::getAppVersion, req.getAppVersion())
                .eq(AppInfo::getDevice, req.getDevice())
                .ne(AppInfo::getId, id)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        return Optional.ofNullable(baseMapper.selectById(id))
                .map(appInfo -> {
                    AppInfo app = new AppInfo();
                    req.setId(id);
                    BeanUtils.copyProperties(req, app);
                    app.setDownloadUrl(formatDownloadUrl(req.getDownloadUrl()));

                    boolean update = updateById(app);
                    return update ? RestResult.ok() : RestResult.failed();
                })
                .orElse(RestResult.failure(ResultCode.DATA_NOT_FOUND));
    }

    private String formatDownloadUrl(String downloadUrl) {
        return Optional.ofNullable(downloadUrl)
                .filter(url -> !url.startsWith("https://"))
                .map(url -> baseUrl + url)
                .orElse(downloadUrl);
    }
}
