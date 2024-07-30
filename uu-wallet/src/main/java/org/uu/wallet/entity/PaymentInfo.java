package org.uu.wallet.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付页面数据
 *
 * @author Simon
 * @date 2024/01/02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "支付页面数据")
public class PaymentInfo implements Serializable {

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

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
     * 支付剩余时间 秒
     */
    @ApiModelProperty("支付剩余时间 秒")
    private Long paymentExpireTime;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
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
     * 返回地址
     */
    @ApiModelProperty("返回地址")
    private String returnUrl;


    /**
     * 订单状态 默认状态: 待支付
     */
    @ApiModelProperty("订单状态, 取值说明: 1:待支付, 2: 已支付, 3: 已取消")
    private String orderStatus = CollectionOrderStatusEnum.BE_PAID.getCode();

    /**
     * 蚂蚁正在链接的upiId
     */
    private String upiId;
}
