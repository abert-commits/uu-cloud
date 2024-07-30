package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "短信预警DTO类")
public class SmsBalanceWarnDTO implements Serializable {

    /**
     * 当前余额
     */
    @ApiModelProperty("当前余额")
    private BigDecimal currentBalance;


    /**
     * 阈值
     */
    @ApiModelProperty(value = "阈值")
    private BigDecimal threshold;


    /**
     * 是否告警
     */
    @ApiModelProperty(value = "是否告警")
    private Boolean isWarn;

}
