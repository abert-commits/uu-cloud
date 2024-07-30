package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel(description = "确认支付接口请求参数")
public class ConfirmingPayVo implements Serializable {

    /**
     * 商户订单号
     */
    @NotBlank(message = "商户订单号不能为空")
    @ApiModelProperty(value = "商户订单号")
    private String merchantOrder;

    /**
     * 订单提交金额
     */
    @NotNull(message = "订单提交金额不能为空")
    @DecimalMin(value = "0.00", message = "订单提交金额格式不正确")
    @ApiModelProperty(value = "订单提交金额")
    private BigDecimal orderSubmitAmount;

    /**
     * 订单实际金额
     */
    @NotNull(message = "订单实际金额不能为空")
    @DecimalMin(value = "0.00", message = "订单实际金额格式不正确")
    @ApiModelProperty(value = "订单实际金额")
    private BigDecimal orderActualAmount;

    /**
     * 商户号
     */
    @NotBlank(message = "商户号不能为空")
    @ApiModelProperty(value = "商户号")
    private String merchantCode;

    /**
     * 时间戳
     */
    @NotBlank(message = "时间戳不能为空")
    @ApiModelProperty(value = "时间戳")
    private String timestamp;

    /**
     * 会员ID
     */
    @NotBlank(message = "会员ID不能为空")
    @ApiModelProperty(value = "会员ID")
    private String memberId;

    /**
     * md5签名值
     */
    @NotBlank(message = "sign不能为空")
    @ApiModelProperty(value = "md5签名值")
    private String sign;
}
