package org.uu.manager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Admin
 */
@Data
@ApiModel(description = "账变返回")
public class AccountChangeDTO implements Serializable {


//    /**
//     * 账变类型
//     */
//    private Integer changeType;

    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;


    /**
     * 商户名
     */
    @ApiModelProperty("商户名")
    private String merchantName;


    /**
     * 商户订单
     */
    @ApiModelProperty("商户订单")
    private String merchantOrder;






    /**
     * 手续费
     */
    @ApiModelProperty("手续费")
    private BigDecimal commission;




    /**
     * 商户订单号
     */
    @ApiModelProperty("商户订单号")
    private String orderNo;

    /**
     * 账变前
     */
    @ApiModelProperty("账变前")
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    @ApiModelProperty("变化金额")
    private BigDecimal amountChange;


    /**
     * 账变后金额
     */
    @ApiModelProperty("账变后金额")
    private BigDecimal afterChange;

    /**
     * 创建时间
     */
    @ApiModelProperty("订单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     * 账变类型: 1-代收, 2-代付, 3-下发,4-上分
     */
    @ApiModelProperty("账变类型 1-代收, 2-代付, 3-下发,4-上分,5-代收费用,6-代付费用,7-下发回退")
    private Integer changeType;


    /**
     * 支付通道
     */
    @ApiModelProperty("支付通道")
    private String paymentChannel;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("usdt地址")
    private String usdtAddr;

    /**
     * USDT余额
     */
    @ApiModelProperty("USDT余额")
    private BigDecimal usdtBalance;


    /**
     * TRX余额
     */
    @ApiModelProperty("TRX余额")
    private BigDecimal trxBalance;





}
