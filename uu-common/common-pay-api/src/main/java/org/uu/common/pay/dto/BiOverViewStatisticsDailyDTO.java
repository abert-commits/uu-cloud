package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

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

public class BiOverViewStatisticsDailyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 代付申请订单数量
     */
    @ApiModelProperty("代付申请订单数量")
    private Integer merchantApplicationPaymentOrderNum;

    /**
     * 代付发起订单数量
     */
    @ApiModelProperty("代付发起订单数量")
    private Integer merchantInitiatePaymentOrderNum;

    /**
     * 代付成功订单数量
     */
    @ApiModelProperty("代付成功订单数量")
    private Integer merchantSuccessPaymentOrderNum;

    /**
     * 代付成功金额
     */
    @ApiModelProperty("代付成功金额")
    private BigDecimal merchantSuccessPaymentAmount;

    /**
     * 代付手续费
     */
    @ApiModelProperty("代付手续费")
    private BigDecimal merchantSuccessPaymentCommission;

    /**
     * 代收申请订单数量
     */
    @ApiModelProperty("代收申请订单数量")
    private Integer merchantApplicationCollectionOrderNum;

    /**
     * 代收发起订单数量
     */
    @ApiModelProperty("代收发起订单数量")
    private Integer merchantInitiateCollectionOrderNum;

    /**
     * 代收成功订单数量
     */
    @ApiModelProperty("代收成功订单数量")
    private Integer merchantSuccessCollectionOrderNum;

    /**
     * 代收成功金额
     */
    @ApiModelProperty("代收成功金额")
    private BigDecimal merchantSuccessCollectionAmount;

    /**
     * 代收手续费
     */
    @ApiModelProperty("代收手续费")
    private BigDecimal merchantSuccessCollectionCommission;


    /**
     * 代收匹配成功率
     */
    @ApiModelProperty("代收匹配成功率")
    private BigDecimal merchantCollectionMatchingSuccessRate = BigDecimal.ZERO;
    /**
     * 代收成功率
     */
    @ApiModelProperty("代收成功率")
    private BigDecimal merchantCollectionSuccessRate =BigDecimal.ZERO;

    /**
     * 代收平均交易金额
     */
    @ApiModelProperty("代收平均交易金额")
    private BigDecimal merchantCollectionAvgAmount = BigDecimal.ZERO;

    /**
     * 代付发起订单金额
     */
    @ApiModelProperty("代付发起订单金额")
    private BigDecimal merchantInitiatePaymentOrderAmount;

    /**
     * 代付匹配成功率
     */
    @ApiModelProperty("代付匹配成功率")
    private BigDecimal merchantPaymentMatchingSuccessRate =BigDecimal.ZERO;
    /**
     * 代付成功率
     */
    @ApiModelProperty("代付成功率")
    private BigDecimal merchantPaymentSuccessRate =BigDecimal.ZERO;

    /**
     * 代付平均交易金额
     */
    @ApiModelProperty("代付平均交易金额")
    private BigDecimal merchantPaymentAvgAmount = BigDecimal.ZERO;

    /**
     * 代收发起订单金额
     */
    @ApiModelProperty("代收发起订单金额")
    private BigDecimal merchantInitiateCollectionOrderAmount;
    /**
     * 代收成功金额占比
     */
    @ApiModelProperty("代收成功金额占比")
    private BigDecimal merchantCollectionAmountProportion = BigDecimal.ZERO;

    /**
     * 代付成功金额占比
     */
    @ApiModelProperty("代付成功金额占比")
    private BigDecimal merchantPaymentAmountProportion = BigDecimal.ZERO;

    /**
     * 平台发起订单数量
     */
    @ApiModelProperty("平台发起订单数量")
    private Integer platformInitiateOrderNum = 0 ;
    /**
     * 平台成功订单数量
     */
    @ApiModelProperty("平台成功订单数量")
    private Integer platformSuccessOrderNum = 0;
    /**
     * 平台成功率
     */
    @ApiModelProperty("平台成功率")
    private BigDecimal platformSuccessRate = BigDecimal.ZERO;
    /**
     * 平台发成功金额
     */
    @ApiModelProperty("平台发成功金额")
    private BigDecimal platformSuccessAmount = BigDecimal.ZERO;

    /**
     * 平台-平均交易额
     */
    @ApiModelProperty("平台-平均交易额")
    private BigDecimal platformAvgAmount = BigDecimal.ZERO;

    /**
     * 平台成功金额占比
     */
    @ApiModelProperty("平台成功金额占比")
    private BigDecimal platformOrderAmountProportion = BigDecimal.ZERO;

    /**
     * 平台手续费
     */
    @ApiModelProperty("平台手续费")
    private BigDecimal platformOrderCommission = BigDecimal.ZERO;


}
