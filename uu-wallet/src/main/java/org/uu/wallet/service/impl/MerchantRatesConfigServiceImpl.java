package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MerchantPayMentDTO;
import org.uu.common.pay.dto.MerchantRatesConfigDTO;
import org.uu.common.pay.req.MerchantRatesConfigPageReq;
import org.uu.common.pay.req.MerchantRatesConfigReq;
import org.uu.wallet.entity.MerchantRatesConfig;
import org.uu.wallet.mapper.MerchantRatesConfigMapper;
import org.uu.wallet.service.IMerchantRatesConfigService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author afei
 * @date 2024/7/15
 */
@Service
@Slf4j
public class MerchantRatesConfigServiceImpl extends ServiceImpl<MerchantRatesConfigMapper, MerchantRatesConfig> implements IMerchantRatesConfigService {


    @Override
    public PageReturn<MerchantRatesConfigDTO> merchantRatesConfigListPage(MerchantRatesConfigPageReq req) {
        Page<MerchantRatesConfig> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(MerchantRatesConfig::getUpdateTime)
                .eq(MerchantRatesConfig::getDeleted, 0)
                .eq(MerchantRatesConfig::getType, req.getType())
                .eq(MerchantRatesConfig::getMerchantCode, req.getMerchantCode())
                .page(page);

        List<MerchantRatesConfigDTO> list = page.getRecords().stream()
                .map(this::convertToMerchantRatesConfigDTO)
                .collect(Collectors.toList());

        return PageUtils.flush(page, list);
    }

    @Override
    public RestResult addMerchantRatesConfig(MerchantRatesConfigReq req) {
        // 检查是否存在相同的类型
        int count = lambdaQuery()
                .eq(MerchantRatesConfig::getMerchantCode, req.getMerchantCode())
                .eq(MerchantRatesConfig::getPayType, req.getPayType())
                .eq(MerchantRatesConfig::getType, req.getType())
                .eq(MerchantRatesConfig::getDeleted, 0)
                .count();
        if (count > 0) {
            //值重复
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }

        MerchantRatesConfig merchantRatesConfig = new MerchantRatesConfig();
        BeanUtils.copyProperties(req, merchantRatesConfig);

        if (baseMapper.insert(merchantRatesConfig) > 0) {
            return RestResult.ok();
        }

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    @Override
    public boolean deleteMerchantRatesConfig(Long id) {
        return lambdaUpdate().eq(MerchantRatesConfig::getId, id).set(MerchantRatesConfig::getDeleted, 1).update();
    }

    @Override
    public RestResult updateMerchantRatesConfig(Long id, MerchantRatesConfigReq req) {
        MerchantRatesConfig merchantRatesConfig = baseMapper.selectById(id);
        if (Objects.nonNull(merchantRatesConfig)) {
            merchantRatesConfig.setMoneyMax(req.getMoneyMax());
            merchantRatesConfig.setMoneyMin(req.getMoneyMin());
            merchantRatesConfig.setRates(req.getRates());
            merchantRatesConfig.setFixedFee(req.getFixedFee());
            merchantRatesConfig.setPaymentReminderAmount(req.getPaymentReminderAmount());
            merchantRatesConfig.setPayType(req.getPayType());
            merchantRatesConfig.setPayTypeName(req.getPayTypeName());
            merchantRatesConfig.setStatus(req.getStatus());
            boolean update = updateById(merchantRatesConfig);

            return update ? RestResult.ok() : RestResult.failed();
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    @Override
    public RestResult<MerchantRatesConfigDTO> getMerchantRatesConfigById(Long id) {
        MerchantRatesConfig merchantRatesConfig = lambdaQuery()
                .eq(MerchantRatesConfig::getId, id)
                .eq(MerchantRatesConfig::getDeleted, 0)
                .one();

        if (Objects.nonNull(merchantRatesConfig)) {
            MerchantRatesConfigDTO merchantRatesConfigDTO = new MerchantRatesConfigDTO();
            BeanUtils.copyProperties(merchantRatesConfig, merchantRatesConfigDTO);
            return RestResult.ok(merchantRatesConfigDTO);
        } else {
            return RestResult.failure(ResultCode.DATA_NOT_FOUND);
        }
    }

    @Override
    public Map<String, Map<Integer, List<MerchantPayMentDTO>>> getMerchantPayMentDTOs(List<String> merchantCodes, List<Integer> paymentTypes) {
        List<MerchantRatesConfig> merchantRatesConfigs = lambdaQuery()
                .orderByDesc(MerchantRatesConfig::getUpdateTime)
                .eq(MerchantRatesConfig::getDeleted, 0)
                .eq(MerchantRatesConfig::getStatus, 1)
                .in(MerchantRatesConfig::getType, paymentTypes)
                .in(MerchantRatesConfig::getMerchantCode, merchantCodes)
                .list();

        // 将查询结果映射到 MerchantPayMentDTO 对象中
        List<MerchantPayMentDTO> results = merchantRatesConfigs.stream()
                .map(config -> {
                    MerchantPayMentDTO dto = new MerchantPayMentDTO();
                    dto.setMerchantCode(config.getMerchantCode());
                    dto.setPaymentType(config.getType());
                    dto.setPayType(config.getPayType());
                    dto.setPayTypeName(config.getPayTypeName());
                    dto.setMoneyMin(config.getMoneyMin());
                    dto.setMoneyMax(config.getMoneyMax());
                    dto.setRates(config.getRates());
                    dto.setFixedFee(config.getFixedFee());
                    return dto;
                })
                .collect(Collectors.toList());

        // 对结果进行按 merchantId 和 paymentType 分组
        Map<String, Map<Integer, List<MerchantPayMentDTO>>> resultMap = results.stream()
                .collect(Collectors.groupingBy(
                        MerchantPayMentDTO::getMerchantCode,
                        Collectors.groupingBy(
                                MerchantPayMentDTO::getPaymentType
                        )
                ));

        return resultMap;
    }


    private MerchantRatesConfigDTO convertToMerchantRatesConfigDTO(MerchantRatesConfig entity) {
        return MerchantRatesConfigDTO.builder()
                .id(entity.getId())
                .payType(entity.getPayType())
                .payTypeName(entity.getPayTypeName())
                .status(entity.getStatus())
                .rates(entity.getRates())
                .fixedFee(entity.getFixedFee())
                .moneyMax(entity.getMoneyMax())
                .moneyMin(entity.getMoneyMin())
                .paymentReminderAmount(entity.getPaymentReminderAmount())
                .updateBy(entity.getUpdateBy())
                .updateTime(entity.getUpdateTime())
                .build();
    }


    /**
     * 根据商户号获取商户支付类型配置
     *
     * @param type         1: 代收, 2: 代付
     * @param payType      支付类型: 1: 银行卡, 2: USDT, 3: UPI
     * @param merchantCode 商户号
     * @return {@link MerchantRatesConfig }
     */
    @Override
    public MerchantRatesConfig getMerchantRatesConfigByCode(String type, String payType, String merchantCode) {
        return lambdaQuery()
                .eq(MerchantRatesConfig::getType, type)
                .eq(MerchantRatesConfig::getPayType, payType)
                .eq(MerchantRatesConfig::getMerchantCode, merchantCode)
                .eq(MerchantRatesConfig::getDeleted, 0)
                .eq(MerchantRatesConfig::getStatus, 1)
                .one();
    }

    @Override
    public Map<String, String> getMerchantRates(Integer type, List<String> payTypes, String merchantCode) {
        Map<String, MerchantRatesConfig> resultMap = lambdaQuery()
                .eq(MerchantRatesConfig::getType, type)
                .eq(MerchantRatesConfig::getMerchantCode, merchantCode)
                .eq(MerchantRatesConfig::getDeleted, 0)
                .eq(MerchantRatesConfig::getStatus, 1)
                .list()
                .stream()
                .collect(Collectors.toMap(MerchantRatesConfig::getPayType, Function.identity()));

        return payTypes.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        payType -> {
                            MerchantRatesConfig ratesConfig = resultMap.getOrDefault(payType, null);
                            return Objects.nonNull(ratesConfig) ? ratesConfig.getRates() + "%" + "+" + ratesConfig.getFixedFee() : "--";
                        }
                ));
    }
}
