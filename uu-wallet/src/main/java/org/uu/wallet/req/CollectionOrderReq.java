package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

@Data
@ApiModel(description = "代收订单请求参数")
public class CollectionOrderReq extends PageRequest {

    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private String merchantCode;


    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式")
    private String payType;

    /**
     * 商户订单号
     */
    @ApiModelProperty(value = "商户订单号")
    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty(value = "平台订单号")
    private String platformOrder;

    /**
     * 三方订单号
     */
//    @ApiModelProperty(value="三方订单号")
//    private String thirdOrder;

    /**
     * 转账流水
     */
//    @ApiModelProperty(value="转账流水")
//    private String transferStatement;

    /**
     * 订单金额
     */
//    @ApiModelProperty(value="订单金额")
//    private BigDecimal amount;

    /**
     * 订单费率
     */
//    @ApiModelProperty(value="订单费率")
//    private BigDecimal orderRate;

    /**
     * 汇率
     */
//    @ApiModelProperty(value="汇率")
//    private BigDecimal exchangeRate;

    /**
     * 转换金额
     */
//    @ApiModelProperty(value="转换金额")
//    private BigDecimal conversionAmount;

    /**
     * 手续费
     */
//    @ApiModelProperty(value="手续费")
//    private BigDecimal commission;

    /**
     * 结算金额
     */
//    @ApiModelProperty(value="结算金额")
//    private BigDecimal settlementAmount;

    /**
     * 收款金额
     */
//    @ApiModelProperty(value="收款金额")
//    private String collectedAmount;

    /**
     * 订单状态
     */
    @ApiModelProperty(value = "支付状态")
    private String orderStatus;

    /**
     * 回调状态
     */
    @ApiModelProperty(value = "回调状态")
    private String callbackStatus;

    /**
     * 开始时间 时间戳(10位)
     */
    @ApiModelProperty(value = "开始时间")
    private Long startTime;

    /**
     * 结束时间 时间戳(10位)
     */
    @ApiModelProperty(value = "结束时间")
    private Long endTime;

    /**
     * 创建人
     */
//    @ApiModelProperty(value="创建人")
//    private String createBy;

    /**
     * 修改人
     */
//    @ApiModelProperty(value="修改人")
//    private LocalDateTime updateBy;

}
