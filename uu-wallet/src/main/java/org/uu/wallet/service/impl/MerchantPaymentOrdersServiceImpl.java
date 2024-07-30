package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.uu.wallet.service.IMerchantPaymentOrdersService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 商户代付订单表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-01-05
 */
@Service
public class MerchantPaymentOrdersServiceImpl extends ServiceImpl<MerchantPaymentOrdersMapper, MerchantPaymentOrders> implements IMerchantPaymentOrdersService {


    /**
     * 根据商户订单号 获取订单信息
     *
     * @param merchantOrder
     * @return {@link MerchantPaymentOrders}
     */
    @Override
    public MerchantPaymentOrders getOrderInfoByOrderNumber(String merchantOrder) {
        return lambdaQuery()
                .eq(MerchantPaymentOrders::getMerchantOrder, merchantOrder)
                .or()
                .eq(MerchantPaymentOrders::getPlatformOrder, merchantOrder)
                .one();
    }

    @Override
    public Map<String, List<MerchantPaymentOrders>> paymentMap(String merchantCode) {
        return lambdaQuery()
                .eq(MerchantPaymentOrders::getOrderStatus, CollectionOrderStatusEnum.PAID.getCode())
                .eq(MerchantPaymentOrders::getMerchantCode, merchantCode)
                .list()
                .parallelStream()
                .collect(Collectors.groupingByConcurrent(MerchantPaymentOrders::getPayType));
    }
}
