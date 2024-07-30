package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.wallet.Enum.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_info")
public class MemberInfo extends BaseEntityOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobileNumber;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 会员类型
     */
    private String memberType;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 累计买入次数
     */
    private Integer totalBuyCount;

    /**
     * 累计卖出次数
     */
    private Integer totalSellCount;

    /**
     * 累计买入金额
     */
    private BigDecimal totalBuyAmount;

    /**
     * 累计卖出金额
     */
    private BigDecimal totalSellAmount;

    /**
     * 累计买入成功次数
     */
    private Integer totalBuySuccessCount;

    /**
     * 累计卖出成功次数
     */
    private Integer totalSellSuccessCount;

    /**
     * 累计买入成功金额
     */
    private BigDecimal totalBuySuccessAmount;

    /**
     * 累计卖出成功金额
     */
    private BigDecimal totalSellSuccessAmount;

    /**
     * 累计买入奖励
     */
    private BigDecimal totalBuyBonus;

    /**
     * 累计卖出奖励
     */
    private BigDecimal totalSellBonus;

    /**
     * 被申诉次数
     */
    private Integer appealCount;
    
    /**
     * 买入奖励比例
     */
    private BigDecimal buyBonusProportion;

    /**
     * 卖出奖励比例
     */
    private BigDecimal sellBonusProportion;

    /**
     * 补充：平台分红
     */
    private BigDecimal platformDividends;

    /**
     * 状态 默认值 启用
     */
    private String status = MemberStatusEnum.ENABLE.getCode();

    /**
     * 在线状态 默认值 离线
     */
    private String onlineStatus = MemberOnlineStatusEnum.OFF_LINE.getCode();

    /**
     * 买入状态 默认值 开启
     */
    private String buyStatus = BuyStatusEnum.ENABLE.getCode();

    /**
     * 卖出状态 默认值 开启
     */
    private String sellStatus = SellStatusEnum.ENABLE.getCode();

    /**
     * 委托状态: 默认 委托失败
     */
    private Integer delegationStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * UPI_ID
     */
    private String upiId;

    /**
     * UPI_Name
     */
    private String upiName;

    /**
     * 是否删除 默认值: 0
     */
    private String deleted = "0";

    /**
     * 注册ip
     */
    private String registerIp;

    /**
     * 注册设备
     */
    private String registerDevice;

    /**
     * 首次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstLoginTime;

    /**
     * 首次登录IP
     */
    private String firstLoginIp;


    /**
     * 登录ip
     */
    private String loginIp;

    /**
     * 邀请码
     */
    private String invitationCode;

    /**
     * 上级邀请码
     */
    private String referrerCode;

    /**
     * 邮箱账号
     */
    private String emailAccount;

    /**
     * 身份证号
     */
    private String idCardNumber;

    /**
     * 正在进行中的卖出订单数量
     */
    private Integer activeSellOrderCount;

    /**
     * 证件图片
     */
    private String idCardImage;

    /**
     * 钱包地址
     */
    private String walletAddress;

    /**
     * 分组 默认值: 1(默认分组)
     */
    private Long memberGroup = 1L;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private Integer avatar;

    /**
     * 支付密码
     */
    private String paymentPassword;

    /**
     * 支付密码提示语
     */
    private String paymentPasswordHint;

    /**
     * 人脸照片
     */
    private String facePhoto;

    /**
     * 实名认证状态
     */
    private String authenticationStatus;

    /**
     * 实名认证时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime realNameVerificationTime;

    /**
     * 手动认证人员
     */
    @ApiModelProperty("手动认证人员")
    private String verificationBy;

    /**
     * 后台冻结金额
     */
    @ApiModelProperty("后台冻结金额")
    private BigDecimal biFrozenAmount;

    /**
     * 后台冻结金额
     */
    @ApiModelProperty("人数")
    @TableField(exist = false)
    private BigDecimal num = BigDecimal.ZERO;

    /**
     * 充值次数
     */
    @ApiModelProperty("充值次数")
    private Long rechargeNum = 0L;

    /**
     * 累计充值金额
     */
    @ApiModelProperty("累计充值金额")
    private BigDecimal rechargeTotalAmount = BigDecimal.ZERO;


    /**
     * 提现次数
     */
    @ApiModelProperty("提现次数")
    private Long withdrawNum = 0L;


    /**
     * 累计提现金额
     */
    @ApiModelProperty("累计提现金额")
    private BigDecimal withdrawTotalAmount = BigDecimal.ZERO;


    /**
     * 累计领取任务奖励金额
     */
    @ApiModelProperty("累计领取任务奖励金额")
    private BigDecimal totalTaskRewards;


    /**
     * 今日买入成功次数
     */
    @ApiModelProperty("今日买入成功次数")
    private Integer todayBuySuccessCount;


    /**
     * 今日买入成功金额
     */
    @ApiModelProperty("今日买入成功金额")
    private BigDecimal todayBuySuccessAmount;


    /**
     * 今日卖出成功次数
     */
    @ApiModelProperty("今日卖出成功次数")
    private Integer todaySellSuccessCount;


    /**
     * 今日卖出成功金额
     */
    @ApiModelProperty("今日卖出成功金额")
    private BigDecimal todaySellSuccessAmount;

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
     * 等级
     */
    @ApiModelProperty("变化前等级")
    @TableField(exist = false)
    private Integer beforeLevel;

    @TableField(exist = false)
    @ApiModelProperty(value = "可用余额总计")
    private BigDecimal balanceTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "买入金额总计")
    private BigDecimal totalBuyAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "卖出金额总计")
    private BigDecimal totalSellAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "交易中的金额总计")
    private BigDecimal frozenAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "冻结余额总计")
    private BigDecimal biFrozenAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "充值金额总计")
    private BigDecimal rechargeAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "提现金额总计")
    private BigDecimal withdrawAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "买入奖励总计")
    private BigDecimal totalBuyBonusTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "卖出奖励总计")
    private BigDecimal totalSellBonusTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "团队奖励总计")
    private BigDecimal teamRewardsTotal;

    /**
     * 平台分红总计
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "平台分红总计")
    private BigDecimal platformDividendsTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "总奖励总计")
    private BigDecimal totalBonusTotal;




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
     * 退回冻结金额
     */
    @ApiModelProperty(value = "退回冻结金额")
    private BigDecimal cashBackFrozenAmount;

    /**
     * 新手卖出教程状态
     */
    @ApiModelProperty(value = "余额退回弹窗 0:未完成 1:已完成 ")
    private Integer cashBackAttention;

    /**
     * 蚂蚁层级
     */
    @ApiModelProperty("蚂蚁层级")
    private Integer antLevel;

    /**
     * 累计买入返佣金额
     */
    @ApiModelProperty(value = "累计买入返佣金额")
    private BigDecimal totalBuyCommissionAmount;

    /**
     * 累计买入返佣金额
     */
    @ApiModelProperty(value = "累计卖出返佣金额")
    private BigDecimal totalSellCommissionAmount;

    /**
     * 分红等级 0-未分红
     */
    @ApiModelProperty(value = "分红等级 0-未分红")
    private Integer dividendsLevel;

    /**
     * 对应upi kyc数量
     */
    @ApiModelProperty(value = "对应upi kyc数量")
    private Integer totalKycPartner;

    /**
     * 委托中金额
     */
    private BigDecimal delegatedAmount;
}