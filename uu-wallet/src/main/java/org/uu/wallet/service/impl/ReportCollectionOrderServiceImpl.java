package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CollectionOrderDTO;
import org.uu.common.pay.req.CollectionOrderReq;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.mapper.AccountChangeMapper;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.service.IMatchingOrderService;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.service.IPaymentOrderService;
import org.uu.wallet.service.IReportCollectionOrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Service
public class ReportCollectionOrderServiceImpl extends ServiceImpl<CollectionOrderMapper, CollectionOrder> implements IReportCollectionOrderService {

    private final RedisUtils redisUtils;
    private final RedissonUtil redissonUtil;
    private final IMerchantInfoService merchantInfoService;
    private final IPaymentOrderService paymentOrderService;
    private final IMatchingOrderService matchingOrderService;
    private final RabbitTemplate rabbitTemplate;
    private final MerchantInfoMapper merchantInfoMapper;
    private final AccountChangeMapper accountChangeMapper;
    private final WalletMapStruct  walletMapStruct;

//    @Autowired
//    private MerchantSendRecommendAmount merchantSendRecommendAmount;

    @Override
    public PageReturn<CollectionOrderDTO> listDayPage(CollectionOrderReq req) {
        QueryWrapper<CollectionOrder> queryWrapper = new QueryWrapper<>();

            queryWrapper.select("date_format(create_time, '%Y-%m-%d') as dateInterval",
                            "count(1) as quantity",
                            "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                            "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                            "sum(case when order_status ='7' then 1 else 0 end) as finish",
                            "sum(case when order_status ='7' then amount else 0 end) as amount",
                            "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                            "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
                            );
                    if(!StringUtils.isBlank(req.getBeginTime())){
                        queryWrapper.eq("create_time",req.getBeginTime());
                    }

        queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<CollectionOrderDTO> list = jsonArray.toJavaList(CollectionOrderDTO.class);
        return PageUtils.flush(page, list);
    }



    @Override
    public PageReturn<CollectionOrderDTO> listMothPage(CollectionOrderReq req) {

        QueryWrapper<CollectionOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("date_format(create_time, '%Y-%m') as dateInterval",
                "count(1) as quantity",
                "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then amount else 0 end) as amount",
                "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
        );
        if(!StringUtils.isBlank(req.getBeginTime())){
            queryWrapper.eq("create_time",req.getBeginTime());
        }

        queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<CollectionOrderDTO> list = jsonArray.toJavaList(CollectionOrderDTO.class);
        return PageUtils.flush(page, list);
    }





    @Override
    public PageReturn<CollectionOrderDTO> listDayPageTotal(CollectionOrderReq req) {
        QueryWrapper<CollectionOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(
                "count(1) as quantity",
                "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then amount else 0 end) as amount",
                "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
        );
        if(!StringUtils.isBlank(req.getBeginTime())){
            queryWrapper.eq("create_time",req.getBeginTime());
        }

        //queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<CollectionOrderDTO> list = jsonArray.toJavaList(CollectionOrderDTO.class);
        return PageUtils.flush(page, list);
    }



    @Override
    public PageReturn<CollectionOrderDTO> listMothPageTotal(CollectionOrderReq req) {

        QueryWrapper<CollectionOrder> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(
                "count(1) as quantity",
                "sum(case when order_status ='8' then 1 else 0 end) as cancel",
                "sum(case when order_status ='6' then 1 else 0 end) as shensu",
                "sum(case when order_status ='7' then 1 else 0 end) as finish",
                "sum(case when order_status ='7' then amount else 0 end) as amount",
                "sum(case when order_status ='7' then order_rate else 0 end) as orderRate",
                "sum(case when order_status ='7' then complete_duration else 0 end) as completeDuration"
        );
        if(!StringUtils.isBlank(req.getBeginTime())){
            queryWrapper.eq("create_time",req.getBeginTime());
        }

      //  queryWrapper.groupBy("dateInterval");

        Page<Map<String,Object>> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        baseMapper.selectMapsPage(page,queryWrapper);
        List<Map<String,Object>> records = page.getRecords();
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(records);
        List<CollectionOrderDTO> list = jsonArray.toJavaList(CollectionOrderDTO.class);
        return PageUtils.flush(page, list);
    }



}
