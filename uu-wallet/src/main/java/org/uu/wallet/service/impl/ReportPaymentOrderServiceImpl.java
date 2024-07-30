package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.PaymentOrderDTO;
import org.uu.common.pay.req.PaymentOrderReq;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.service.IReportPaymentOrderService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
public class ReportPaymentOrderServiceImpl extends ServiceImpl<PaymentOrderMapper, PaymentOrder> implements IReportPaymentOrderService {
    private final IMerchantInfoService merchantInfoService;


    @Override
    public PageReturn<PaymentOrderDTO> listDayPage(PaymentOrderReq req) {

        QueryWrapper<PaymentOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("date_format(create_time, '%Y-%m-%d') as dateInterval",
                "count(1) as quantity",
                "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then amount else 0 end) as amount",
                "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
        );
        if(!StringUtils.isBlank(req.getQueryTime())){
            queryWrapper.eq("create_time",req.getQueryTime());
        }

        queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<PaymentOrderDTO> list = jsonArray.toJavaList(PaymentOrderDTO.class);
        return PageUtils.flush(page, list);


    }


    @Override
    public PageReturn<PaymentOrderDTO> listMothPage(PaymentOrderReq req) {

        QueryWrapper<PaymentOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("date_format(create_time, '%Y-%m-%d') as dateInterval",
                "count(1) as quantity",
                "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then amount else 0 end) as amount",
                "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
        );
        if(!StringUtils.isBlank(req.getQueryTime())){
            queryWrapper.eq("create_time",req.getQueryTime());
        }

        queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<PaymentOrderDTO> list = jsonArray.toJavaList(PaymentOrderDTO.class);
        return PageUtils.flush(page, list);


    }

   public PageReturn<PaymentOrderDTO> listDayTotal(PaymentOrderReq req){

        QueryWrapper<PaymentOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("date_format(create_time, '%Y-%m-%d') as dateInterval",
                "count(1) as quantity",
                "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then amount else 0 end) as amount",
                "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
        );
        if(!StringUtils.isBlank(req.getQueryTime())){
            queryWrapper.eq("create_time",req.getQueryTime());
        }

        queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<PaymentOrderDTO> list = jsonArray.toJavaList(PaymentOrderDTO.class);
        return PageUtils.flush(page, list);

    }

   public PageReturn<PaymentOrderDTO> listMothTotal(PaymentOrderReq req){

        QueryWrapper<PaymentOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("date_format(create_time, '%Y-%m-%d') as dateInterval",
                "count(1) as quantity",
                "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then amount else 0 end) as amount",
                "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
        );
        if(!StringUtils.isBlank(req.getQueryTime())){
            queryWrapper.eq("create_time",req.getQueryTime());
        }

        queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<PaymentOrderDTO> list = jsonArray.toJavaList(PaymentOrderDTO.class);
        return PageUtils.flush(page, list);

    }




}
