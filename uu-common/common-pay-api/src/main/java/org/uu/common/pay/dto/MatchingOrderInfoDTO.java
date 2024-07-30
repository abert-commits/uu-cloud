package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 匹配订单记录表
 *
 * @author
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@ApiModel(description = "匹配订单返回")
public class MatchingOrderInfoDTO {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String mobileNumber;


    /**
     * 凭证
     */
    private String voucher;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;


    /**
     * UPI_ID
     */
    @ApiModelProperty("UPI ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty("UPI NAME")
    private String upiName;

    @ApiModelProperty("UTR")
    private String utr;


    /**
     * 状态
     */
    @ApiModelProperty("订单状态")
    private String status;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("匹配时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private String updateBy;

    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;
    /**
     * 完成人
     */
    @ApiModelProperty("完成人")
    private String completionBy;
    /**
     *
     */
    @ApiModelProperty("审核人")
    private String appealReviewBy;
    @ApiModelProperty("审核人时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealReviewTime;

    /**
     * 取消人
     */
    @ApiModelProperty("取消人")
    private String cancelBy;

    /**
     * 取消人
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("取消时间")
    private LocalDateTime cancelTime;


    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("是否开启人工审核")
    private Integer isManualReview;

    @ApiModelProperty("随机码")
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