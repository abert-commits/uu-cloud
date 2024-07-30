package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收款信息
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "收款信息返回")
public class CollectionInfoDTO  implements Serializable {


    @ApiModelProperty("主键")
    private Long id;

    /**
     * UPI_ID
     */
    @ApiModelProperty("upiId")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty("UPI_Name")
    private String upiName;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String mobileNumber;

    /**
     * 日限额
     */
    @ApiModelProperty("日限额")
    private BigDecimal dailyLimitAmount;

    /**
     * 日限笔数
     */
    @ApiModelProperty("日限笔数")
    private Integer dailyLimitNumber;

    /**
     * 最小金额
     */
    @ApiModelProperty("最小金额")
    private BigDecimal minimumAmount;

    /**
     * 最大金额
     */
    @ApiModelProperty("最大金额")
    private BigDecimal maximumAmount;

    /**
     * 已收款金额
     */
    @ApiModelProperty("已收款金额")
    private BigDecimal collectedAmount;

    /**
     * 已收款次数
     */
    @ApiModelProperty("已收款次数")
    private Integer collectedNumber;

    @ApiModelProperty("会员账号")
    private String memberAccount;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 收款状态 默认值: 正常
     */
    @ApiModelProperty("收款状态: 1-正常,0-关闭")
    private String collectedStatus;



    /**
     * 付款类型 1-印度银行卡 2-印度USDT 3-印度upi 4-印度pix 5、upi/银行卡
     */
    @ApiModelProperty("付款类型 1-印度银行卡 2-印度USDT 3-印度upi 4-印度pix 5、upi/银行卡")
    private Integer type;


    /**
     * 银行编码
     */
    @ApiModelProperty("银行编码")
    private String bankCode;


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
     * email
     */
    @ApiModelProperty("邮箱")
    private String email;

    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人")
    private String bankCardOwner;
}