package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author admin
 * @date 2024/3/8 14:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BiMerchantDailyEnDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty("Date Time")
    private String dateTime;

    /**
     * 商户名称
     */
    @ApiModelProperty("Merchant Name")
    private String merchantName;

    /**
     * 商户类型: 1.内部商户 2.外部商户
     */
    @ApiModelProperty("Merchant Type")
    private String merchantType;

    /**
     * 代收金额
     */
    @ApiModelProperty("Pay Money")
    private BigDecimal payMoney = BigDecimal.ZERO;



    /**
     * 代收下单总笔数
     */
    @ApiModelProperty("Pay Order Num")
    private Long payOrderNum = 0L;

    /**
     * 代收成功笔数
     */
    @ApiModelProperty("Pay Success Order Number")
    private Long paySuccessOrderNum = 0L;


    @TableField(exist = false)
    @ApiModelProperty(value = "Pay Success Rate")
    private Double paySuccessRate;

    /**
     * 代付金额
     */
    @ApiModelProperty("Withdraw Money")
    private BigDecimal withdrawMoney = BigDecimal.ZERO;


    /**
     * 代付下单总笔数
     */
    @ApiModelProperty("Withdraw Order Number")
    private Long withdrawOrderNum = 0L;

    /**
     * 代付成功笔数
     */
    @ApiModelProperty("Withdraw Success Order Number")
    private Long withdrawSuccessOrderNum = 0L;

    /**
     * 收付差额
     */
    @ApiModelProperty("Difference")
    private BigDecimal difference = BigDecimal.ZERO;


    @ApiModelProperty(value = "Withdraw Success Rate")
    private Double withdrawSuccessRate;

    /**
     * 总费用
     */
    @ApiModelProperty("Total Fee")
    private BigDecimal totalFee = BigDecimal.ZERO;
}
