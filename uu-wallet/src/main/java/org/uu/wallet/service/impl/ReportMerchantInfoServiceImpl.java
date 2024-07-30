package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MerchantInfoReportDTO;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.common.pay.req.MerchantInfoReq;
import org.uu.wallet.service.ICollectionOrderService;
import org.uu.wallet.service.IPaymentOrderService;
import org.uu.wallet.service.IReportMerchantInfoService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ReportMerchantInfoServiceImpl extends ServiceImpl<MerchantInfoMapper, MerchantInfo> implements IReportMerchantInfoService {
     private final ICollectionOrderService collectionOrderService;
    private final IPaymentOrderService paymentOrderService;




    @Override
    public PageReturn<MerchantInfoReportDTO> listDayPage(MerchantInfoReq req) {
        QueryWrapper<CollectionOrder> queryWrapper = new QueryWrapper<>();
        QueryWrapper<PaymentOrder> queryPaymentOrderWrapper = new QueryWrapper<>();
        queryWrapper.select("date_format(create_time, '%Y-%m-%d') as dateInterval",
                "merchant_code as code",
                "merchant_type as merchantType",
                "count(1) as quantity",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then cost else 0 end) as cost",
                "sum(case when order_status ='7' then amount else 0 end) as amount"
        );
        if(!StringUtils.isBlank(req.getBeginTime())){
            queryWrapper.eq("create_time",req.getBeginTime());
            queryPaymentOrderWrapper.eq("create_time",req.getBeginTime());
        }
        if(!StringUtils.isBlank(req.getCode())){
            queryWrapper.eq("merchant_code",req.getCode());
        }
        if(!StringUtils.isBlank(req.getCurrency())){
            queryWrapper.eq("currency",req.getCurrency());
            queryPaymentOrderWrapper.eq("currency",req.getCurrency());
        }
        queryWrapper.groupBy("dateInterval","merchant_code");
        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        collectionOrderService.getBaseMapper().selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<MerchantInfoReportDTO> list = jsonArray.toJavaList(MerchantInfoReportDTO.class);

        queryPaymentOrderWrapper.select("date_format(create_time, '%Y-%m-%d') as dateInterval",
                "merchant_code as code",
                "merchant_type as merchantType",
                "count(1) as pquantity",
                "sum(case when order_status ='7' then 1 else 0 end) as pfinish",
                "sum(case when order_status ='7' then cost else 0 end) as cost",
                "sum(case when order_status ='7' then amount else 0 end) as pamount"
        );

        List<String> codeList = list.stream()
                .map(MerchantInfoReportDTO::getCode)
                .collect(Collectors.toList());
        queryPaymentOrderWrapper.in("merchant_code",codeList);
        Page<Map<String,Object>> pagePayment = new Page<>();
        pagePayment.setCurrent(req.getPageNo());
        pagePayment.setSize(req.getPageSize());
        queryWrapper.groupBy("dateInterval","merchant_code");
        paymentOrderService.getBaseMapper().selectMapsPage(pagePayment,queryPaymentOrderWrapper);
        List<Map<String,Object>> pMapList = pagePayment.getRecords();
        JSONArray pjsonArray = new JSONArray();
        jsonArray.addAll(pMapList);
        List<MerchantInfoReportDTO> plist = jsonArray.toJavaList(MerchantInfoReportDTO.class);
        Map<String,List<MerchantInfoReportDTO>> pmap = plist.stream().collect(Collectors.groupingBy(MerchantInfoReportDTO::getCode));
        list.stream().forEach(merchant-> {
                 if(pmap.get(merchant.getCode())!=null&&(pmap.get(merchant.getCode()).get(0)!=null)){
                     MerchantInfoReportDTO tmp =   pmap.get(merchant.getCode()).get(0);
                     merchant.setPamount(tmp.getPamount());
                     merchant.setPfinish(tmp.getPfinish());
                     merchant.setPquantity(tmp.getQuantity());
                     merchant.setCost(tmp.getCost().add(merchant.getCost()));
                 }
                }
        );

        return PageUtils.flush(page, list);
    }



}
