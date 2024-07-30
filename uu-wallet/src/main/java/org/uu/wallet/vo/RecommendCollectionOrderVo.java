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
@ApiModel(description = "推荐金额支付下单接口接口请求参数")
public class RecommendCollectionOrderVo implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 充值商户订单号
     */
    @NotBlank(message = "充值商户订单号不能为空")
    @ApiModelProperty(value = "充值商户订单号")
    private String collectionMerchantOrder;

    /**
     * 提现商户订单号
     */
    @NotBlank(message = "提现商户订单号不能为空")
    @ApiModelProperty(value = "提现商户订单号")
    private String paymentMerchantOrder;

    /**
     * 充值提交金额
     */
    @NotNull(message = "充值提交金额不能为空")
    @DecimalMin(value = "0.00", message = "充值提交金额格式不正确")
    @ApiModelProperty(value = "充值提交金额")
    private BigDecimal collectionSubmitAmount;

    /**
     * 充值实际金额
     */
    @NotNull(message = "充值实际金额不能为空")
    @DecimalMin(value = "0.00", message = "充值实际金额格式不正确")
    @ApiModelProperty(value = "充值实际金额")
    private BigDecimal collectionActualAmount;

    /**
     * 充值商户号
     */
    @NotBlank(message = "充值商户号不能为空")
    @ApiModelProperty(value = "充值商户号")
    private String collectionMerchantCode;

    /**
     * 提现商户号
     */
    @NotBlank(message = "提现商户号不能为空")
    @ApiModelProperty(value = "提现商户号")
    private String paymentMerchantCode;

    /**
     * 充值会员ID
     */
    @NotBlank(message = "充值会员ID不能为空")
    @ApiModelProperty(value = "充值会员ID")
    private String collectionMemberId;

    /**
     * 提现会员ID
     */
    @NotBlank(message = "提现会员ID不能为空")
    @ApiModelProperty(value = "提现会员ID")
    private String paymentMemberId;

    /**
     * md5签名值
     */
    @NotBlank(message = "sign不能为空")
    @ApiModelProperty(value = "md5签名值")
    private String sign;
}