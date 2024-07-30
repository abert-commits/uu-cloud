package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "货币配置请求参数")
public class SystemCurrencyReq {

    private Long id;

    @ApiModelProperty(value = "币种符号")
    private String currencyCode;

    @ApiModelProperty(value = "币种ID(火币)")
    private String coinId;


    @ApiModelProperty(value = "币种名称")
    private String currencyName;


    @ApiModelProperty(value = "币种ID2(火币)")
    private String currencyId;


    @ApiModelProperty(value = "代收支付方式")
    private String paymentType;


    @ApiModelProperty(value = "代付类型")
    private String withdrawType;


    @ApiModelProperty(value = "USDT最低汇率")
    private BigDecimal usdtMin;


    @ApiModelProperty(value = "USDT最高汇率")
    private BigDecimal usdtMax;

    @ApiModelProperty(value = "USDT自动汇率")
    private BigDecimal usdtAuto;

    @ApiModelProperty(value = "USDT矫正汇率")
    private BigDecimal usdtCorrect;

    @ApiModelProperty(value = "使用汇率标记 1:自动 2:矫正汇率")
    private Integer rateFlag;


    @ApiModelProperty(value = "时区")
    private String timeZone;


    @ApiModelProperty(value = "是否可用 0 可用 ,1 不可用")
    private Integer enableFlag;
}
