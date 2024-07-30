package org.uu.manager.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.pay.dto.BiOverViewStatisticsDailyDTO;
import org.uu.common.pay.enums.OrderEventEnum;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.manager.entity.BiMerchantDaily;
import org.uu.manager.entity.BiOverViewStatisticsDaily;
import org.uu.manager.mapper.BiMerchantDailyMapper;
import org.uu.manager.mapper.BiOverViewStatisticsDailyMapper;
import org.uu.manager.service.IBiOverViewStatisticsDailyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 首页订单统计 服务实现类
 * </p>
 *
 * @author
 * @since 2024-07-13
 */
@Service
@Slf4j
public class BiOverViewStatisticsDailyServiceImpl extends ServiceImpl<BiOverViewStatisticsDailyMapper, BiOverViewStatisticsDaily> implements IBiOverViewStatisticsDailyService {

    @Resource
    BiMerchantDailyMapper biMerchantDailyMapper;
    @Resource
    RedissonUtil redissonUtil;

    @Override
    public boolean statisticsDaily(OrderEventReq req) {
        String key = "ar-manager-statisticsDaily";
        RLock lock = redissonUtil.getLock(key);
        boolean lockStatus = false;

        try {
            lockStatus = lock.tryLock(10, TimeUnit.SECONDS);
            if(lockStatus){
                // 获取今天日期
                String todayStr = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY);
                BiOverViewStatisticsDaily todayData = lambdaQuery().eq(BiOverViewStatisticsDaily::getDateTime, todayStr).one();

                if (ObjectUtils.isEmpty(todayData)) {
                    Long id = initTodayData(todayStr);
                    todayData.setId(id);
                }

                // 商户充值订单申请
                if (req.getEventId().equals(OrderEventEnum.MERCHANT_COLLECTION_ORDER_APPLICATION.getCode())) {
                    todayData.setMerchantApplicationCollectionOrderNum(todayData.getMerchantApplicationCollectionOrderNum() + 1);
                }
                // 商户充值订单匹配
                if (req.getEventId().equals(OrderEventEnum.MERCHANT_COLLECTION_ORDER_MATCHING.getCode())) {
                    String params = req.getParams();
                    JSONObject jsonObject = JSONObject.parseObject(params);
                    String amountStr = jsonObject.getString("amount");
                    BigDecimal amountValue = ObjectUtils.isEmpty(amountStr) ? BigDecimal.ZERO : new BigDecimal(amountStr);

                    todayData.setMerchantInitiateCollectionOrderNum(todayData.getMerchantInitiateCollectionOrderNum() + 1);
                    todayData.setMerchantInitiateCollectionOrderAmount(todayData.getMerchantInitiateCollectionOrderAmount().add((amountValue)));
                }
                // 商户充值订单完成
                if (req.getEventId().equals(OrderEventEnum.MERCHANT_COLLECTION_ORDER_SUCCESS.getCode())) {
                    String params = req.getParams();
                    JSONObject jsonObject = JSONObject.parseObject(params);
                    String commissionStr = jsonObject.getString("commission");
                    String amountStr = jsonObject.getString("amount");
                    BigDecimal commission = ObjectUtils.isEmpty(commissionStr) ? BigDecimal.ZERO : new BigDecimal(commissionStr);
                    BigDecimal amount = ObjectUtils.isEmpty(amountStr) ? BigDecimal.ZERO : new BigDecimal(amountStr);

                    todayData.setMerchantSuccessCollectionOrderNum(todayData.getMerchantSuccessCollectionOrderNum() + 1);
                    todayData.setMerchantSuccessCollectionCommission(todayData.getMerchantSuccessCollectionCommission().add(commission));
                    todayData.setMerchantSuccessCollectionAmount(todayData.getMerchantSuccessCollectionAmount().add(amount));
                }

                // 商户提现订单申请
                if (req.getEventId().equals(OrderEventEnum.MERCHANT_PAYMENT_ORDER_APPLICATION.getCode())) {
                    todayData.setMerchantApplicationPaymentOrderNum(todayData.getMerchantApplicationPaymentOrderNum() + 1);
                }
                // 商户提现订单匹配
                if (req.getEventId().equals(OrderEventEnum.MERCHANT_PAYMENT_ORDER_MATCHING.getCode())) {
                    String params = req.getParams();
                    JSONObject jsonObject = JSONObject.parseObject(params);
                    String amountStr = jsonObject.getString("amount");
                    BigDecimal amountValue = ObjectUtils.isEmpty(amountStr) ? BigDecimal.ZERO : new BigDecimal(amountStr);
                    todayData.setMerchantInitiatePaymentOrderNum(todayData.getMerchantInitiatePaymentOrderNum() + 1);
                    todayData.setMerchantInitiatePaymentOrderAmount(todayData.getMerchantInitiatePaymentOrderAmount().add(amountValue));
                }
                // 商户提现订单完成
                if (req.getEventId().equals(OrderEventEnum.MERCHANT_PAYMENT_ORDER_SUCCESS.getCode())) {
                    String params = req.getParams();
                    JSONObject jsonObject = JSONObject.parseObject(params);
                    String commissionStr = jsonObject.getString("commission");
                    String amountStr = jsonObject.getString("amount");
                    BigDecimal commission = ObjectUtils.isEmpty(commissionStr) ? BigDecimal.ZERO : new BigDecimal(commissionStr);
                    BigDecimal amount = ObjectUtils.isEmpty(amountStr) ? BigDecimal.ZERO : new BigDecimal(amountStr);

                    todayData.setMerchantSuccessPaymentOrderNum(todayData.getMerchantSuccessPaymentOrderNum() + 1);
                    todayData.setMerchantSuccessPaymentCommission(todayData.getMerchantSuccessPaymentCommission().add(commission));
                    todayData.setMerchantSuccessPaymentAmount(todayData.getMerchantSuccessPaymentAmount().add(amount));
                }
                int i = baseMapper.updateById(todayData);
                return i == 1;
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("统计首页参数失败 错误信息:{}, req:{}", e, req);
        }finally {
            //释放锁
            if (lockStatus && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }


    @Override
    public boolean statisticsMerchantDaily(OrderEventReq req) {
        try {
            // 获取今天日期
            String todayStr = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY);
            LambdaQueryWrapper<BiMerchantDaily> todayQueryWrapper = new LambdaQueryWrapper<>();
            todayQueryWrapper.eq(BiMerchantDaily::getDateTime, todayStr);
            BiMerchantDaily biMerchantDailyData = biMerchantDailyMapper.selectOne(todayQueryWrapper);

            if (ObjectUtils.isEmpty(biMerchantDailyData)) {
                Long id = initTodayMerchantDailyData(todayStr);
                biMerchantDailyData.setId(id);
            }

            BiMerchantDaily biMerchantDaily = new BiMerchantDaily();

            // 商户充值订单申请
            if (req.getEventId().equals(OrderEventEnum.MERCHANT_COLLECTION_ORDER_APPLICATION.getCode())) {
                biMerchantDaily.setPayOrderNum(biMerchantDailyData.getPayOrderNum() + 1);
            }

            // 商户充值订单匹配
            if (req.getEventId().equals(OrderEventEnum.MERCHANT_COLLECTION_ORDER_MATCHING.getCode())) {
                String params = req.getParams();
                JSONObject jsonObject = JSONObject.parseObject(params);
                String amountStr = jsonObject.getString("amount");
                BigDecimal amountValue = ObjectUtils.isEmpty(amountStr) ? BigDecimal.ZERO : new BigDecimal(amountStr);

                biMerchantDaily.setPaySuccessOrderNum(biMerchantDailyData.getPaySuccessOrderNum() + 1);
                biMerchantDaily.setPayMoney(biMerchantDailyData.getPayMoney().add(amountValue));
            }
            // 商户充值订单完成
            if (req.getEventId().equals(OrderEventEnum.MERCHANT_COLLECTION_ORDER_SUCCESS.getCode())) {
                biMerchantDaily.setWithdrawSuccessOrderNum(biMerchantDailyData.getWithdrawSuccessOrderNum() + 1);
                String params = req.getParams();
                JSONObject jsonObject = JSONObject.parseObject(params);
                BigDecimal commission = (BigDecimal) jsonObject.getOrDefault("commission", new BigDecimal(0));
                BigDecimal amount = (BigDecimal) jsonObject.getOrDefault("amount", new BigDecimal(0));
                biMerchantDaily.setCollectionFee(biMerchantDailyData.getCollectionFee().add(commission));
                biMerchantDaily.setCollectionInitiationAmount(biMerchantDailyData.getCollectionInitiationAmount().add(amount));
            }

            // 商户提现订单申请
            if (req.getEventId().equals(OrderEventEnum.MERCHANT_PAYMENT_ORDER_APPLICATION.getCode())) {
                biMerchantDaily.setWithdrawOrderNum(biMerchantDailyData.getWithdrawOrderNum() + 1);
            }

            // 商户提现订单匹配
            if (req.getEventId().equals(OrderEventEnum.MERCHANT_PAYMENT_ORDER_MATCHING.getCode())) {
                String params = req.getParams();
                JSONObject jsonObject = JSONObject.parseObject(params);
                String amountStr = jsonObject.getString("amount");
                BigDecimal amountValue = ObjectUtils.isEmpty(amountStr) ? BigDecimal.ZERO : new BigDecimal(amountStr);
                biMerchantDaily.setWithdrawSuccessOrderNum(biMerchantDailyData.getWithdrawSuccessOrderNum() + 1);
                biMerchantDaily.setWithdrawMoney(biMerchantDailyData.getWithdrawMoney().add(amountValue));
            }

            // 商户提现订单完成
            if (req.getEventId().equals(OrderEventEnum.MERCHANT_PAYMENT_ORDER_SUCCESS.getCode())) {
                biMerchantDaily.setPaySuccessOrderNum(biMerchantDailyData.getPaySuccessOrderNum() + 1);
                String params = req.getParams();
                JSONObject jsonObject = JSONObject.parseObject(params);
                BigDecimal commission = (BigDecimal) jsonObject.getOrDefault("commission", new BigDecimal(0));
                BigDecimal amount = (BigDecimal) jsonObject.getOrDefault("amount", new BigDecimal(0));
                biMerchantDaily.setPaymentFee(biMerchantDailyData.getPaymentFee().add(commission));
                biMerchantDaily.setPaymentInitiationAmount(biMerchantDailyData.getPaymentInitiationAmount().add(amount));
            }

            biMerchantDaily.setCreateTime(LocalDateTime.now(ZoneId.systemDefault()));
            biMerchantDaily.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
            //保存商户日报表
            int insert = biMerchantDailyMapper.insert(biMerchantDaily);
            if (insert > 0) {
                return true;
            }
        } catch (Exception e) {
            log.error("商户日报表统计失败 错误信息:{},req:{}", e, req);
        }
        return false;
    }

    @Override
    public BiOverViewStatisticsDailyDTO getDataByDate(String start, String end) {

        LambdaQueryWrapper<BiOverViewStatisticsDaily> queryWrapper = new QueryWrapper<BiOverViewStatisticsDaily>()
                .select("sum(merchant_application_payment_order_num) as merchantApplicationPaymentOrderNum,\n" +
                        "                sum(merchant_initiate_payment_order_num) as merchantInitiatePaymentOrderNum,\n" +
                        "                sum(merchant_initiate_payment_order_amount) as merchantInitiatePaymentOrderAmount,\n" +
                        "                sum(merchant_success_payment_order_num) as merchantSuccessPaymentOrderNum,\n" +
                        "                sum(merchant_success_payment_amount) as merchantSuccessPaymentAmount,\n" +
                        "                sum(merchant_success_payment_commission) as merchantSuccessPaymentCommission,\n" +
                        "                sum(merchant_application_collection_order_num) as merchantApplicationCollectionOrderNum,\n" +
                        "                sum(merchant_initiate_collection_order_num) as merchantInitiateCollectionOrderNum,\n" +
                        "                sum(merchant_initiate_collection_order_amount) as merchantInitiateCollectionOrderAmount,\n" +
                        "                sum(merchant_success_collection_order_num) as merchantSuccessCollectionOrderNum,\n" +
                        "                sum(merchant_success_collection_amount) as merchantSuccessCollectionAmount,\n" +
                        "                sum(merchant_success_collection_commission) as merchantSuccessCollectionCommission").lambda();
        if (ObjectUtils.isNotEmpty(start)) {
            queryWrapper.ge(BiOverViewStatisticsDaily::getDateTime, start);
        }

        if (ObjectUtils.isNotEmpty(end)) {
            queryWrapper.le(BiOverViewStatisticsDaily::getDateTime, end);
        }
        BiOverViewStatisticsDaily baseData = baseMapper.selectOne(queryWrapper);
        BiOverViewStatisticsDailyDTO dto = new BiOverViewStatisticsDailyDTO();
        if (ObjectUtils.isEmpty(baseData)) {
            baseData = new BiOverViewStatisticsDaily();
            BeanUtil.copyProperties(baseData, dto);
            return dto;
        }
        BeanUtil.copyProperties(baseData, dto);
        // 代收匹配成功率
        Integer merchantApplicationCollectionOrderNum = dto.getMerchantApplicationCollectionOrderNum();
        Integer merchantInitiateCollectionOrderNum = dto.getMerchantInitiateCollectionOrderNum();
        Integer merchantSuccessCollectionOrderNum = dto.getMerchantSuccessCollectionOrderNum();
        dto.setMerchantCollectionMatchingSuccessRate(BigDecimal.ZERO);
        dto.setMerchantCollectionSuccessRate(BigDecimal.ZERO);
        if (merchantApplicationCollectionOrderNum != 0 && merchantInitiateCollectionOrderNum != 0) {
            dto.setMerchantCollectionMatchingSuccessRate(new BigDecimal(merchantInitiateCollectionOrderNum).divide(new BigDecimal(merchantApplicationCollectionOrderNum), 2, RoundingMode.DOWN));
        }
        // 代收成功率=代收成功订单数/代收发起订单数
        if (merchantSuccessCollectionOrderNum != 0 && merchantInitiateCollectionOrderNum != 0) {
            dto.setMerchantCollectionSuccessRate(new BigDecimal(merchantSuccessCollectionOrderNum).divide(new BigDecimal(merchantInitiateCollectionOrderNum), 2, RoundingMode.DOWN));
        }

        // 代付匹配成功率
        Integer merchantApplicationPaymentOrderNum = dto.getMerchantApplicationPaymentOrderNum();
        Integer merchantInitiatePaymentOrderNum = dto.getMerchantInitiatePaymentOrderNum();
        Integer merchantSuccessPaymentOrderNum = dto.getMerchantSuccessPaymentOrderNum();
        dto.setMerchantPaymentMatchingSuccessRate(BigDecimal.ZERO);
        dto.setMerchantPaymentSuccessRate(BigDecimal.ZERO);
        if (merchantInitiatePaymentOrderNum != 0 && merchantApplicationPaymentOrderNum != 0) {
            dto.setMerchantPaymentMatchingSuccessRate(new BigDecimal(merchantInitiatePaymentOrderNum).divide(new BigDecimal(merchantApplicationPaymentOrderNum), 2, RoundingMode.DOWN));
        }
        // 代付成功率=代付成功订单数/代付发起订单数
        if (merchantSuccessPaymentOrderNum != 0 && merchantInitiatePaymentOrderNum != 0) {
            dto.setMerchantPaymentSuccessRate(new BigDecimal(merchantSuccessPaymentOrderNum).divide(new BigDecimal(merchantInitiatePaymentOrderNum), 2, RoundingMode.DOWN));
        }
        BigDecimal merchantSuccessCollectionAmount = dto.getMerchantSuccessCollectionAmount();
        BigDecimal merchantSuccessPaymentAmount = dto.getMerchantSuccessPaymentAmount();
        dto.setMerchantCollectionAvgAmount(BigDecimal.ZERO);
        dto.setMerchantPaymentAvgAmount(BigDecimal.ZERO);
        // 代收平均金额
        if (merchantSuccessCollectionAmount.compareTo(BigDecimal.ZERO) != 0 && merchantSuccessCollectionOrderNum != 0) {
            dto.setMerchantCollectionAvgAmount(merchantSuccessCollectionAmount.divide(new BigDecimal(merchantSuccessCollectionOrderNum), 2, RoundingMode.DOWN));
        }
        // 代付平均金额
        if (merchantSuccessPaymentAmount.compareTo(BigDecimal.ZERO) != 0 && merchantSuccessPaymentOrderNum != 0) {
            dto.setMerchantPaymentAvgAmount(merchantSuccessPaymentAmount.divide(new BigDecimal(merchantSuccessPaymentOrderNum), 2, RoundingMode.DOWN));
        }
        // 代收成功金额占比
        BigDecimal merchantInitiateCollectionOrderAmount = dto.getMerchantInitiateCollectionOrderAmount();
        dto.setMerchantCollectionAmountProportion(BigDecimal.ZERO);
        if (merchantSuccessCollectionAmount.compareTo(BigDecimal.ZERO) != 0 && merchantInitiateCollectionOrderAmount.compareTo(BigDecimal.ZERO) != 0) {
            dto.setMerchantCollectionAmountProportion(merchantSuccessCollectionAmount.divide(merchantInitiateCollectionOrderAmount, 2, RoundingMode.DOWN));
        }
        // 代付成功金额占比
        BigDecimal merchantInitiatePaymentOrderAmount = dto.getMerchantInitiatePaymentOrderAmount();
        dto.setMerchantPaymentAmountProportion(BigDecimal.ZERO);
        if (merchantSuccessPaymentAmount.compareTo(BigDecimal.ZERO) != 0 && merchantInitiatePaymentOrderAmount.compareTo(BigDecimal.ZERO) != 0) {
            dto.setMerchantPaymentAmountProportion(merchantSuccessPaymentAmount.divide(merchantInitiatePaymentOrderAmount, 2, RoundingMode.DOWN));
        }
        dto.setPlatformInitiateOrderNum(dto.getMerchantInitiateCollectionOrderNum() + dto.getMerchantInitiatePaymentOrderNum());
        dto.setPlatformSuccessOrderNum(dto.getMerchantSuccessCollectionOrderNum() + dto.getMerchantSuccessPaymentOrderNum());
        dto.setPlatformSuccessRate(BigDecimal.ZERO);
        if (dto.getMerchantPaymentSuccessRate().compareTo(BigDecimal.ZERO) != 0 || dto.getMerchantCollectionSuccessRate().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal rate = dto.getMerchantCollectionSuccessRate().add(dto.getMerchantPaymentSuccessRate());
            dto.setPlatformSuccessRate(rate.divide(new BigDecimal(2), 2, RoundingMode.DOWN));
        }
        dto.setPlatformSuccessAmount(dto.getMerchantSuccessCollectionAmount().add(dto.getMerchantSuccessPaymentAmount()));
        dto.setPlatformAvgAmount(BigDecimal.ZERO);
        if (dto.getMerchantPaymentAvgAmount().compareTo(BigDecimal.ZERO) != 0 || dto.getMerchantCollectionAvgAmount().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal avgTotal = dto.getMerchantPaymentAvgAmount().add(dto.getMerchantCollectionAvgAmount());
            dto.setPlatformAvgAmount(avgTotal.divide(new BigDecimal(2), 2, RoundingMode.DOWN));
        }
        dto.setPlatformOrderAmountProportion(BigDecimal.ZERO);
        if (dto.getMerchantPaymentAmountProportion().compareTo(BigDecimal.ZERO) != 0 || dto.getMerchantCollectionAmountProportion().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal totalProportion = dto.getMerchantPaymentAmountProportion().add(dto.getMerchantCollectionAmountProportion());
            dto.setPlatformOrderAmountProportion(totalProportion.divide(new BigDecimal(2), 2, RoundingMode.DOWN));
        }
        dto.setPlatformOrderCommission(dto.getMerchantSuccessPaymentCommission().add(dto.getMerchantSuccessCollectionCommission()));
        return dto;
    }


    private Long initTodayData(String todayStr) {
        BiOverViewStatisticsDaily todayData = new BiOverViewStatisticsDaily();
        todayData.setDateTime(todayStr);
        baseMapper.insert(todayData);
        return todayData.getId();
    }

    private Long initTodayMerchantDailyData(String todayStr) {
        BiMerchantDaily todayData = new BiMerchantDaily();
        todayData.setDateTime(todayStr);
        biMerchantDailyMapper.insert(todayData);
        return todayData.getId();
    }
}
