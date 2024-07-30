package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "买入订单详情")
public class BuyOrderDetailsVo implements Serializable {

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 10: 买入失败, 11: 金额错误, 13: 支付超时")
    private String orderStatus;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 实际金额
     */
    @ApiModelProperty("实际金额")
    private BigDecimal actualAmount;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    /**
     * 金额错误提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "金额错误提交时间")
    private LocalDateTime amountErrorSubmitTime;

    /**
     * UTR
     */
    @ApiModelProperty(value = "UTR")
    private String utr;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;

    /**
     * 奖励
     */
    @ApiModelProperty(value = "奖励")
    private BigDecimal bonus;

    /**
     * UPI_ID
     */
    @ApiModelProperty("UPI ID")
    private String upiId;

    /**
     * upiName
     */
    @ApiModelProperty("upiName")
    private String upiName;

    /**
     * 支付凭证
     */
    @ApiModelProperty(value = "支付凭证")
    private String voucher;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "支付时间")
    private LocalDateTime paymentTime;

    /**
     * 金额错误图片
     */
    @ApiModelProperty(value = "金额错误图片 多张图片以 ,逗号分割")
    private String amountErrorImage;

    /**
     * 金额错误视频
     */
    @ApiModelProperty(value = "金额错误视频")
    private String amountErrorVideo;

    /**
     * 确认中剩余时间
     */
    @ApiModelProperty(value = "确认中剩余时间  单位: 秒  如果值为null或负数 表示该笔订单已过期")
    private Long confirmExpireTime;

    /**
     * 待支付剩余时间
     */
    @ApiModelProperty(value = "待支付剩余时间  单位: 秒  如果值为null或负数 表示该笔订单已过期")
    private Long paymentExpireTime;

    /**
     * 取消原因
     */
    @ApiModelProperty(value = "取消原因")
    private String cancellationReason;

    /**
     * 是否申诉 默认值 0
     */
    @ApiModelProperty(value = "是否经过申诉, 取值说明: 0: 未申诉, 1: 已申诉")
    private Integer isAppealed = 0;


    /**
     * 失败原因
     */
    @ApiModelProperty(value = "失败原因")
    private String remark;

    /**
     * 是否人工审核状态
     */
    @ApiModelProperty(value = "是否人工审核状态 true-是 false-否")
    private Boolean isAuditingStatus = Boolean.FALSE;

    /**
     * 人工审核截至时间(分钟)
     */
    @ApiModelProperty("人工审核截至时间(分钟)")
    private Long delayMinutes;

    /**
     * 申诉类型
     */
    @ApiModelProperty(value = "申诉类型 1: 未到账  2: 金额错误")
    private Integer displayAppealType = 1;

    /**
     * 支付方式
     */
    @ApiModelProperty("支付方式 1: 银行卡, 3: UPI")
    private String payType;

    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人姓名")
    private String bankCardOwner;


    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;


    /**
     * ifsc_code
     */
    @ApiModelProperty("ifsc_code")
    private String ifscCode;

    /**
     * 申诉原因
     */
    @ApiModelProperty(value = "申诉原因")
    private String reason;

    @ApiModelProperty("用户类型 1-内部 2-外部")
    private Integer memberType;

    @ApiModelProperty(value = "KYC账户")
    private String kycAccount;

    @ApiModelProperty(value = "KYC银行名称")
    private String kycBankName;
}