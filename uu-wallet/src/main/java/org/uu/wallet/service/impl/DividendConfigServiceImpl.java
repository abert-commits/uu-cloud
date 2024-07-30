package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.dto.DividendConfigDTO;
import org.uu.common.pay.req.DividendConfigReq;
import org.uu.wallet.entity.DividendConfig;
import org.uu.wallet.mapper.DividendConfigMapper;
import org.uu.wallet.service.DividendConfigService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 分红配置表 服务实现类
 * </p>
 *
 * @author Parker
 * @since 2024-07-02
 */
@Service
public class DividendConfigServiceImpl extends ServiceImpl<DividendConfigMapper, DividendConfig> implements DividendConfigService {

    @Override
    public List<DividendConfigDTO> dividendConfigList() {
        List<DividendConfig> list = list();
        return list.stream()
                .map(this::cast)
                .collect(Collectors.toList());
    }

    @Override
    public RestResult updateDividendConfig(Long id, DividendConfigReq req) {
        DividendConfig dividendConfig = getById(id);
        if (Objects.isNull(dividendConfig)) {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }

        // 获取上一个等级值
        DividendConfig dividendsLevelMin = lambdaQuery()
                .eq(DividendConfig::getDividendsLevel, dividendConfig.getDividendsLevel() - 1)
                .one();
        // 获取下一个等级值
        DividendConfig dividendsLevelMax = lambdaQuery()
                .eq(DividendConfig::getDividendsLevel, dividendConfig.getDividendsLevel() + 1)
                .one();

        // 判断是否需要验证RewardRatio和CriticalPoint的值
        if ((dividendsLevelMin == null || req.getRewardRatio().compareTo(dividendsLevelMin.getRewardRatio()) > 0)
                && (dividendsLevelMax == null || req.getRewardRatio().compareTo(dividendsLevelMax.getRewardRatio()) < 0)
                && (dividendsLevelMin == null || req.getCriticalPoint() > dividendsLevelMin.getCriticalPoint())
                && (dividendsLevelMax == null || req.getCriticalPoint() < dividendsLevelMax.getCriticalPoint())) {
            dividendConfig.setRewardRatio(req.getRewardRatio());
            dividendConfig.setCriticalPoint(req.getCriticalPoint());
            boolean update = updateById(dividendConfig);
            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failed();
        }
    }

    @Override
    public RestResult addDividendConfig(DividendConfigReq req) {
        // 检查是否存在相同的分红临界点值
        int count = lambdaQuery()
                .eq(DividendConfig::getCriticalPoint, req.getCriticalPoint())
                .count();
        if (count > 0) {
            // 分红临界点值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        // 查询 dividends_level 分红等级的最大值
        Integer maxDividendsLevel = lambdaQuery()
                .select(DividendConfig::getDividendsLevel)
                .orderByDesc(DividendConfig::getDividendsLevel)
                .last("limit 1")
                .oneOpt()
                .map(DividendConfig::getDividendsLevel)
                .map(level -> level + 1)
                .orElse(1);

        DividendConfig dividendConfig = new DividendConfig();
        BeanUtils.copyProperties(req, dividendConfig);
        dividendConfig.setDividendsLevel(maxDividendsLevel);

        boolean success = baseMapper.insert(dividendConfig) > 0;
        return success ? RestResult.ok() : RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    private DividendConfigDTO cast(DividendConfig dividendConfig) {
        BigDecimal dividendAmount = calculateDividendAmount(dividendConfig);
        return new DividendConfigDTO(dividendConfig.getId(), dividendConfig.getCriticalPoint(), dividendConfig.getRewardRatio(), dividendAmount);
    }

    private BigDecimal calculateDividendAmount(DividendConfig dividendConfig) {
        BigDecimal dividendAmount = BigDecimal.ZERO;

        if (dividendConfig.getCriticalPoint() != null && dividendConfig.getRewardRatio() != null) {
            dividendAmount = BigDecimal.valueOf(dividendConfig.getCriticalPoint())
                    .multiply(dividendConfig.getRewardRatio())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return dividendAmount;
    }
}
