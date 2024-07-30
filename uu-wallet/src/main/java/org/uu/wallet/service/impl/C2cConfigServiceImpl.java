package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.C2cConfigDTO;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.C2cConfig;
import org.uu.wallet.entity.CancellationRecharge;
import org.uu.wallet.mapper.C2cConfigMapper;
import org.uu.wallet.req.C2cConfigReq;
import org.uu.wallet.req.CancellationRechargeReq;
import org.uu.wallet.service.IC2cConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 
*/  @RequiredArgsConstructor
    @Service
    public class C2cConfigServiceImpl extends ServiceImpl<C2cConfigMapper, C2cConfig> implements IC2cConfigService {

    private final WalletMapStruct walletMapStruct;

    @Override
    public PageReturn<C2cConfigDTO> listPage(C2cConfigReq req) {
        Page<C2cConfig> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<C2cConfig> lambdaQuery = lambdaQuery();
//        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getWithdrawalRewardRatio())) {
//            lambdaQuery.eq(CancellationRecharge::getReason, req.getWithdrawalRewardRatio());
//        }
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<C2cConfig> records = page.getRecords();
        List<C2cConfigDTO> listDTO = walletMapStruct.C2cConfigTransform(records);
        return PageUtils.flush(page, listDTO);
    }

    }
