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
@ApiModel(description = "我的页面返回数据")
public class MemberInformationVo implements Serializable {

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
     * 会员类型
     */
    @ApiModelProperty(value = "会员类型, 取值说明: 1: 内部会员, 2: 外部会员")
    private String memberType;

    /**
     * 钱包地址
     */
    @ApiModelProperty(value = "钱包地址")
    private String walletAddress;

    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * INR余额
     */
    @ApiModelProperty(value = "INR余额")
    private BigDecimal inrBalance;

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
     * 通知数
     */
    @ApiModelProperty(value = "通知数")
    private Integer notificationCount;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    /**
     * 通知数
     */
    @ApiModelProperty(value = "用户头像")
    private Integer avatar;

    /**
     * 是否设置了支付密码
     */
    @ApiModelProperty(value = "是否设置了支付密码, 取值说明:  0: 未设置, 1:已设置")
    private Integer hasPaymentPassword = 0;

    /**
     * 实名认证时间
     */
    @ApiModelProperty(value = "实名认证时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime realNameVerificationTime;

    /**
     * 实名认证状态
     */
    @ApiModelProperty("实名认证状态: 1-已认证 0-未认证")
    private String authenticationStatus;


    /**
     * 支付密码提示语
     */
    @ApiModelProperty(value = "支付密码提示语")
    private String paymentPasswordHint;


    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;


    /**
     * 商户logo
     */
    @ApiModelProperty(value = "商户图标")
    private String merchantIcon;


    /**
     * 快捷金额
     */
    @ApiModelProperty(value = "快捷金额")
    private String quickAmount;


    @ApiModelProperty(value = "充值奖励比例")
    private BigDecimal rechargeRewardRatio;

    /**
     * 补充字段 平台分红
     * @author pom
     */
    @ApiModelProperty(value = "平台分红")
    private BigDecimal platformDividends;


    /**
     * 任务活动开关
     */
    @ApiModelProperty(value = "任务活动开关 1: 开启, 0: 关闭")
    private String taskSwitch = "0";

    /**
     * 会员等级
     */
    @ApiModelProperty(value = "会员等级: 0-普通 1-青铜 2-白银 3-黄金 4-铂金 5-钻石")
    private Integer level;

    @ApiModelProperty(value = "可否自选买入 1:可以 0不可以")
    private Integer selfSelectionBuy;

    /**
     * 快捷买入单次最大限额
     */
    @ApiModelProperty(value = "快捷买入单次最大限额")
    private String quickBuyMaxLimit;

    /**
     * 快捷买入单次最小限额
     */
    @ApiModelProperty(value = "快捷买入单次最小限额")
    private String quickBuyMinLimit;

    /**
     * 新手买入教程状态
     */
    @ApiModelProperty(value = "新手买入教程状态 0:未完成 1:已完成 ")
    private Integer buyGuideStatus;

    /**
     * 新手卖出教程状态
     */
    @ApiModelProperty(value = "新手卖出教程状态 0:未完成 1:已完成 ")
    private Integer sellGuideStatus;


    /**
     * 累计买入成功次数
     */
    @ApiModelProperty(value = "累计买入成功次数")
    private Integer totalBuySuccessCount;

    /**
     * 累计卖出成功次数
     */
    @ApiModelProperty(value = "累计卖出成功次数")
    private Integer totalSellSuccessCount;

    /**
     * 累计买入次数
     */
    @ApiModelProperty(value = "累计买入次数")
    private Integer totalBuyCount;

    /**
     * 信用分
     */
    @ApiModelProperty("信用分")
    private BigDecimal creditScore;

    /**
     * 交易信用分限制
     */
    @ApiModelProperty("交易信用分限制")
    private BigDecimal tradeCreditScoreLimit;

    /**
     * 买入状态 默认值 开启
     */
    private String buyStatus;

    /**
     * 卖出状态 默认值 开启
     */
    private String sellStatus;

    /**
     * 状态 默认值 启用
     */
    private String status;


    /**
     * 交易中金额
     */
    @ApiModelProperty("交易中金额")
    private BigDecimal frozenAmount;

    /**
     * 后台冻结金额
     */
    @ApiModelProperty("后台冻结金额")
    private BigDecimal biFrozenAmount;

    /**
     * 后台冻结金额
     */
    @ApiModelProperty("是否余额退回 0-未退回 1-已退回")
    private Integer isCashBack = 0;


    /**
     * 银行卡号长度限制
     */
    @ApiModelProperty("银行卡号长度限制 实例: 0-25")
    private String bankCardNumberLengthLimit = "0-25";

    /**
     * 买入奖励比例
     */
    @ApiModelProperty("买入奖励比例")
    private BigDecimal buyBonusProportion;

    @ApiModelProperty("INR买入IToken汇率")
    private BigDecimal inrBuyItokenRatio;

    @ApiModelProperty("USDT买入IToken汇率")
    private BigDecimal usdtBuyItoken;

}