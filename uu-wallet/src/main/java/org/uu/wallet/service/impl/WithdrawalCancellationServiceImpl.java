package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.WithdrawalCancellationDTO;
import org.uu.common.pay.req.WithdrawalCancellationReq;
import org.uu.wallet.entity.WithdrawalCancellation;
import org.uu.wallet.mapper.WithdrawalCancellationMapper;
import org.uu.wallet.service.IWithdrawalCancellationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* @author 
*/
@Service
    public class WithdrawalCancellationServiceImpl extends ServiceImpl<WithdrawalCancellationMapper, WithdrawalCancellation> implements IWithdrawalCancellationService {
    @Override
    public PageReturn<WithdrawalCancellationDTO> listPage(WithdrawalCancellationReq req) {
        Page<WithdrawalCancellation> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<WithdrawalCancellation> lambdaQuery = lambdaQuery();
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getReason())) {
            lambdaQuery.eq(WithdrawalCancellation::getReason, req.getReason());
        }
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<WithdrawalCancellationDTO> list = new ArrayList<WithdrawalCancellationDTO>();
        List<WithdrawalCancellation> records = page.getRecords();
        for(WithdrawalCancellation withdrawalCancellation : records){
            WithdrawalCancellationDTO withdrawalCancellationDTO = new WithdrawalCancellationDTO();
            BeanUtils.copyProperties(withdrawalCancellation,withdrawalCancellationDTO);
            list.add(withdrawalCancellationDTO);
        }
        return PageUtils.flush(page, list);
    }

    /**
     * 获取卖出取消原因列表
     *
     * @return {@link List}<{@link String}>
     */
    @Override
    public List<String> getSellCancelReasonsList() {

        List<WithdrawalCancellation> cancellationRechargeList = lambdaQuery().list();

        ArrayList<String> reasonList = new ArrayList<>();

        for (WithdrawalCancellation withdrawalCancellation : cancellationRechargeList) {
            reasonList.add(withdrawalCancellation.getReason());
        }

        return reasonList;
    }
}
