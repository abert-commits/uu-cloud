package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.uu.wallet.Enum.PayTypeEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_account_change")
public class MemberAccountChange implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private String mid;

    /**
     * 币种
     */
    private String currentcy;

    /**
     * 账变类型：add-增加, sub-支出
     */
    private String changeMode;

    /**
     * 账变类型: 1-买入, 2-卖出, 3-usdt充值,4-人工上分,5-人工下分
     */
    private String changeType;

    /**
     * 平台订单号
     */
    private String orderNo;

    /**
     * 商户订单号
     */
    private String merchantOrder;

    /**
     * 账变前
     */
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    private BigDecimal amountChange;

    /**
     * 商户会员ID
     */
    private String memberId;

    /**
     * 所属商户
     */
    private String merchantName;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 会员账号
     */
    private String remark;


    /**
     * 账变后金额
     */
    private BigDecimal afterChange;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;


    @TableField(exist = false)
    private BigDecimal beforeChangeTotal;

    @TableField(exist = false)
    private BigDecimal afterChangeTotal;

    @TableField(exist = false)
    private BigDecimal amountChangeTotal;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 返佣标识 0-未返佣 1-已返佣
     */
    private Integer commissionFlag;
}