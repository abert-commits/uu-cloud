package org.uu.manager.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 首页订单统计
 * </p>
 *
 * @author 
 * @since 2024-07-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_over_view_statistics_daily")
public class BiOverViewStatisticsDaily implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    private String dateTime;

    /**
     * 代付申请订单数量
     */
    private Integer merchantApplicationPaymentOrderNum  = 0;

    /**
     * 代付发起订单数量
     */
    private Integer merchantInitiatePaymentOrderNum  = 0;

    /**
     * 代付成功订单数量
     */
    private Integer merchantSuccessPaymentOrderNum = 0;

    /**
     * 代付成功金额
     */
    private BigDecimal merchantSuccessPaymentAmount = BigDecimal.ZERO;

    /**
     * 代付手续费
     */
    private BigDecimal merchantSuccessPaymentCommission = BigDecimal.ZERO;

    /**
     * 代收申请订单数量
     */
    private Integer merchantApplicationCollectionOrderNum = 0;

    /**
     * 代收发起订单数量
     */
    private Integer merchantInitiateCollectionOrderNum = 0;

    /**
     * 代收发起订单金额
     */
    private BigDecimal merchantInitiateCollectionOrderAmount = BigDecimal.ZERO;

    /**
     * 代付成功订单数量
     */
    private Integer merchantSuccessCollectionOrderNum = 0;

    /**
     * 代付成功金额
     */
    private BigDecimal merchantSuccessCollectionAmount = BigDecimal.ZERO;

    /**
     * 代收手续费
     */
    private BigDecimal merchantSuccessCollectionCommission = BigDecimal.ZERO;

    /**
     * 代付发起订单金额
     */
    private BigDecimal merchantInitiatePaymentOrderAmount = BigDecimal.ZERO;


}
