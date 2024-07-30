package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商户代付日报表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BiMerchantWithdrawOrderDailyEnDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 日期
     */
    @ApiModelProperty(value = "Date")
    private String dateTime;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "Order Amount")
    private BigDecimal money = BigDecimal.ZERO;

    /**
     * 下单笔数
     */
    @ApiModelProperty(value = "Number of Orders")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty(value = "Successful Orders")
    private Long successOrderNum = 0L;


    /**
     * 费用
     */
    @ApiModelProperty(value = "Fee")
    private BigDecimal totalFee = BigDecimal.ZERO;




}