package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@ApiModel(description = "充值列表返回")
public class CollectionOrderInfoDTO implements Serializable {


    @ApiModelProperty("主键")
    private Long id;
    @ApiModelProperty("upi")
    private String upiId;
    @ApiModelProperty("upi name")
    private String upiName;


    /**
     * 交易回调时间
     */
    @ApiModelProperty("回调时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;


    /**
     * 凭证
     */
    @ApiModelProperty("凭证")
    private String voucher;

    @ApiModelProperty("支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("完成时间")
    private LocalDateTime completionTime;
    @ApiModelProperty("手动完成人")
    private String completedBy;
    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealReviewTime;
    @ApiModelProperty("审核人")
    private String appealReviewBy;
    @ApiModelProperty("取消时间")
    private LocalDateTime cancelTime;
    @ApiModelProperty("取消人")
    private String cancelBy;

    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("uid")
    private String memberId;

    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String mobileNumber;

    /**
     * UTR
     */
    @ApiModelProperty("utr")
    private String utr;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间或者匹配时间")
    private LocalDateTime createTime;

    /**
     * 随机码
     */
    @ApiModelProperty(value = "支付随机码")
    private String randomCode;
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

}