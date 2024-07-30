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

/**
 * @author
 */
@Data
@ApiModel(description = "充值下单接口请求参数")
public class CollectionOrderVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商户订单号
     */
    @NotBlank(message = "商户订单号不能为空")
    @ApiModelProperty(value = "商户订单号")
    private String merchantOrder;

    /**
     * 订单金额
     */
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.00", message = "订单金额格式不正确")
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 商户号
     */
    @NotBlank(message = "商户号不能为空")
    @ApiModelProperty(value = "商户号")
    private String merchantCode;

    /**
     * 回调地址
     */
    @NotBlank(message = "交易回调地址不能为空")
    @ApiModelProperty(value = "交易回调地址")
    private String tradeNotifyUrl;

    /**
     * 时间戳
     */
    @NotBlank(message = "时间戳不能为空")
    @ApiModelProperty(value = "时间戳")
    private String timestamp;

    /**
     * 会员id
     */
    @NotBlank(message = "会员id不能为空")
    @ApiModelProperty(value = "会员id")
    private String memberId;

    /**
     * 会员账号
     */
    @NotBlank(message = "会员账号不能为空")
    @ApiModelProperty(value = "会员账号")
    private String memberAccount;

    /**
     * md5签名值
     */
    @NotBlank(message = "sign不能为空")
    @ApiModelProperty(value = "md5签名值")
    private String sign;
}