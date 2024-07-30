package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.UsdtConfigDTO;
import org.uu.common.pay.req.UsdtConfigPageReq;
import org.uu.wallet.Enum.UsdtBuyStatusEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.UsdtConfig;
import org.uu.wallet.mapper.UsdtConfigMapper;
import org.uu.wallet.service.IUsdtConfigService;

import java.util.List;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
public class UsdtConfigServiceImpl extends ServiceImpl<UsdtConfigMapper, UsdtConfig> implements IUsdtConfigService {
    private final WalletMapStruct walletMapStruct;

    @Override
    public PageReturn<UsdtConfigDTO> listPage(UsdtConfigPageReq req) {
        Page<UsdtConfig> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<UsdtConfig> lambdaQuery = lambdaQuery();
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getNetworkProtocol())) {
            lambdaQuery.eq(UsdtConfig::getNetworkProtocol, req.getNetworkProtocol());
        }
        lambdaQuery.orderByDesc(UsdtConfig::getId);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<UsdtConfig> records = page.getRecords();
        List<UsdtConfigDTO> list = walletMapStruct.UsdtConfigTransform(records);
        return PageUtils.flush(page, list);
    }

    /**
     * 匹配USDT收款信息
     *
     * @param networkProtocol
     * @return {@link UsdtConfig}
     */
    @Override
    public UsdtConfig matchUsdtReceiptInfo(String networkProtocol) {
        return lambdaQuery().eq(UsdtConfig::getNetworkProtocol, networkProtocol).eq(UsdtConfig::getStatus, UsdtBuyStatusEnum.ENABLE.getCode()).last("LIMIT 1").one();
    }

    /**
     * 获取主网络下拉列表
     *
     * @return {@link List}
     */
    @Override
    public List getNetworkProtocol() {
        //去重查询 启用中的usdt 收款主网络
        LambdaQueryWrapper<UsdtConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(UsdtConfig::getNetworkProtocol).eq(UsdtConfig::getDeleted, 0).eq(UsdtConfig::getStatus, UsdtBuyStatusEnum.ENABLE.getCode()).groupBy(UsdtConfig::getNetworkProtocol);
        return getBaseMapper().selectObjs(queryWrapper);
    }
}
