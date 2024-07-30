package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("apply_distributed")
public class ApplyDistributed implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商户
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String username;

    /**
     * 下发usdt地址
     */
    private String usdtAddr;

    /**
     * 币种
     */
    private String currence;

    /**
     * 总额度
     */
    private BigDecimal balance;

    private BigDecimal amount;


    private String remark;

    /**
     * 状态
     */
    private String status;


    private String type;

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

    @TableField(exist = false)
    private BigDecimal amountTotal;

    @TableField(exist = false)
    private BigDecimal balanceTotal;

    /**
     * 商户下发余额类型：3:商户的选择法币，1:USDT, 2:TRX
     */
    private String payType;
}