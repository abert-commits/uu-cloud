package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * @author
 */
@Data
@ApiModel(description = "下发申请参数")
public class ApplyDistributedReq implements Serializable {


    @ApiModelProperty("商户")
    private String merchantCode;

    @ApiModelProperty("下发usdt地址")
    private String usdtAddr;

    @ApiModelProperty("币种")
    private String currency;


    @ApiModelProperty("总额度")
    private BigDecimal balance;
    @ApiModelProperty("下发金额")
    private BigDecimal amount;

    @ApiModelProperty("remark")
    private String remark;

    @ApiModelProperty("商户下发余额类型：3:商户的选择法币，1:USDT, 2:TRX")
    private String payType = "3";


}