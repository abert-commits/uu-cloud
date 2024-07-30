package org.uu.wallet.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;

import java.io.Serializable;

/**
 * 支付页面数据
 *
 * @author Simon
 * @date 2024/01/02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "激活钱包页面数据")
public class ActivateWallet implements Serializable {


    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;


    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;


    /**
     * 返回地址
     */
    @ApiModelProperty("返回地址")
    private String returnUrl;


    /**
     * 订单状态 默认状态: 待支付
     */
    @ApiModelProperty("会员激活状态, 取值说明: 1:已激活, 0: 未激活")
    private String orderStatus = CollectionOrderStatusEnum.BE_PAID.getCode();


    /**
     * 激活钱包页面过期剩余时间 秒
     */
    @ApiModelProperty("激活钱包页面过期剩余时间 秒")
    private Long walletActivationPageExpiryTime;
}
