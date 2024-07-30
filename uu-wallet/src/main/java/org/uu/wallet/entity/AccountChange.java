package org.uu.wallet.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("account_change")
public class AccountChange implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 商户号
     */
    private String merchantCode;


    /**
     * 商户名
     */
    private String merchantName;

    /**
     * 平台订单号
     */
    private String merchantOrder;

    /**
     * 手续费
     */
    private BigDecimal commission;

    /**
     * 币种
     */
    private String currentcy;

    /**
     * 账变类型：add-增加, sub-支出
     */
    private String changeMode;

    /**
     * 账变类型: 1-代收, 2-代付, 3-下发,4-上分
     */
    private Integer changeType;

    /**
     * 商户订单号
     */
    private String orderNo;

    /**
     * 账变前
     */
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    private BigDecimal amountChange;

    /**
     * 支付通道
     */
    private String paymentChannel;


    /**
     * 账变后法币
     */
    private BigDecimal afterChange;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    @ApiModelProperty(value = "账变前额度总计")
    private BigDecimal beforeChangeTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "账变金额总计")
    private BigDecimal amountChangeTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "账变后额度总计")
    private BigDecimal afterChangeTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "手续费总计")
    private BigDecimal commissionTotal;

    /**
     * USDT充值地址
     */
    private String usdtAddr;
    /**
     * 账变后USDT
     */
    private BigDecimal usdtBalance;


    /**
     * 账变后TRX
     */
    private BigDecimal trxBalance;



}