package org.uu.manager.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BiMerchantWithdrawOrderDailyDTO;
import org.uu.manager.entity.BiMerchantWithdrawOrderDaily;
import org.uu.manager.entity.BiMerchantWithdrawOrderMonth;
import org.uu.manager.mapper.BiMerchantWithdrawOrderMonthMapper;
import org.uu.manager.req.MerchantMonthReportReq;
import org.uu.manager.service.IBiMerchantWithdrawOrderMonthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
@Service
public class BiMerchantWithdrawOrderMonthServiceImpl extends ServiceImpl<BiMerchantWithdrawOrderMonthMapper, BiMerchantWithdrawOrderMonth> implements IBiMerchantWithdrawOrderMonthService {

    @Override
    public PageReturn<BiMerchantWithdrawOrderMonth> listPage(MerchantMonthReportReq req) {
        Page<BiMerchantWithdrawOrderMonth> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<BiMerchantWithdrawOrderMonth> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiMerchantWithdrawOrderMonth::getDateTime);

        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiMerchantWithdrawOrderMonth::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiMerchantWithdrawOrderMonth::getDateTime, req.getEndTime());
        }

        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(BiMerchantWithdrawOrderMonth::getMerchantCode, req.getMerchantCode());
        }

        page = baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<BiMerchantWithdrawOrderMonth> records = page.getRecords();
        return PageUtils.flush(page, records);
    }

    @Override
    public PageReturn<BiMerchantWithdrawOrderDailyDTO> listPageForExport(MerchantMonthReportReq req) {
        List<BiMerchantWithdrawOrderDailyDTO> list = new ArrayList<>();
        PageReturn<BiMerchantWithdrawOrderMonth> biWithdrawOrderDailyPageReturn = listPage(req);
        List<BiMerchantWithdrawOrderMonth> data = biWithdrawOrderDailyPageReturn.getList();
        for (BiMerchantWithdrawOrderMonth item : data) {
            BiMerchantWithdrawOrderDailyDTO biWithdrawOrderDailyExportDTO = new BiMerchantWithdrawOrderDailyDTO();
            BeanUtils.copyProperties(item, biWithdrawOrderDailyExportDTO);
            list.add(biWithdrawOrderDailyExportDTO);
        }
        Page<BiMerchantWithdrawOrderDailyDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(biWithdrawOrderDailyPageReturn.getTotal());
        return PageUtils.flush(page, list);
    }
}
