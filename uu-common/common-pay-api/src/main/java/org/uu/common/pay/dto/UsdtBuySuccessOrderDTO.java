package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "ustd买入交易成功订单返回")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsdtBuySuccessOrderDTO implements Serializable {

    private Long id;


    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 商户ID
     */
    private String merchantId;

    @ApiModelProperty("交易id")
    private String txid;

    /**
     * 订单号
     */
    @ApiModelProperty("平台订单号")
    private String platformOrder;


    /**
     * USDT地址
     */
    @ApiModelProperty("收款地址")
    private String usdtAddr;

    /**
     * 该会员操作转款的自己的USDT地址
     */
    @ApiModelProperty("会员操作转款的自己的USDT地址")
    private String memberUsdtAddr;


    /**
     * USDT数量
     */
    @ApiModelProperty("USDT数量")
    private BigDecimal usdtNum;


    /**
     * 转账时间
     */
    @ApiModelProperty("转账时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;
}