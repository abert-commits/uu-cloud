package org.uu.common.pay.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ApiModel(description = "代付订单请求参数")
public class PaymentOrderInfoDTO{
    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI_ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "upiName")
    private String upiName;

    @ApiModelProperty("凭证")
    private String voucher;




    /**
     * 会员Id
     */
    @ApiModelProperty(value = "会员Id")
    private String memberId;




    /**
     * 订单状态
     */
    @ApiModelProperty(value = "支付状态")
    private String orderStatus;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;



    /**
     * 匹配时长
     */
    @ApiModelProperty(value = "匹配时长")
    private String matchDuration;


    @ApiModelProperty(value = "匹配时间")
    private String matchTime;


    @ApiModelProperty(value = "完成时长")
    private String completeDuration;


    @ApiModelProperty(value = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;



    @ApiModelProperty(value = "提现时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "申诉审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealReviewTime;

    @ApiModelProperty(value = "申诉审核人")
    private String appealReviewBy;

    @ApiModelProperty(value = "取消时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    @ApiModelProperty(value = "手机号码")
    private String mobileNumber;

    /**
     * 取消人
     */
    @ApiModelProperty(value = "取消人")
    private String cancelBy;

    /**
     * UTR
     */
    @ApiModelProperty(value = "utr")
    private String utr;




    /**
     * randomCode
     */
    @ApiModelProperty(value = "randomCode")
    private String randomCode = null;


    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人姓名
     */
    @ApiModelProperty("持卡人姓名")
    private String bankCardOwner;

    /**
     * ifscCode
     */
    @ApiModelProperty("ifscCode")
    private String ifscCode;

    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;

    /**
     * current_pay_type
     */
    @ApiModelProperty("当前支付类型")
    private String currentPayType;

}
