package org.uu.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BiMerchantPayOrderExportDTO;
import org.uu.common.pay.dto.MerchantOrderOverviewDTO;
import org.uu.manager.entity.BiMerchantPayOrderDaily;
import org.uu.manager.mapper.BiMerchantPayOrderDailyMapper;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.req.MerchantMonthReportReq;
import org.uu.manager.service.IBiMerchantPayOrderDailyService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
@Service
public class BiMerchantPayOrderDailyServiceImpl extends ServiceImpl<BiMerchantPayOrderDailyMapper, BiMerchantPayOrderDaily> implements IBiMerchantPayOrderDailyService {

    @Override
    public PageReturn<BiMerchantPayOrderDaily> listPage(MerchantMonthReportReq req) {
        Page<BiMerchantPayOrderDaily> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<BiMerchantPayOrderDaily> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiMerchantPayOrderDaily::getDateTime);

        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiMerchantPayOrderDaily::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiMerchantPayOrderDaily::getDateTime, req.getEndTime());
        }

        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(BiMerchantPayOrderDaily::getMerchantCode, req.getMerchantCode());
        }

        page = baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<BiMerchantPayOrderDaily> records = page.getRecords();
        return PageUtils.flush(page, records);
    }

    @Override
    public PageReturn<BiMerchantPayOrderExportDTO> listPageForExport(MerchantMonthReportReq req) {
        List<BiMerchantPayOrderExportDTO> list = new ArrayList<>();
        PageReturn<BiMerchantPayOrderDaily> biMerchantPayOrderDailyPageReturn = listPage(req);
        List<BiMerchantPayOrderDaily> data = biMerchantPayOrderDailyPageReturn.getList();
        for (BiMerchantPayOrderDaily item : data) {
            BiMerchantPayOrderExportDTO dto = new BiMerchantPayOrderExportDTO();
            BeanUtils.copyProperties(item, dto);
            list.add(dto);
        }
        Page<BiMerchantPayOrderExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(biMerchantPayOrderDailyPageReturn.getTotal());
        return PageUtils.flush(page, list);
    }

    @Override
    public MerchantOrderOverviewDTO getMerchantOrderOverview(MerchantDailyReportReq req) {
        LambdaQueryWrapper<BiMerchantPayOrderDaily> lambdaQuery = new QueryWrapper<BiMerchantPayOrderDaily>()
                .select("IFNULL(sum(actual_money),0) as merchantPayAmount," +
                        "        IFNULL(sum(success_order_num),0) as merchantPayTransNum," +
                        "        IFNULL(sum(total_fee),0) as payFee"
                )
                .lambda();
        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiMerchantPayOrderDaily::getDateTime, req.getStartTime());
        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiMerchantPayOrderDaily::getDateTime, req.getEndTime());
        }
        BiMerchantPayOrderDaily biMerchantPayOrderDaily = baseMapper.selectOne(lambdaQuery);
        MerchantOrderOverviewDTO result = new MerchantOrderOverviewDTO();
        result.setMerchantPayAmount(biMerchantPayOrderDaily.getMerchantPayAmount());
        result.setMerchantPayTransNum(biMerchantPayOrderDaily.getMerchantPayTransNum());
        result.setPayFee(biMerchantPayOrderDaily.getPayFee());
        return result;
    }
}
