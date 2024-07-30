package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.common.pay.dto.LastOrderWarnDTO;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MerchantCollectOrders;
import org.uu.wallet.entity.MerchantPaymentOrders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户代付订单表 Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-01-05
 */
@Mapper
public interface MerchantPaymentOrdersMapper extends BaseMapper<MerchantPaymentOrders> {

    Long selectWithdrawFuture();

    List<MemberInfo> selectMerchantWithdrawNum();

    List<MerchantPaymentOrders> selectCountGroupByCode(@Param("startTime")String startTime, @Param("endTime")String endTime);

    List<MemberInfo> selectCostByDate(@Param("dateStr")String dateStr);

    List<LastOrderWarnDTO> getPaymentLastOrderCreditTime();

    /**
     * 根据商户订单号查询代付订单 加上排他行锁
     *
     * @return {@link CollectionOrder}
     */
    @Select("SELECT * FROM merchant_payment_orders WHERE merchant_order = #{merchantOrder} FOR UPDATE")
    MerchantPaymentOrders selectMerchantPaymentOrdersByMerchantOrderForUpdate(String merchantOrder);


    /**
     * 根据平台订单号查询代付订单 加上排他行锁
     *
     * @return {@link MerchantPaymentOrders}
     */
    @Select("SELECT * FROM merchant_payment_orders WHERE platform_order = #{platformOrder} FOR UPDATE")
    MerchantPaymentOrders selectMerchantPaymentOrdersByPlatformOrderForUpdate(String platformOrder);
}
