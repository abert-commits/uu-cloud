package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "USDT买入下单接口返回数据")
public class UsdtBuyVo implements Serializable {

    /**
     * 主网络
     */
    @ApiModelProperty(value = "主网络")
    private String networkProtocol;

    /**
     * USDT充值地址
     */
    @ApiModelProperty(value = "USDT充值地址")
    private String usdtAddr;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;

    /**
     * USDT支付剩余时间
     */
    @ApiModelProperty(value = "USDT支付剩余时间 单位: 秒  如果值为null或负数 表示该笔订单已过期")
    private Long usdtPaymentExpireTime;


    /**
     * USDT数量
     */
    @ApiModelProperty(value = "USDT数量")
    private BigDecimal usdtNum;


    /**
     * iToken数量
     */
    @ApiModelProperty(value = "iToken数量")
    private BigDecimal iToken;

    /**
     * 蚂蚁USDT最低充值金额
     */
    @ApiModelProperty(value = "蚂蚁USDT最低充值金额")
    private BigDecimal minAntUsdtDepositAmount;
}