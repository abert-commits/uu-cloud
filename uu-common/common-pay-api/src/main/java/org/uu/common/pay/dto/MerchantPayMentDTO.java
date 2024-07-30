package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "商户列表列对应的代收、代付列表")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantPayMentDTO implements Serializable {

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

    @ApiModelProperty(value = "商户code")
    private String merchantCode;

    @ApiModelProperty(value = "1:代收 2:代付")
    private Integer paymentType;

}