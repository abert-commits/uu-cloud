package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "商户对应的代收、代付费率设置记录请求")
public class MerchantRatesConfigReq {

    @ApiModelProperty("商户会员code")
    private String merchantCode;

    @ApiModelProperty("1:代收 2:代付")
    private Integer type;

    @ApiModelProperty("代收、代付的类型code")
    private String payType;

    @ApiModelProperty("代收、代付的具体类型：例如upi、银行")
    private String payTypeName;


    @ApiModelProperty("代收、代付费率")
    private BigDecimal rates;

    @ApiModelProperty("固定手续费")
    private BigDecimal fixedFee;


    @ApiModelProperty("代收、代付最小金额")
    private BigDecimal moneyMin;

    @ApiModelProperty("代收、代付最大金额")
    private BigDecimal moneyMax;

    @ApiModelProperty("代付提醒金额")
    private BigDecimal paymentReminderAmount;

    @ApiModelProperty(value = "状态（1为启用，0为禁用）")
    private Integer status;

}