package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CancellationRechargeDTO;
import org.uu.common.pay.req.CancellationRechargePageListReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.CancellationRecharge;
import org.uu.wallet.mapper.CancellationRechargeMapper;
import org.uu.wallet.service.ICancellationRechargeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationRechargeServiceImpl extends ServiceImpl<CancellationRechargeMapper, CancellationRecharge> implements ICancellationRechargeService {
    private final WalletMapStruct walletMapStruct;

    @Override
    public PageReturn<CancellationRechargeDTO> listPage(CancellationRechargePageListReq req) {
        Page<CancellationRecharge> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<CancellationRecharge> lambdaQuery = lambdaQuery();
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getReason())) {
            lambdaQuery.eq(CancellationRecharge::getReason, req.getReason());
        }
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<CancellationRecharge> records = page.getRecords();
        List<CancellationRechargeDTO> list = walletMapStruct.CancellationRechargeTransform(records);
        return PageUtils.flush(page, list);
    }

    /**
     * 获取买入取消原因列表
     *
     * @return {@link List}<{@link String}>
     */
    @Override
    public List<String> getBuyCancelReasonsList() {
        List<CancellationRecharge> CancellationRechargeList = lambdaQuery().list();

        ArrayList<String> reasonList = new ArrayList<>();

        for (CancellationRecharge cancellationRecharge : CancellationRechargeList) {
            reasonList.add(cancellationRecharge.getReason());
        }

        return reasonList;
    }

}
