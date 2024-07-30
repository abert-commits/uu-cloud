package org.uu.job.processor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.pay.dto.MerchantActivationInfoDTO;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.entity.BiMerchantDaily;
import org.uu.manager.entity.BiMerchantMonth;
import org.uu.manager.mapper.BiMerchantDailyMapper;
import org.uu.manager.mapper.BiMerchantMonthMapper;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.entity.MerchantCollectOrders;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * 清洗商户报表数据
 *
 * @author Admin
 */
@Component("handleMerchantReportProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandleMerchantReportProcessor implements BasicProcessor {

    private final BiMerchantDailyMapper biMerchantDailyMapper;
    private final MerchantCollectOrdersMapper collectionOrderMapper;
    private final BiMerchantMonthMapper biMerchantMonthMapper;
    private final MerchantPaymentOrdersMapper paymentOrderMapper;
    private final MerchantInfoMapper merchantInfoMapper;
    private final MemberInfoMapper memberInfoMapper;

    private final RedisUtils redisUtils;

    /**
     * 批次大小
     */
    private static final int BATCH_SIZE = 1000;

    private static final String TODAY = "today";


    /**
     * 跨天时长(毫秒)
     */
    private static final int OVER_TIME = 30 * 60 * 1000;


    /**
     * dateType：today，interval
     * 1.当天执行重跑: {"dateType":"today","startTime":"2023-11-15 00:00:00", "endTime":"2023-11-15 23:59:59"}
     * 2.跨天数据处理 ：凌晨半个小时处理前天数据
     * 3.重跑一段时间数据：连续天数
     * 4.Job开关,停服开关控制,服务重启以免数据丢失
     * 5.自动跑今天数据
     *
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {

        try {
            // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
            OmsLogger omsLogger = context.getOmsLogger();
            omsLogger.info("清洗商户报表数据参数->{}", context.getJobParams());
            log.info("清洗代收订单数据参数->{}", context.getJobParams());
            String jobSwitch = (String) redisUtils.get(RedisConstants.JOB_SWITCH);
            if (!StringUtils.isEmpty(jobSwitch) && GlobalConstants.STATUS_OFF.equals(Integer.parseInt(jobSwitch))) {
                log.info("清洗商户报表数据Job未执行,Job开关已关闭.");
                return new ProcessResult(true, "Job switch turned off");
            }
            String params = !StringUtils.isEmpty(context.getJobParams()) ? context.getJobParams() : context.getInstanceParams();
            JSONObject jsonObject = StringUtils.isEmpty(params) ? new JSONObject() : (JSONObject) JSONObject.parse(params);
            String startTime = jsonObject.getString("startTime");
            String endTime = jsonObject.getString("endTime");

            String dateType = jsonObject.getString("dateType");
            String dateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).plusDays(-1), GlobalConstants.DATE_FORMAT_DAY);
            // dateType为空代表自动
            if (StringUtils.isEmpty(dateType)) {
                biMerchantDailyMapper.deleteDailyByDateTime(dateStr);
                handleMerchantCollectOrders(dateStr, startTime, endTime, dateType);
                handleWithdrawOrder(dateStr, startTime, endTime, dateType);

            } else {
                // 自定义时间
                log.info("进入自定义时间");
                LocalDateTime startDate = DateUtil.parseLocalDateTime(startTime, GlobalConstants.DATE_FORMAT);
                LocalDateTime endDate = DateUtil.parseLocalDateTime(endTime, GlobalConstants.DATE_FORMAT);
                long value = startDate.until(endDate, ChronoUnit.DAYS);

                for (int i = 0; i <= value; i++) {
                    String dateStr1 = DateUtil.format(startDate.plusDays(i), GlobalConstants.DATE_FORMAT_DAY);
                    biMerchantDailyMapper.deleteDailyByDateTime(dateStr1);
                    handleMerchantCollectOrders(dateStr1, null, null, dateType);
                    handleWithdrawOrder(dateStr1, null, null, dateType);
                }

                // 重新统计月数据
                int monthVal = CommonUtils.calculateMonthDiff(startDate, endDate);
                for (int i = 0; i <= monthVal; i++) {
                    String monthStr = DateUtil.format(startDate.plusMonths(i), GlobalConstants.DATE_FORMAT_MONTH);
                    biMerchantMonthMapper.deleteMonthByDateTime(monthStr);
                    List<BiMerchantMonth> biMerchantPaymentOrdersMonthList = biMerchantMonthMapper.selectPayDataInfoByMonth(monthStr);
                    List<MerchantActivationInfoDTO> list = memberInfoMapper.selectActiveInfoMonthList(monthStr);
                    for (BiMerchantMonth biMerchantPaymentOrdersMonth : biMerchantPaymentOrdersMonthList) {
                        biMerchantPaymentOrdersMonth.setDateTime(monthStr);
                        Optional<MerchantActivationInfoDTO> itemOpt = list.stream().filter(m -> !StringUtils.isEmpty(m.getMerchantCode()) && m.getMerchantCode().equals(biMerchantPaymentOrdersMonth.getMerchantCode())).findFirst();
                        if (itemOpt.isPresent()) {
                            biMerchantPaymentOrdersMonth.setActivationNewUser(itemOpt.get().getActivationTotalNum());
                        }
                        biMerchantMonthMapper.updateByDateTime(biMerchantPaymentOrdersMonth);
                    }


                    List<BiMerchantMonth> biMerchantWithdrawOrdersMonthList = biMerchantMonthMapper.selectWithdrawDataInfoByMonth(monthStr);
                    for (BiMerchantMonth biMerchantWithdrawOrdersMonth : biMerchantWithdrawOrdersMonthList) {
                        biMerchantWithdrawOrdersMonth.setDateTime(monthStr);
                        biMerchantMonthMapper.updateByDateTime(biMerchantWithdrawOrdersMonth);
                    }

                }

            }
        } catch (Exception e) {
            log.error("HandleMerchantPaymentOrdersProcessor.process" + e.getMessage());
        }

        return new ProcessResult(true, "return success");
    }

    private void handleMerchantCollectOrders(String dateStr, String startTime, String endTime, String dateType) throws ExecutionException, InterruptedException {
        int pageNo = 1;
        long totalSize = 0;
        Page<MerchantCollectOrders> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(BATCH_SIZE);
        // startTime <= time < endTime
        page = updateBiMerchantPaymentOrders(dateStr, startTime, endTime, page, dateType);
        List<MerchantCollectOrders> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            log.info("查询代收记录数为空,直接返回");
            return;
        }
        if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE > 0) {
            totalSize = (page.getTotal() / BATCH_SIZE) + 1;
            log.info("总记录数大于批次,且余数大于0,totalSize->{}", totalSize);
        } else if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE <= 0) {
            totalSize = (page.getTotal() / BATCH_SIZE);
            log.info("总记录数大于批次,且余数等于0,totalSize->{}", totalSize);
        }
        for (int i = 0; i < totalSize; i++) {
            pageNo++;
            page.setCurrent(pageNo);
            page.setSize(BATCH_SIZE);
            updateBiMerchantPaymentOrders(dateStr, startTime, endTime, page, dateType);
        }
    }


    @NotNull
    private Page<MerchantCollectOrders> updateBiMerchantPaymentOrders(String dateStr, String startTime, String endTime, Page<MerchantCollectOrders> page, String dateType) throws ExecutionException, InterruptedException {

        //DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.WALLET);
        String dateTmp = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_MINUTE);
        String todayStr = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY);
        LambdaQueryChainWrapper<MerchantCollectOrders> lambdaQuery2 = new LambdaQueryChainWrapper<>(collectionOrderMapper);
        lambdaQuery2.ge(MerchantCollectOrders::getCreateTime, dateStr + " 00:00:00");
        lambdaQuery2.le(MerchantCollectOrders::getCreateTime, dateStr + " 23:59:59");

        LambdaQueryChainWrapper<MerchantCollectOrders> lambdaQuery = new LambdaQueryChainWrapper<>(collectionOrderMapper);
        lambdaQuery.ge(MerchantCollectOrders::getCreateTime, dateStr + " 00:00:00");
        lambdaQuery.le(MerchantCollectOrders::getCreateTime, dateStr + " 23:59:59");

        Page<MerchantCollectOrders> finalPage = page;
        CompletableFuture<Page<MerchantCollectOrders>> merchantCollectOrdersFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.selectPage(finalPage, lambdaQuery2.getWrapper());
        });

        // 查询订单数量
        CompletableFuture<List<MerchantCollectOrders>> totalFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.selectCountGroupByCode(dateStr + " 00:00:00", dateStr + " 23:59:59");
        });

        CompletableFuture<List<MerchantActivationInfoDTO>> merchantActivationFuture = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectActiveInfoList(dateStr);
        });
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(merchantCollectOrdersFuture, merchantActivationFuture, totalFuture);
        allFutures.get();
        page = merchantCollectOrdersFuture.get();
        List<MerchantCollectOrders> totalList = totalFuture.get();
        List<MerchantActivationInfoDTO> resultList = merchantActivationFuture.get();
        log.info("清洗商户报表订单总记录数->{}", page.getRecords().size());
        List<MerchantCollectOrders> records = page.getRecords();
        Map<String, List<MerchantCollectOrders>> collectionOrderMap =
                records.stream().collect(Collectors.groupingBy(MerchantCollectOrders::getMerchantCode));
        log.info("总记录数分组对象->{}", JSON.toJSON(collectionOrderMap));
        if (!CollectionUtils.isEmpty(collectionOrderMap)) {

            for (String key : collectionOrderMap.keySet()) {

                BiMerchantDaily biMerchantPaymentOrders = new BiMerchantDaily();
                List<MerchantCollectOrders> collectionOrderList = collectionOrderMap.get(key);
                log.info("商户code{},总记录数->{}", key, collectionOrderList.size());
                for (MerchantCollectOrders collectionOrder : collectionOrderList) {
                    if (collectionOrder.getOrderStatus().equals(CollectionOrderStatusEnum.PAID.getCode())) {
                        biMerchantPaymentOrders.setTotalFee(biMerchantPaymentOrders.getTotalFee().add(collectionOrder.getCost()));
                        // 计算订单状态为成功数量
                        biMerchantPaymentOrders.setPaySuccessOrderNum(biMerchantPaymentOrders.getPaySuccessOrderNum() + 1);
                        biMerchantPaymentOrders.setPayMoney(biMerchantPaymentOrders.getPayMoney().add(collectionOrder.getAmount()));
                        biMerchantPaymentOrders.setDifference(biMerchantPaymentOrders.getDifference().add(collectionOrder.getAmount()));
                    }
                }
                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(key);
                if (ObjectUtils.isNotEmpty(totalList)) {
                    for (MerchantCollectOrders item : totalList) {
                        if (item.getMerchantCode().equals(key)) {
                            biMerchantPaymentOrders.setPayOrderNum(item.getAmount().longValue());
                        }
                    }

                }
                biMerchantPaymentOrders.setDateTime(dateStr);
                biMerchantPaymentOrders.setMerchantCode(key);
                biMerchantPaymentOrders.setMerchantType(merchantInfo.getMerchantType());
                biMerchantPaymentOrders.setMerchantName(merchantInfo.getUsername());

                // 自动跑今天数据endTime不为空,手动跑历史数据endTime为空
                if (!StringUtils.isEmpty(endTime)) {
                    biMerchantPaymentOrders.setLastMinute(endTime);
                }
                //DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.MANAGER);
                // dateType 为空为自动执行
                if (StringUtils.isEmpty(dateType)) {
                    biMerchantDailyMapper.updateByDateTime(biMerchantPaymentOrders);
                    // 更新月表
                    LocalDateTime dateTime = DateUtil.parseLocalDateTime(dateStr, GlobalConstants.DATE_FORMAT_DAY);
                    biMerchantPaymentOrders.setDateTime(DateUtil.format(dateTime, GlobalConstants.DATE_FORMAT_MONTH));
                    BiMerchantMonth biMerchantPaymentOrdersMonth = BeanUtil.toBean(biMerchantPaymentOrders, BiMerchantMonth.class);

                    log.info("清洗商户报表月对象->{}", JSONObject.toJSONString(biMerchantPaymentOrdersMonth));
                    biMerchantMonthMapper.updateByDateTime(biMerchantPaymentOrdersMonth);

                } else {
                    // 只有手动跑当日数据才更新 lastMinute
                    if (todayStr.equals(dateStr)) {
                        biMerchantPaymentOrders.setLastMinute(dateTmp);
                    }
                    biMerchantDailyMapper.updateByDateTime(biMerchantPaymentOrders);
                }

            }

        }
        return page;
    }


    public void handleWithdrawOrder(String dateStr, String startTime, String endTime, String dateType) {
        int pageNo = 1;
        long totalSize = 0;
        Page<MerchantPaymentOrders> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(BATCH_SIZE);
        // startTime <= time < endTime
        page = updateBiWithdrawOrder(dateStr, startTime, endTime, page, dateType);
        List<MerchantPaymentOrders> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            log.info("查询代付记录数为空,直接返回");
            return;
        }
        if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE > 0) {
            totalSize = (page.getTotal() / BATCH_SIZE) + 1;
            log.info("总记录数大于批次,且余数大于0,totalSize->{}", totalSize);
        } else if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE <= 0) {
            totalSize = (page.getTotal() / BATCH_SIZE);
            log.info("总记录数大于批次,且余数等于0,totalSize->{}", totalSize);
        }
        for (int i = 0; i < totalSize; i++) {
            pageNo++;
            page.setCurrent(pageNo);
            page.setSize(BATCH_SIZE);
            updateBiWithdrawOrder(dateStr, startTime, endTime, page, dateType);
        }
    }

    @NotNull
    public Page<MerchantPaymentOrders> updateBiWithdrawOrder(String dateStr, String startTime, String endTime, Page<MerchantPaymentOrders> page, String dateType) {
        String dateTmp = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_MINUTE);
        LambdaQueryChainWrapper<MerchantPaymentOrders> lambdaQuery2 = new LambdaQueryChainWrapper<>(paymentOrderMapper);
        String todayStr = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY);
        lambdaQuery2.ge(MerchantPaymentOrders::getCreateTime, dateStr + " 00:00:00");
        lambdaQuery2.le(MerchantPaymentOrders::getCreateTime, dateStr + " 23:59:59");

        LambdaQueryChainWrapper<MerchantPaymentOrders> lambdaQuery = new LambdaQueryChainWrapper<>(paymentOrderMapper);
        lambdaQuery.ge(MerchantPaymentOrders::getCreateTime, dateStr + " 00:00:00");
        lambdaQuery.le(MerchantPaymentOrders::getCreateTime, dateStr + " 23:59:59");
        List<MerchantPaymentOrders> totalList = paymentOrderMapper.selectCountGroupByCode(dateStr + " 00:00:00", dateStr + " 23:59:59");
        page = paymentOrderMapper.selectPage(page, lambdaQuery2.getWrapper());
        log.info("清洗商户报表总记录数->{}", page.getRecords().size());
        List<MerchantPaymentOrders> records = page.getRecords();
        Map<String, List<MerchantPaymentOrders>> paymentOrderMap =
                records.stream().collect(Collectors.groupingBy(MerchantPaymentOrders::getMerchantCode));

        if (!CollectionUtils.isEmpty(paymentOrderMap)) {

            for (String key : paymentOrderMap.keySet()) {
                BiMerchantDaily biWithdrawOrder = new BiMerchantDaily();
                List<MerchantPaymentOrders> paymentOrderList = paymentOrderMap.get(key);
                log.info("商户code{},总记录数->{}", key, paymentOrderList.size());
                for (MerchantPaymentOrders paymentOrder : paymentOrderList) {
                    if (paymentOrder.getOrderStatus().equals(CollectionOrderStatusEnum.PAID.getCode())) {
                        biWithdrawOrder.setWithdrawMoney(biWithdrawOrder.getWithdrawMoney().add(paymentOrder.getAmount()));
                        biWithdrawOrder.setWithdrawSuccessOrderNum(biWithdrawOrder.getWithdrawSuccessOrderNum() + 1);
                        biWithdrawOrder.setTotalFee(biWithdrawOrder.getTotalFee().add(paymentOrder.getCost()));
                        biWithdrawOrder.setDifference(biWithdrawOrder.getDifference().add(paymentOrder.getAmount()));
                    }
                }
                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(key);
                if (ObjectUtils.isNotEmpty(totalList)) {
                    for (MerchantPaymentOrders item : totalList) {
                        if (item.getMerchantCode().equals(key)) {
                            biWithdrawOrder.setWithdrawOrderNum(item.getAmount().longValue());
                        }
                    }
                }
                biWithdrawOrder.setDateTime(dateStr);
                biWithdrawOrder.setMerchantCode(key);
                biWithdrawOrder.setMerchantType(merchantInfo.getMerchantType());
                biWithdrawOrder.setMerchantName(merchantInfo.getUsername());
                // 自动跑今天数据endTime不为空,手动跑历史数据endTime为空
                if (!StringUtils.isEmpty(endTime)) {
                    biWithdrawOrder.setLastMinute(endTime);
                }

                // dateType 为空为自动执行
                if (StringUtils.isEmpty(dateType)) {
                    biMerchantDailyMapper.updateWithdrawByDateTime(biWithdrawOrder);
                    // 更新月表,自动跑是累积方式，所以也需要累积月表数据
                    LocalDateTime dateTime = DateUtil.parseLocalDateTime(dateStr, GlobalConstants.DATE_FORMAT_DAY);
                    biWithdrawOrder.setDateTime(DateUtil.format(dateTime, GlobalConstants.DATE_FORMAT_MONTH));
                    BiMerchantMonth biWithdrawOrderMonth = BeanUtil.toBean(biWithdrawOrder, BiMerchantMonth.class);
                    biMerchantMonthMapper.updateWithdrawByDateTime(biWithdrawOrderMonth);

                } else {
                    // 只有手动跑当日数据才更新 lastMinute
                    if (todayStr.equals(dateStr)) {
                        biWithdrawOrder.setLastMinute(dateTmp);
                    }
                    biMerchantDailyMapper.updateWithdrawByDateTime(biWithdrawOrder);
                }

            }


        }
        return page;
    }
}
