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
public class BiMerchantWithdrawOrderDailyDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private String dateTime;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal money = BigDecimal.ZERO;

    /**
     * 下单笔数
     */
    @ApiModelProperty(value = "下单笔数")
    private Long orderNum = 0L;

    /**
     * 成功笔数
     */
    @ApiModelProperty(value = "成功笔数")
    private Long successOrderNum = 0L;


    /**
     * 费用
     */
    @ApiModelProperty(value = "费用")
    private BigDecimal totalFee = BigDecimal.ZERO;




}