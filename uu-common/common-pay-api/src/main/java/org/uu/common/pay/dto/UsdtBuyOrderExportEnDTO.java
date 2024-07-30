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
public class UsdtBuyOrderExportEnDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 会员id
     */
    @ApiModelProperty("memberId")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("memberAccount")
    private String memberAccount;

    /**
     * 订单号
     */
    @ApiModelProperty("platformOrder")
    private String platformOrder;


    /**
     * USDT地址
     */
    @ApiModelProperty("usdtAddr")
    private String usdtAddr;

    /**
     * ARB数量
     */
    @ApiModelProperty("ITokenNum")
    private BigDecimal arbNum;

    /**
     * 支付方式
     */
    @ApiModelProperty("payType")
    private String payType;

    /**
     * 汇率
     */
    @ApiModelProperty("exchangeRates")
    private BigDecimal exchangeRates;

    /**
     * USDT数量
     */
    @ApiModelProperty("usdtNum")
    private BigDecimal usdtNum;

    /**
     * USDT实际数量
     */
    @ApiModelProperty("usdtActualNum")
    private BigDecimal usdtActualNum;

    /**
     * 创建时间
     */
    @ApiModelProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     * 订单状态 默认值: 待支付
     */
    @ApiModelProperty("status")
    private String status;

    /**
     * 创建人
     */
    @ApiModelProperty("createBy")
    private String createBy;


    /**
     * 修改人
     */
    @ApiModelProperty("updateBy")
    private String updateBy;

    /**
     * 修改时间
     */
    @ApiModelProperty("updateTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


    @ApiModelProperty("remark")
    private String remark;

    @ApiModelProperty("txid")
    private String txid;
}