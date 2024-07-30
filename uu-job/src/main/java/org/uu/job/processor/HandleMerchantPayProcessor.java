package org.uu.job.processor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.entity.BiMerchantPayOrderDaily;
import org.uu.manager.entity.BiPaymentOrderMonth;
import org.uu.manager.mapper.BiMerchantPayOrderDailyMapper;
import org.uu.manager.mapper.BiMerchantPayOrderMonthMapper;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;
import org.uu.wallet.entity.MerchantCollectOrders;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


/**
 * 清洗商户代收订单数据
 *
 * @author Admin
 */
@Component("handleMerchantPayProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandleMerchantPayProcessor implements BasicProcessor {

    private final BiMerchantPayOrderDailyMapper biPaymentOrderMapper;
    private final MerchantCollectOrdersMapper collectionOrderMapper;
    private final BiMerchantPayOrderMonthMapper biPaymentOrderMonthMapper;

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
            omsLogger.info("清洗代收订单数据参数->{}", context.getJobParams());
            log.info("清洗代收订单数据参数->{}", context.getJobParams());
            String jobSwitch = (String) redisUtils.get(RedisConstants.JOB_SWITCH);
            if (!StringUtils.isEmpty(jobSwitch) && GlobalConstants.STATUS_OFF.equals(Integer.parseInt(jobSwitch))) {
                log.info("清洗商户代收订单数据Job未执行,Job开关已关闭.");
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
                biPaymentOrderMapper.deleteDailyByDateTime(dateStr);
                handleCollectionOrder(dateStr, startTime, endTime, dateType);
            } else {
                // 自定义时间
                log.info("进入自定义时间");
                LocalDateTime startDate = DateUtil.parseLocalDateTime(startTime, GlobalConstants.DATE_FORMAT);
                LocalDateTime endDate = DateUtil.parseLocalDateTime(endTime, GlobalConstants.DATE_FORMAT);
                long value = startDate.until(endDate, ChronoUnit.DAYS);

                for (int i = 0; i <= value; i++) {
                    String dateStr1 = DateUtil.format(startDate.plusDays(i), GlobalConstants.DATE_FORMAT_DAY);
                    biPaymentOrderMapper.deleteDailyByDateTime(dateStr1);
                    handleCollectionOrder(dateStr1, null, null, dateType);
                }

                // 重新统计月数据
                long monthVal = startDate.until(endDate, ChronoUnit.MONTHS);
                //DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.MANAGER);
                for (int i = 0; i <= monthVal; i++) {
                    String monthStr = DateUtil.format(startDate.plusMonths(i), GlobalConstants.DATE_FORMAT_MONTH);
                    biPaymentOrderMonthMapper.deleteMonthByDateTime(monthStr);
                    List<BiPaymentOrderMonth> biPaymentOrderMonthList = biPaymentOrderMonthMapper.selectDataInfoByMonth(monthStr);
                    for (BiPaymentOrderMonth item : biPaymentOrderMonthList) {
                        item.setDateTime(monthStr);
                        biPaymentOrderMonthMapper.updateByDateTime(item);
                    }

                }

            }
        } catch (Exception e) {
            log.error("HandlePaymentOrderProcessor.process" + e.getMessage());
        }

        return new ProcessResult(true, "return success");
    }

    private void handleCollectionOrder(String dateStr, String startTime, String endTime, String dateType) {
        int pageNo = 1;
        long totalSize = 0;
        Page<MerchantCollectOrders> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(BATCH_SIZE);
        // startTime <= time < endTime
        page = updateBiPaymentOrder(dateStr, startTime, endTime, page, dateType);
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
            updateBiPaymentOrder(dateStr, startTime, endTime, page, dateType);
        }
    }

    @SneakyThrows
    @NotNull
    private Page<MerchantCollectOrders> updateBiPaymentOrder(String dateStr, String startTime, String endTime, Page<MerchantCollectOrders> page, String dateType) {

        //DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.WALLET);
        String dateTmp = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_MINUTE);
        String todayStr = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY);
        LambdaQueryChainWrapper<MerchantCollectOrders> lambdaQuery2 = new LambdaQueryChainWrapper<>(collectionOrderMapper);
        lambdaQuery2.ge(MerchantCollectOrders::getUpdateTime, dateStr + " 00:00:00");
        lambdaQuery2.le(MerchantCollectOrders::getUpdateTime, dateStr + " 23:59:59");

        LambdaQueryChainWrapper<MerchantCollectOrders> lambdaQuery = new LambdaQueryChainWrapper<>(collectionOrderMapper);
        lambdaQuery.ge(MerchantCollectOrders::getCreateTime, dateStr + " 00:00:00");
        lambdaQuery.le(MerchantCollectOrders::getCreateTime, dateStr + " 23:59:59");

        // 查询申诉订单数量
        Page<MerchantCollectOrders> finalPage = page;
        CompletableFuture<Page<MerchantCollectOrders>> listFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.selectPage(finalPage, lambdaQuery2.getWrapper());
        });

        // 查询申诉订单数量
        CompletableFuture<List<MerchantCollectOrders>> totalListFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.selectCountGroupByCode(dateStr + " 00:00:00", dateStr + " 23:59:59");
        });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(listFuture, totalListFuture);
        allFutures.get();

        Page<MerchantCollectOrders> resultPage = listFuture.get();
        List<MerchantCollectOrders> totalList= totalListFuture.get();
        log.info("清洗商户代收订单总记录数->{}", resultPage.getRecords().size());
        List<MerchantCollectOrders> records = resultPage.getRecords();
        Map<String, List<MerchantCollectOrders>> collectionOrderMap =
                records.stream().collect(Collectors.groupingBy(o->o.getMerchantCode() + ":" + o.getMerchantName() + ":" + o.getMerchantType()));

        if(!CollectionUtils.isEmpty(collectionOrderMap)) {

            for (String key : collectionOrderMap.keySet()) {

                List<MerchantCollectOrders> collectionOrderList = collectionOrderMap.get(key);
                log.info("商户code{},总记录数->{}", key, collectionOrderList.size());
                BiMerchantPayOrderDaily biPaymentOrder = new BiMerchantPayOrderDaily();
                for (MerchantCollectOrders collectionOrder : collectionOrderList) {
                    if (collectionOrder.getOrderStatus().equals(CollectionOrderStatusEnum.PAID.getCode())) {
                        biPaymentOrder.setMoney(biPaymentOrder.getMoney().add(collectionOrder.getAmount()));
                        biPaymentOrder.setActualMoney(biPaymentOrder.getActualMoney().add(collectionOrder.getAmount()));
                        biPaymentOrder.setTotalFee(biPaymentOrder.getTotalFee().add(collectionOrder.getCost()));
                        // 计算订单状态为成功数量
                        biPaymentOrder.setSuccessOrderNum(biPaymentOrder.getSuccessOrderNum() + 1);
                    }
                }
                String merchantCode = CommonUtils.getMerchantCode(key);
                String merchantName = CommonUtils.getMerchantName(key);
                String merchantType = key.split(":")[2];

                if(!totalList.isEmpty()){
                    for (MerchantCollectOrders item : totalList) {
                        if(item.getMerchantCode().equals(merchantCode)){
                            biPaymentOrder.setOrderNum(item.getAmount().longValue());
                        }
                    }

                }
                biPaymentOrder.setDateTime(dateStr);
                biPaymentOrder.setMerchantCode(merchantCode);
                biPaymentOrder.setMerchantName(merchantName);
                biPaymentOrder.setMerchantType(Integer.parseInt(merchantType));
                // 自动跑今天数据endTime不为空,手动跑历史数据endTime为空
                if (!StringUtils.isEmpty(endTime)) {
                    biPaymentOrder.setLastMinute(endTime);
                }
                //DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.MANAGER);
                // dateType 为空为自动执行
                if (StringUtils.isEmpty(dateType)) {
                    biPaymentOrderMapper.updateByDateTime(biPaymentOrder);
                    // 更新月表
                    LocalDateTime dateTime = DateUtil.parseLocalDateTime(dateStr, GlobalConstants.DATE_FORMAT_DAY);
                    biPaymentOrder.setDateTime(DateUtil.format(dateTime, GlobalConstants.DATE_FORMAT_MONTH));
                    BiPaymentOrderMonth biPaymentOrderMonth = BeanUtil.toBean(biPaymentOrder, BiPaymentOrderMonth.class);
                    biPaymentOrderMonthMapper.updateByDateTime(biPaymentOrderMonth);

                } else {
                    // 只有手动跑当日数据才更新 lastMinute
                    if (todayStr.equals(dateStr)) {
                        biPaymentOrder.setLastMinute(dateTmp);
                    }
                    biPaymentOrderMapper.updateByDateTime(biPaymentOrder);
                }
            }
        }

        return resultPage;
    }
}
