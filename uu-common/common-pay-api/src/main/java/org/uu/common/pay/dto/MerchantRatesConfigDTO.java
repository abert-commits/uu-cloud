package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "商户对应的代收、代付分页列表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantRatesConfigDTO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "代收、代付的类型")
    private String payType;

    @ApiModelProperty(value = "代收、代付的具体类型：例如upi、银行")
    private String payTypeName;

    @ApiModelProperty(value = "代收、代付费率")
    private BigDecimal rates;

    @ApiModelProperty(value = "固定手续费")
    private BigDecimal fixedFee;

    @ApiModelProperty(value = "代收、代付最小金额")
    private BigDecimal moneyMin;

    @ApiModelProperty(value = "代收、代付最大金额")
    private BigDecimal moneyMax;

    @ApiModelProperty(value = "代付提醒金额")
    private BigDecimal paymentReminderAmount;

    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "修改时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "修改人")
    private String updateBy;
}