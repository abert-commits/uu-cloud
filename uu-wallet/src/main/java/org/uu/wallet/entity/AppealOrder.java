package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.wallet.Enum.PayTypeEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 申诉订单
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("appeal_order")
public class AppealOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private String mid;

    /**
     * 会员账号
     */
    private String mAccount;

    /**
     * 所属商户code
     */
    private String belongMerchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 提现订单号
     */
    private String withdrawOrderNo;

    /**
     * 充值订单号
     */
    private String rechargeOrderNo;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 申诉类型: 1-提现申诉 2-充值申诉
     */
    private Integer appealType;

    /**
     * 申诉状态: 1-申诉中 2-已支付 3-未支付,4-金额错误
     */
    private Integer appealStatus;

    private BigDecimal actualAmount;

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
     * 申诉原因
     */
    private String reason;

    /**
     * 图片信息
     */
    private String picInfo;

    /**
     * 视频url
     */
    private String videoUrl;

    /**
     * UTR
     */
    private String utr;

    /**
     * 被申诉会员id
     */
    private String appealedMemberId;

    @TableField(exist = false)
    private Long actualAmountTotal;
    @TableField(exist = false)
    private Long orderAmountTotal;

    /**
     * 显示申诉类型 1: 未到账  2: 金额错误
     */
    private Integer displayAppealType;

    /**
     * 支付方式
     */
    private String payType;

}