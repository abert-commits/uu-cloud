package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "ustd买入订单返回")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UsdtBuyOrderExportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;

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
     * ARB数量
     */
    @ApiModelProperty("IToken数量")
    private BigDecimal arbNum;

    /**
     * 支付方式
     */
    @ApiModelProperty("支付方式")
    private String payType;

    /**
     * 汇率
     */
    @ApiModelProperty("汇率")
    private BigDecimal exchangeRates;

    /**
     * USDT数量
     */
    @ApiModelProperty("USDT数量")
    private BigDecimal usdtNum;

    /**
     * USDT实际数量
     */
    @ApiModelProperty("实际金额")
    private BigDecimal usdtActualNum;

    /**
     * 创建时间
     */
    @ApiModelProperty("订单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     * 订单状态 默认值: 待支付
     */
    @ApiModelProperty("订单状态")
    private String status;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;


    /**
     * 修改人
     */
    @ApiModelProperty("更新人")
    private String updateBy;

    /**
     * 修改时间
     */
    @ApiModelProperty("操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("交易id")
    private String txid;
}