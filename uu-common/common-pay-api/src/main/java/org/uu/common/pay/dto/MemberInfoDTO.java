package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "会员信息表")
public class MemberInfoDTO implements Serializable {
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;


    /**
     * 真实姓名
     */
    @ApiModelProperty("真实姓名")
    private String realName;

    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String mobileNumber;

    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 会员类型
     */
    @ApiModelProperty("会员类型")
    private String memberType;

    /**
     * 余额
     */
    @ApiModelProperty("余额")
    private BigDecimal balance;

    /**
     * 交易中金额
     */
    @ApiModelProperty("交易中金额")
    private BigDecimal frozenAmount;


    /**
     * 冻结金额
     */
    @ApiModelProperty("后台冻结金额")
    private BigDecimal biFrozenAmount;

    /**
     * 累计买入金额
     */

    @ApiModelProperty("累计买入金额")
    private BigDecimal totalBuyAmount;

    /**
     * 累计卖出金额
     */
    @ApiModelProperty("累计卖出金额")
    private BigDecimal totalSellAmount;

    /**
     * 累计买入成功次数
     */
    @ApiModelProperty("累计买入成功次数")
    private Integer totalBuySuccessCount;

    /**
     * 累计卖出成功次数
     */
    @ApiModelProperty("累计卖出成功次数")
    private Integer totalSellSuccessCount;

    /**
     * 累计买入奖励
     */
    @ApiModelProperty("累计买入奖励")
    private BigDecimal totalBuyBonus;

    /**
     * 累计卖出奖励
     */
    @ApiModelProperty("累计卖出奖励")
    private BigDecimal totalSellBonus;


    /**
     * 买入奖励比例
     */
    @ApiModelProperty("买入奖励比例")
    private BigDecimal buyBonusProportion;

    /**
     * 卖出奖励比例
     */
    @ApiModelProperty("卖出奖励比例")
    private BigDecimal sellBonusProportion;

    /**
     * 状态 默认值 启用
     */
    @ApiModelProperty("状态")
    private String status;

    /**
     * 在线状态 默认值 离线
     */
    @ApiModelProperty("在线状态")
    private String onlineStatus;

    /**
     * 买入状态 默认值 开启
     */
    @ApiModelProperty("买入状态")
    private String buyStatus;

    /**
     * 卖出状态 默认值 开启
     */
    @ApiModelProperty("卖出状态")
    private String sellStatus;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    /**
     * UPI_ID
     */
    @ApiModelProperty("UPI_ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty("UPI_Name")
    private String upiName;

    /**
     * 是否删除 默认值: 0
     */
    @ApiModelProperty("是否删除")
    private String deleted = "0";

    /**
     * 注册ip
     */
    @ApiModelProperty("注册ip")
    private String registerIp;

    /**
     * 注册设备
     */
    @ApiModelProperty("注册设备")
    private String registerDevice;

    /**
     * 首次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("首次登录时间")
    private LocalDateTime firstLoginTime;

    /**
     * 登录ip
     */
    @ApiModelProperty("登录ip")
    private String loginIp;

    /**
     * 邀请码
     */
    @ApiModelProperty("邀请码")
    private String invitationCode;

    /**
     * 邮箱账号
     */
    @ApiModelProperty("邮箱账号")
    private String emailAccount;

    /**
     * 身份证号
     */
    @ApiModelProperty("身份证号")
    private String idCardNumber;

    /**
     * 正在进行中的卖出订单数量
     */
    @ApiModelProperty("正在进行中的卖出订单数量")
    private Integer activeSellOrderCount;

    /**
     * 证件图片
     */
    @ApiModelProperty("证件图片")
    private String idCardImage;

    /**
     * 钱包地址
     */
    @ApiModelProperty("钱包地址")
    private String walletAddress;

    @ApiModelProperty("累计买入次数")
    private Integer totalBuyCount;

    @ApiModelProperty("累计卖出次数")
    private Integer totalSellCount;



    @ApiModelProperty("被申诉次数")
    private Integer appealCount;

    /**
     * 首次登录IP
     */
    @ApiModelProperty("首次登录时间")
    private String firstLoginIp;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("注册时间")
    private LocalDateTime registerTime;


    /**
     * 商户会员ip
     */
    @ApiModelProperty("商户会员ip")
    private String merchantMemberIp;

    /**
     * 信用分
     */
    @ApiModelProperty("信用分")
    private BigDecimal creditScore;

    /**
     * 等级
     */
    @ApiModelProperty("等级")
    private Integer level;

    /**
     * 人数
     */
    @ApiModelProperty("人数")
    private BigDecimal num = BigDecimal.ZERO;

    /**
     * 商户会员ID
     */
    @ApiModelProperty(value = "商户会员ID")
    private String externalMemberId;

    /**
     * 上级邀请码
     */
    @ApiModelProperty(value = "上级邀请码")
    private String referrerCode;
}