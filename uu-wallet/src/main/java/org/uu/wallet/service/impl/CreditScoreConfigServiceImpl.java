package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CreditScoreConfigDTO;
import org.uu.common.pay.req.CreditScoreConfigListPageReq;
import org.uu.common.pay.req.CreditScoreConfigUpdateReq;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.wallet.entity.CreditScoreConfig;
import org.uu.wallet.mapper.CreditScoreConfigMapper;
import org.uu.wallet.service.ICreditScoreConfigService;
import org.uu.wallet.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 信用分配置表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@Service
@RequiredArgsConstructor
public class CreditScoreConfigServiceImpl extends ServiceImpl<CreditScoreConfigMapper, CreditScoreConfig> implements ICreditScoreConfigService {

    private final RedisUtil redisUtil;
    @Override
    public PageReturn<CreditScoreConfigDTO> listPage(CreditScoreConfigListPageReq req) {
        Page<CreditScoreConfig> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<CreditScoreConfig> lambdaQuery = lambdaQuery();
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<CreditScoreConfig> creditScoreConfigs = page.getRecords();
        List<CreditScoreConfigDTO> dtoList = creditScoreConfigs.stream().map(creditScoreConfig -> {
            CreditScoreConfigDTO creditScoreConfigDTO = new CreditScoreConfigDTO();
            BeanUtils.copyProperties(creditScoreConfig, creditScoreConfigDTO);
            BigDecimal score = creditScoreConfig.getScore();
            creditScoreConfigDTO.setScore(score.toString());
            if(score.compareTo(BigDecimal.ZERO) > 0) {
                String sign = "+";
                creditScoreConfigDTO.setScore(sign + score);
            }
            return creditScoreConfigDTO;
        }).collect(Collectors.toList());
        return PageUtils.flush(page, dtoList);
    }

    @Override
    public RestResult<CreditScoreConfigDTO> updateScore(CreditScoreConfigUpdateReq req) {
        LambdaQueryChainWrapper<CreditScoreConfig> queryChainWrapper = lambdaQuery();
        queryChainWrapper.eq(CreditScoreConfig::getEventId, req.getEventId());
        CreditScoreConfig creditScoreConfig = baseMapper.selectOne(queryChainWrapper.getWrapper());
        if(Objects.isNull(creditScoreConfig)){
            return RestResult.failed(ResultCode.DATA_NOT_FOUND);
        }
        if(!req.getScore().startsWith("+") && !req.getScore().startsWith("-")){
            return RestResult.failed(ResultCode.PARAM_IS_EMPTY_OR_ERROR);
        }
        creditScoreConfig.setScore(new BigDecimal(req.getScore()));
        int i = baseMapper.updateById(creditScoreConfig);
        if(i != 1){
            return RestResult.failed(ResultCode.UPDATE_CREDIT_CONFIG_FAILED);
        }
        String key = RedisKeys.CREDIT_CONFIG + ":" + creditScoreConfig.getTradeType() + ":" + creditScoreConfig.getEventType();
        delRedisCreditConfig(key);
        CreditScoreConfigDTO creditScoreConfigDTO = new CreditScoreConfigDTO();
        creditScoreConfigDTO.setScore(creditScoreConfig.getScore().toString());
        return RestResult.ok(creditScoreConfigDTO);
    }

    @Override
    public CreditScoreConfig getCreditScoreConfig(Integer eventType, Integer tradeType) {
        // 查看redis中是否存在
        String key = RedisKeys.CREDIT_CONFIG + ":" + tradeType + ":" + eventType;
        String creditScoreConfig = redisUtil.getCreditScoreConfig(key);
        if(ObjectUtils.isEmpty(creditScoreConfig)){
            LambdaQueryChainWrapper<CreditScoreConfig> queryChainWrapper = lambdaQuery();
            queryChainWrapper.eq(CreditScoreConfig::getEventType, eventType);
            queryChainWrapper.eq(CreditScoreConfig::getTradeType, tradeType);
            CreditScoreConfig result = baseMapper.selectOne(queryChainWrapper.getWrapper());
            setRedisCreditConfig(key, result);
            return result;
        }
        return JSON.parseObject(creditScoreConfig, CreditScoreConfig.class);
    }

    private void setRedisCreditConfig(String key, CreditScoreConfig creditScoreConfig){
        redisUtil.setCreditScoreConfig(key, JSON.toJSONString(creditScoreConfig));
    }

    private void delRedisCreditConfig(String key){
        redisUtil.delCreditScoreConfig(key);
    }
}
