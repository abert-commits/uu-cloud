package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

@Data
@ApiModel(description = "代付订单请求参数")
public class PaymentOrderReq extends PageRequest {

    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private String merchantCode;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currentcy;

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



    @ApiModelProperty(value = "结束时间")
    private String queryTime;






}
