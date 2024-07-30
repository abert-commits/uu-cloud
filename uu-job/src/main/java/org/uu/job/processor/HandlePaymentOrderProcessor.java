package org.uu.job.processor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.constant.*;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.entity.BiPaymentOrderMonth;
import org.uu.manager.mapper.BiPaymentOrderMapper;
import org.uu.manager.mapper.BiPaymentOrderMonthMapper;
import org.uu.wallet.Enum.AppealStatusEnum;
import org.uu.wallet.Enum.CollectionOrderCancelTypeEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.entity.AppealOrder;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MemberAccountChange;
import org.uu.wallet.mapper.AppealOrderMapper;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.uu.wallet.mapper.MemberAccountChangeMapper;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;
import java.util.stream.Collectors;


/**
 * 清洗买入订单数据
 *
 * @author Admin
 */
@Component("handlePaymentOrderProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandlePaymentOrderProcessor implements BasicProcessor {

    private final BiPaymentOrderMapper biPaymentOrderMapper;
    private final MemberAccountChangeMapper memberAccountChangeMapper;
    private final CollectionOrderMapper collectionOrderMapper;
    private final BiPaymentOrderMonthMapper biPaymentOrderMonthMapper;
    private final AppealOrderMapper appealOrderMapper;

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
                log.info("清洗买入订单数据Job未执行,Job开关已关闭.");
                return new ProcessResult(true, "Job switch turned off");
            }
            String params = !StringUtils.isEmpty(context.getJobParams()) ? context.getJobParams() : context.getInstanceParams();
            JSONObject jsonObject = StringUtils.isEmpty(params) ? new JSONObject() : (JSONObject) JSONObject.parse(params);
            String startTime = jsonObject.getString("startTime");
            String endTime = jsonObject.getString("endTime");

            String dateType = jsonObject.getString("dateType");
            // 每天凌晨30分跑前一天数据
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

    private void handleCollectionOrder(String dateStr, String startTime, String endTime, String dateType) throws ExecutionException, InterruptedException {
        int pageNo = 1;
        long totalSize = 0;
        Page<CollectionOrder> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(BATCH_SIZE);
        // startTime <= time < endTime
        page = updateBiPaymentOrder(dateStr, startTime, endTime, page, dateType);
        List<CollectionOrder> records = page.getRecords();
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

    @NotNull
    private Page<CollectionOrder> updateBiPaymentOrder(String dateStr, String startTime, String endTime, Page<CollectionOrder> page, String dateType) throws ExecutionException, InterruptedException {

        //DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.WALLET);
        String dateTmp = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_MINUTE);
        String todayStr = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY);
        LambdaQueryChainWrapper<CollectionOrder> lambdaQuery2 = new LambdaQueryChainWrapper<>(collectionOrderMapper);
        lambdaQuery2.ge(CollectionOrder::getCreateTime, dateStr + " 00:00:00");
        lambdaQuery2.le(CollectionOrder::getCreateTime, dateStr + " 23:59:59");

        LambdaQueryChainWrapper<CollectionOrder> lambdaQuery = new LambdaQueryChainWrapper<>(collectionOrderMapper);
        lambdaQuery.ge(CollectionOrder::getCreateTime, dateStr + " 00:00:00");
        lambdaQuery.le(CollectionOrder::getCreateTime, dateStr + " 23:59:59");

        // 查询申诉订单数量
        CompletableFuture<List<AppealOrder>> appealNumFuture = CompletableFuture.supplyAsync(() -> {
            return appealOrderMapper.selectAppealNum(dateStr, 2);
        });

        // 查询申诉订单总数量
        CompletableFuture<Long> appealTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return appealOrderMapper.selectAppealTotalNum(dateStr, 2);
        });

        // 查询申诉订单数量
        CompletableFuture<Long> appealWrongAmountNumFuture = CompletableFuture.supplyAsync(() -> {
            return appealOrderMapper.selectWrongAmountNum(dateStr, 1, 2);
        });

        // 查询申诉订单数量
        CompletableFuture<Integer> totalFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.selectCount(lambdaQuery.getWrapper());
        });

        Page<CollectionOrder> finalPage = page;
        CompletableFuture<Page<CollectionOrder>> pageFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.selectPage(finalPage, lambdaQuery2.getWrapper());
        });
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(appealNumFuture, pageFuture, appealTotalNumFuture, totalFuture, appealWrongAmountNumFuture);
        allFutures.get();
        page = pageFuture.get();
        List<AppealOrder> appealList = appealNumFuture.get();
        Integer totalCount = totalFuture.get();
        log.info("HandlePaymentOrderProcessor总记录数->{}, 日期->{}", page.getRecords().size(), dateStr);
        List<CollectionOrder> records = page.getRecords();

        BiPaymentOrder biPaymentOrder = new BiPaymentOrder();
        try {
            for (CollectionOrder collectionOrder : records) {
                if (collectionOrder.getOrderStatus().equals(OrderStatusEnum.SUCCESS.getCode()) || collectionOrder.getOrderStatus().equals(OrderStatusEnum.WAS_CANCELED.getCode()) || collectionOrder.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode())) {
                    biPaymentOrder.setMoney(biPaymentOrder.getMoney().add(collectionOrder.getAmount()));
                    if (collectionOrder.getOrderStatus().equals(OrderStatusEnum.SUCCESS.getCode())) {
                        biPaymentOrder.setActualMoney(biPaymentOrder.getActualMoney().add(collectionOrder.getActualAmount()));
                        // 计算订单状态为成功数量
                        biPaymentOrder.setSuccessOrderNum(biPaymentOrder.getSuccessOrderNum() + 1);
                    }

                    biPaymentOrder.setTotalFee(biPaymentOrder.getTotalFee().add(collectionOrder.getBonus()));
                } else if (collectionOrder.getOrderStatus().equals(OrderStatusEnum.WAS_CANCELED.getCode()) && !StringUtils.isEmpty(collectionOrder.getCancelType()) && collectionOrder.getCancelType().equals(CollectionOrderCancelTypeEnum.CancelOrder.getCode())) {
                    biPaymentOrder.setCancelOrder(biPaymentOrder.getCancelOrder() + 1);
                }

                if (!StringUtils.isEmpty(collectionOrder.getIsPaymentCancelled()) && collectionOrder.getIsPaymentCancelled().equals("1") && !StringUtils.isEmpty(collectionOrder.getCancelType()) && collectionOrder.getCancelType().equals(CollectionOrderCancelTypeEnum.CancelPay.getCode())) {
                    biPaymentOrder.setCancelPay(biPaymentOrder.getCancelPay() + 1);
                }

                if (collectionOrder.getOrderStatus().equals(OrderStatusEnum.WAS_CANCELED.getCode())) {
                    biPaymentOrder.setCancel(biPaymentOrder.getCancel() + 1);
                }
            }
            log.info("买入订单总金额: {}", biPaymentOrder.getMoney());
        } catch (Exception e) {
            log.error("HandlePaymentOrderProcessor.process" + e.getMessage());
        }


        //订单总笔数量
        biPaymentOrder.setOrderNum(Long.valueOf(String.valueOf(records.size())));

        biPaymentOrder.setAppealNum(appealTotalNumFuture.get());
        biPaymentOrder.setAmountError(appealWrongAmountNumFuture.get());
        biPaymentOrder.setDateTime(dateStr);
        for (AppealOrder item : appealList) {
            if (item.getAppealStatus().equals(Integer.parseInt(AppealStatusEnum.PAID.getCode()))) {
                biPaymentOrder.setAppealSuccess(item.getOrderAmount().longValue());
            } else if (item.getAppealStatus().equals(Integer.parseInt(AppealStatusEnum.UNPAID.getCode()))) {
                biPaymentOrder.setAppealFail(item.getOrderAmount().longValue());
            }
        }
        // 自动跑今天数据endTime不为空,手动跑历史数据endTime为空
        if (!StringUtils.isEmpty(endTime)) {
            biPaymentOrder.setLastMinute(endTime);
        }

        //统计会员买入奖励
        LambdaQueryChainWrapper<MemberAccountChange> lambdaQueryChainWrapper = new LambdaQueryChainWrapper<>(memberAccountChangeMapper);
        lambdaQueryChainWrapper.ge(MemberAccountChange::getCreateTime, dateStr + " 00:00:00");
        lambdaQueryChainWrapper.le(MemberAccountChange::getCreateTime, dateStr + " 23:59:59");
        lambdaQueryChainWrapper.eq(MemberAccountChange::getChangeType, 8);

        CompletableFuture<List<MemberAccountChange>> totalBouns = CompletableFuture.supplyAsync(() -> {
            return memberAccountChangeMapper.selectList(lambdaQueryChainWrapper.getWrapper());
        });

        List<MemberAccountChange> list = totalBouns.get();
        //计算会员的买入奖励
        if (!CollectionUtils.isEmpty(list)) {
            for (MemberAccountChange item : list) {
                biPaymentOrder.setBuyBonus(biPaymentOrder.getBuyBonus().add(item.getAmountChange()));
            }
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

        return page;
    }
}
