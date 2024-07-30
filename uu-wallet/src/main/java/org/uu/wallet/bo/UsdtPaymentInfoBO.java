package org.uu.wallet.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * USDT支付页面数据
 *
 * @author Simon
 * @date 2024/01/02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "USDT支付页面数据")
public class UsdtPaymentInfoBO implements Serializable {

    /**
     * USDT充值地址
     */
    @ApiModelProperty(value = "USDT充值地址")
    private String usdtAddr;

    /**
     * 主网络 目前先写死 TRC-20
     */
    @ApiModelProperty(value = "主网络")
    private String networkProtocol;

    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 支付剩余时间 秒
     */
    @ApiModelProperty("支付剩余时间 秒")
    private Long paymentExpireTime;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额 USDT")
    private BigDecimal amount;

    /**
     * 商户订单号
     */
    @ApiModelProperty("商户订单号")
    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty("平台订单号")
    private String platformOrder;

    /**
     * 订单时间
     */
    @ApiModelProperty("订单时间")
    private String createTime;

    /**
     * 订单状态 默认状态: 待支付
     */
    @ApiModelProperty("订单状态, 取值说明: 1:待支付, 2: 已支付, 3: 已取消")
    private String orderStatus;

    /**
     * 最低充值金额
     */
    @ApiModelProperty("最低充值金额 USDT")
    private BigDecimal minimumAmount;
}
