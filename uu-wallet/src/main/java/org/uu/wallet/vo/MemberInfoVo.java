package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "会员信息")
public class MemberInfoVo implements Serializable {


    private Long id;

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "会员ID")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty(value = "会员账号")
    private String memberAccount;

    /**
     * 手机号码
     */
    @ApiModelProperty(value = "手机号码")
    private String mobileNumber;

    /**
     * 真实姓名
     */
    @ApiModelProperty(value = "真实姓名")
    private String realName;

    /**
     * 会员类型
     */
    @ApiModelProperty(value = "会员类型")
    private String memberType;

    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * 买入次数
     */
    @ApiModelProperty(value = "买入次数")
    private BigDecimal buyNumber;

    /**
     * 买入金额
     */
    @ApiModelProperty(value = "买入金额")
    private String buyAmount;

    /**
     * 卖出次数
     */
    @ApiModelProperty(value = "卖出次数")
    private BigDecimal sellNumber;

    /**
     * 卖出金额
     */
    @ApiModelProperty(value = "卖出金额")
    private String sellAmount;

    /**
     * 买入奖励比率
     */
    @ApiModelProperty(value = "买入奖励比率")
    private String buyBonusProportion;

    /**
     * 卖出奖励比率
     */
    @ApiModelProperty(value = "卖出奖励比率")
    private String sellBonusProportion;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private String status;

    /**
     * 在线状态
     */
    @ApiModelProperty(value = "在线状态")
    private String onlineStatus;

    /**
     * 买入状态
     */
    @ApiModelProperty(value = "买入状态")
    private String buyStatus;

    /**
     * 卖出状态
     */
    @ApiModelProperty(value = "卖出状态")
    private String sellStatus;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String email;

    /**
     * 证件号
     */
    @ApiModelProperty(value = "证件号")
    private String idCardNumber;


    /**
     * 钱包地址
     */
    @ApiModelProperty("钱包地址")
    private String walletAddress;
}