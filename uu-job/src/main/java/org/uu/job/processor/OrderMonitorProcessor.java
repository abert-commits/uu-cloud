package org.uu.job.processor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.uu.manager.entity.OrderMonitor;
import org.uu.manager.mapper.OrderMonitorMapper;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MerchantCollectOrders;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.uu.wallet.util.SpringContextUtil;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;


/**
 * 订单监控定时任务
 *
 * @author Admin
 */
@Component("orderMonitorProcessor")
@RequiredArgsConstructor
@Slf4j
public class OrderMonitorProcessor implements BasicProcessor {

    private final MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;
    private final CollectionOrderMapper collectionOrderMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    private final OrderMonitorMapper orderMonitorMapper;


    @Override
    public ProcessResult process(TaskContext context) {
        try {


              LocalDateTime now = LocalDateTime.now();
              DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
              String formattedDateTime = now.format(formatter);
             // long count = merchantPaymentOrdersMapper.selectCount(Wrappers.<MerchantPaymentOrders> lambdaQuery().le(MerchantPaymentOrders::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(MerchantPaymentOrders::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
             CompletableFuture<Integer> countFuture = CompletableFuture.supplyAsync(() -> {
                return merchantPaymentOrdersMapper.selectCount(Wrappers.<MerchantPaymentOrders> lambdaQuery().le(MerchantPaymentOrders::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(MerchantPaymentOrders::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
             });
              LocalDateTime dateTime = LocalDateTime.parse(formattedDateTime, formatter);
              OrderMonitor orderMonitorMerchantPaymentOrders = new OrderMonitor();
              orderMonitorMerchantPaymentOrders.setStatisticalTime(dateTime);
              orderMonitorMerchantPaymentOrders.setStatisticalNum(countFuture.get());
              orderMonitorMerchantPaymentOrders.setCode("DF");

              //long countCollectionOrder = collectionOrderMapper.selectCount(Wrappers.<CollectionOrder> lambdaQuery().le(CollectionOrder::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(CollectionOrder::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                CompletableFuture<Integer> countCollectionOrderFuture = CompletableFuture.supplyAsync(() -> {
                    return collectionOrderMapper.selectCount(Wrappers.<CollectionOrder> lambdaQuery().le(CollectionOrder::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(CollectionOrder::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                });
              OrderMonitor orderMonitorCollectionOrder = new OrderMonitor();
              orderMonitorCollectionOrder.setStatisticalTime(dateTime);
              orderMonitorCollectionOrder.setStatisticalNum(countCollectionOrderFuture.get());
              orderMonitorCollectionOrder.setCode("MR");

              //long countPaymentOrder = paymentOrderMapper.selectCount(Wrappers.<PaymentOrder> lambdaQuery().le(PaymentOrder::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(PaymentOrder::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                CompletableFuture<Integer> countPaymentOrderFuture = CompletableFuture.supplyAsync(() -> {
                    return paymentOrderMapper.selectCount(Wrappers.<PaymentOrder> lambdaQuery().le(PaymentOrder::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(PaymentOrder::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
                });
            OrderMonitor orderMonitorPaymentOrder = new OrderMonitor();
            orderMonitorPaymentOrder.setStatisticalTime(dateTime);
            orderMonitorPaymentOrder.setStatisticalNum(countPaymentOrderFuture.get());
            orderMonitorPaymentOrder.setCode("MC");

              //long countMerchantCollectOrders = merchantCollectOrdersMapper.selectCount(Wrappers.<MerchantCollectOrders> lambdaQuery().le(MerchantCollectOrders::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(MerchantCollectOrders::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            CompletableFuture<Integer> countMerchantCollectOrdersFuture = CompletableFuture.supplyAsync(() -> {
                return merchantCollectOrdersMapper.selectCount(Wrappers.<MerchantCollectOrders> lambdaQuery().le(MerchantCollectOrders::getCreateTime,LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).ge(MerchantCollectOrders::getCreateTime, LocalDateTime.now().minusMinutes(20).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            });
            OrderMonitor orderMonitorMerchantCollectOrders = new OrderMonitor();
            orderMonitorMerchantCollectOrders.setStatisticalTime(dateTime);
            orderMonitorMerchantCollectOrders.setStatisticalNum(countMerchantCollectOrdersFuture.get());
            orderMonitorMerchantCollectOrders.setCode("DS");
            this.orderMonitorMapper.insert(orderMonitorMerchantPaymentOrders);
            this.orderMonitorMapper.insert(orderMonitorMerchantCollectOrders);
            this.orderMonitorMapper.insert(orderMonitorPaymentOrder);
            this.orderMonitorMapper.insert(orderMonitorCollectionOrder);
                    log.error("订单监控.process");
        } catch (Exception e) {
            log.error("订单监控.process" + e.getMessage());
            return new ProcessResult(false, "return success");
        }

        return new ProcessResult(true, "return success");
    }


}
