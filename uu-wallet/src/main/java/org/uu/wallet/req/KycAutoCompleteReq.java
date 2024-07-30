package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lukas
 */
@Data
@ApiModel(description = "自动拉取kyc交易记录参数")
public class KycAutoCompleteReq {

    @ApiModelProperty(value = "买入订单号")
    private String buyerOrder;
    @ApiModelProperty(value = "买入方的会员id")
    private String buyerMemberId;
    @ApiModelProperty(value = "卖出订单号")
    private String sellerOrder;
    @ApiModelProperty(value = "卖出方的会员id")
    private String sellerMemberId;
    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;
    @ApiModelProperty(value = "充值 1 提现 2")
    private String type;
    @ApiModelProperty(value = "提现用户upi(如果是提现必填)")
    private String withdrawUpi;
    @ApiModelProperty(value = "币种")
    private String currency;
    @ApiModelProperty(value = "utr(如果是充值必填)")
    private String utr;
    @ApiModelProperty(value = "kycId")
    private String kycId;
}
