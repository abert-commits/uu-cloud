package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.FrontPageConfigInfoDTO;
import org.uu.common.pay.req.InsertFrontPageConfigReq;
import org.uu.common.pay.req.QueryFrontPageConfigReq;
import org.uu.common.pay.req.UpdateFrontPageConfigReq;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.entity.FrontPageConfig;
import org.uu.wallet.mapper.FrontPageConfigMapper;
import org.uu.wallet.service.FrontPageConfigService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FrontPageConfigServiceImpl extends ServiceImpl<FrontPageConfigMapper, FrontPageConfig> implements FrontPageConfigService {
    private final RedisUtils redisUtils;

    @Override
    public RestResult<Boolean> insertFrontPageConfig(InsertFrontPageConfigReq requestVO) {
        // 优先检索Redis
        Object cacheObj = redisUtils.hget(RedisConstants.FRONT_PAGE_CONFIG, requestVO.getLang().toString());
        if (Objects.nonNull(cacheObj)) {
            return RestResult.failed("You cannot add multiple configurations for the same language");
        }
        // 检索MySQL
        FrontPageConfig frontPageConfig = this.lambdaQuery()
                .eq(FrontPageConfig::getLang, requestVO.getLang())
                .one();
        if (Objects.nonNull(frontPageConfig)) {
            // 同步至MySQL
            redisUtils.hset(RedisConstants.FRONT_PAGE_CONFIG, requestVO.getLang().toString(), frontPageConfig);
            return RestResult.failed("You cannot add multiple configurations for the same language");
        }
        //  持久化至MySQL
        FrontPageConfig resultFrontPageConfig = FrontPageConfig.builder()
                .content(requestVO.getText())
                .createBy(requestVO.getOperator())
                .updateBy(requestVO.getOperator())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .lang(requestVO.getLang())
                .build();
        boolean saveResult = this.save(resultFrontPageConfig);
        // 同步至Redis
        redisUtils.hset(RedisConstants.FRONT_PAGE_CONFIG, requestVO.getLang().toString(), resultFrontPageConfig);
        return saveResult ? RestResult.ok(true) : RestResult.failed("Insert FrontPageConfig failed");
    }

    @Override
    public RestResult<Boolean> removeFrontPageConfigById(Long id) {
        FrontPageConfig frontPageConfig = this.getById(id);
        if (Objects.isNull(frontPageConfig)) {
            return RestResult.failed("This FrontPageConfig does not exist");
        }
        // 更新MySQL
        boolean removeResult = this.removeById(id);
        // 更新Redis
        redisUtils.hdel(RedisConstants.FRONT_PAGE_CONFIG, id.toString());
        return removeResult ? RestResult.ok(true) : RestResult.failed("Remove FrontPageConfig failed");
    }

    @Override
    public RestResult<Boolean> updateFrontPageConfig(UpdateFrontPageConfigReq requestVO) {
        FrontPageConfig frontPageConfig = this.getById(requestVO.getId());
        if (Objects.isNull(frontPageConfig)) {
            return RestResult.failed("This FrontPageConfig does not exist");
        }
        frontPageConfig.setContent(requestVO.getContent())
                .setUpdateBy(UserContext.getCurrentUserName())
                .setUpdateTime(LocalDateTime.now());
        // 更新MySQL
        boolean updateResult = this.updateById(frontPageConfig);
        // 更新Redis
        redisUtils.hset(RedisConstants.FRONT_PAGE_CONFIG, frontPageConfig.getLang().toString(), frontPageConfig);
        return updateResult ? RestResult.ok(true) : RestResult.failed("Update FrontPageConfig failed");
    }

    @Override
    public RestResult<FrontPageConfigInfoDTO> queryFrontPageConfigById(Long id) {
        FrontPageConfig frontPageConfig = this.getById(id);
        FrontPageConfigInfoDTO resultDTO = new FrontPageConfigInfoDTO();
        if (Objects.nonNull(frontPageConfig)) {
            BeanUtils.copyProperties(frontPageConfig, resultDTO);
        }
        return RestResult.ok(resultDTO);
    }

    @Override
    public RestResult<FrontPageConfigInfoDTO> queryFrontPageConfigByLang(Integer lang) {
        // 优先检索Redis
        FrontPageConfig cacheObj = (FrontPageConfig) redisUtils.hget(RedisConstants.FRONT_PAGE_CONFIG, lang.toString());
        if (Objects.isNull(cacheObj)) {
            cacheObj = this.lambdaQuery()
                    .eq(FrontPageConfig::getLang, lang)
                    .one();
            // 更新Redis
            redisUtils.hset(RedisConstants.FRONT_PAGE_CONFIG, lang.toString(), cacheObj);
        }
        FrontPageConfigInfoDTO resultDTO = new FrontPageConfigInfoDTO();
        if (Objects.nonNull(cacheObj)) {
            BeanUtils.copyProperties(cacheObj, resultDTO);
        }
        return RestResult.ok(resultDTO);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RestResult<PageReturn<FrontPageConfigInfoDTO>> queryFrontPageConfigPage(QueryFrontPageConfigReq requestVO) {
        // 构建分页实体
        Page<FrontPageConfig> frontPageConfigPage = new Page<>();
        frontPageConfigPage.setCurrent(requestVO.getPageNo())
                .setSize(requestVO.getPageSize());
        // 构建查询条件并执行
        Page<FrontPageConfig> resultPage = this.lambdaQuery()
                // 内容LIKE查询
                .like(StringUtils.isNotEmpty(requestVO.getText()) && StringUtils.isNotEmpty(requestVO.getText().trim()), FrontPageConfig::getContent, requestVO.getText())
                // 创建人ID EQ查询
                .eq(StringUtils.isNotEmpty(requestVO.getCreateBy()) && StringUtils.isNotEmpty(requestVO.getCreateBy().trim()), FrontPageConfig::getCreateBy, requestVO.getCreateBy())
                // 更新人ID EQ查询
                .eq(StringUtils.isNotEmpty(requestVO.getUpdateBy()) && StringUtils.isNotEmpty(requestVO.getUpdateBy().trim()), FrontPageConfig::getUpdateBy, requestVO.getUpdateBy())
                // 语种EQ查询
                .eq(Objects.nonNull(requestVO.getLang()), FrontPageConfig::getLang, requestVO.getLang())
                // 开始时间GE查询
                .ge(Objects.nonNull(requestVO.getCreateTimeStart()), FrontPageConfig::getCreateTime, requestVO.getCreateTimeStart())
                // 结束时间LE查询
                .le(Objects.nonNull(requestVO.getCreateTimeEnd()), FrontPageConfig::getCreateTime, requestVO.getCreateTimeEnd())
                // 创建时间DESC排序
                .orderByDesc(true, FrontPageConfig::getCreateTime)
                // 分页查询
                .page(frontPageConfigPage);
        List<FrontPageConfig> pageConfigList = resultPage.getRecords();
        List<FrontPageConfigInfoDTO> resultList = new ArrayList<>();
        // 列表非空做对象转换
        if (!CollectionUtils.isEmpty(pageConfigList)) {
            resultList = pageConfigList.stream()
                    .filter(Objects::nonNull)
                    .map(item -> {
                        FrontPageConfigInfoDTO tempDTO = new FrontPageConfigInfoDTO();
                        BeanUtils.copyProperties(item, tempDTO);
                        return tempDTO;
                    })
                    .collect(Collectors.toList());
        }
        return RestResult.ok(PageUtils.flush(frontPageConfigPage, resultList));
    }


    @Override
    public RestResult<List<FrontPageConfigInfoDTO>> frontPageConfigList() {
        List<FrontPageConfig> list = this.lambdaQuery().list();
        if (CollectionUtils.isEmpty(list)) {
            return RestResult.ok(Collections.emptyList());
        }

        List<FrontPageConfigInfoDTO> configInfoDTOS = list.stream()
                .map(this::convertToFrontPageConfigInfoDTO)
                .collect(Collectors.toList());

        return RestResult.ok(configInfoDTOS);
    }


    private FrontPageConfigInfoDTO convertToFrontPageConfigInfoDTO(FrontPageConfig entity) {
        return FrontPageConfigInfoDTO.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .lang(entity.getLang())
                .createBy(entity.getCreateBy())
                .createTime(entity.getCreateTime())
                .updateBy(entity.getUpdateBy())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
