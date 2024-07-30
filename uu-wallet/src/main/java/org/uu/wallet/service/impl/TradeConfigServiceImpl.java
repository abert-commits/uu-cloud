package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.mapper.TradeConfigMapper;
import org.uu.wallet.service.ITradeConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
public class TradeConfigServiceImpl extends ServiceImpl<TradeConfigMapper, TradeConfig> implements ITradeConfigService {

    private  final WalletMapStruct walletMapStruct;
    @Override
    public PageReturn<TradeConfigDTO> listPage(TradeConfigListPageReq req) {
        Page<TradeConfig> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<TradeConfig> lambdaQuery = lambdaQuery();
//        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getWithdrawalRewardRatio())) {
//            lambdaQuery.eq(CancellationRecharge::getReason, req.getWithdrawalRewardRatio());
//        }
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<TradeConfig> records = page.getRecords();
        List<TradeConfigDTO> listDTO = walletMapStruct.TradeConfigTransform(records);
        return PageUtils.flush(page, listDTO);
    }

    @Override
    public TradeConfigVoiceEnableDTO updateVoiceEnable(TradeConfigVoiceEnableReq req) {
        TradeConfig tradeConfig = baseMapper.selectById(req.getId());
        BeanUtils.copyProperties(req, tradeConfig);
        TradeConfigVoiceEnableDTO tradeConfigVoiceEnableDTO = new TradeConfigVoiceEnableDTO();
        BeanUtils.copyProperties(tradeConfig, tradeConfigVoiceEnableDTO);
        int update = baseMapper.updateById(tradeConfig);
        return update == 0 ? null : tradeConfigVoiceEnableDTO;
    }

    @Override
    public TradeWarningConfigDTO updateWarningConfig(TradeConfigWarningConfigUpdateReq req) {
        TradeConfig tradeConfig = baseMapper.selectById(req.getId());
        BeanUtils.copyProperties(req, tradeConfig);
        TradeWarningConfigDTO tradeConfigVoiceEnableDTO = new TradeWarningConfigDTO();
        BeanUtils.copyProperties(tradeConfig, tradeConfigVoiceEnableDTO);
        int update = baseMapper.updateById(tradeConfig);
        return update == 0 ? null : tradeConfigVoiceEnableDTO;
    }

    @Override
    public TradeWarningConfigDTO warningConfigDetail(TradeConfigIdReq req) {
        TradeConfig tradeConfig  = baseMapper.selectById(req.getId());
        TradeWarningConfigDTO tradeConfigDTO = new TradeWarningConfigDTO();
        BeanUtils.copyProperties(tradeConfig,tradeConfigDTO);
        return tradeConfigDTO;
    }

    @Override
    public TradeManualConfigDTO manualReview() {
        TradeConfig tradeConfig  = baseMapper.selectById(1);
        TradeManualConfigDTO dto = new TradeManualConfigDTO();
        BeanUtils.copyProperties(tradeConfig, dto);
        return dto;
    }

    @Override
    public Boolean verifyBuyRewardRatio(BigDecimal buyRewardRatio, BigDecimal buyRewardRatioMax) {
        TradeConfig tradeConfig = this.getById(1);
        if (Objects.isNull(buyRewardRatio) || Objects.isNull(tradeConfig) || buyRewardRatio.compareTo(tradeConfig.getBuyRewardRatioMin()) < 0) {
            return false;
        }

        if (Objects.nonNull(buyRewardRatioMax)) {
            return  buyRewardRatio.compareTo(buyRewardRatioMax) <= 0;
        }
        return buyRewardRatio.compareTo(tradeConfig.getBuyRewardRatioMax()) <= 0;
    }

}
