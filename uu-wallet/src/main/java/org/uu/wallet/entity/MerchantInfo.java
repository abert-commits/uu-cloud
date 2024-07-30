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

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_info")
public class MerchantInfo extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 账号
     */
    private String account;

    /**
     * 状态
     */
    private String status;

    /**
     * 商户编码
     */
    private String code;

    /**
     * 商户公钥
     */
    private String merchantPublicKey;

    /**
     * 平台公钥
     */
    private String platformPublicKey;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 国家
     */
    private String country;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 回调地址
     */
    private String notifyUrl;

    @TableField(exist = false)
    @Size(min = 1, message = "roleIds 不能为空")
    private List<Long> roleIds;

    @TableField(exist = false)
    private List<Long> menuIds;

    @TableField(exist = false)
    private List<String> permissions;

    @TableField(exist = false)
    private List<String> roles;

    private String deleted;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 登录账号
     */
    private String nickname;

    /**
     * 白名单
     */
    private String whiteList;

    /**
     * md5Key
     */
    private String md5Key;

    /**
     * 谷歌身份验证密钥
     */
    private String googlesecret;

    /**
     * 商家网址
     */
    private String website;

    /**
     * 登录次数
     */
    private Integer logins;

    /**
     * 支付费率
     */
    private BigDecimal payRate;

    /**
     * 代付费率
     */
    private BigDecimal transferRate;

    /**
     * 下发usdt地址
     */
    private String usdtAddress;

    /**
     * 币种
     */
    private String currency;

    /**
     * 登录IP
     */
    @ApiModelProperty(value = "登录IP")
    private String loginIp;

    /**
     * 总下分次数: 统计商户自己下分次数
     */
    @ApiModelProperty(value = "总下分次数")
    private Long transferDownCount;

    /**
     * 总下分金额: 统计商户自己提现金额
     */
    @ApiModelProperty(value = "总下分金额")
    private BigDecimal transferDownAmount;

    /**
     * 总上分次数: 统计商户自己上分次数
     */
    @ApiModelProperty(value = "总上分次数")
    private Long transferUpCount;

    /**
     * 总上分金额: 统计商户自己充值金额
     */
    @ApiModelProperty(value = "总上分金额")
    private BigDecimal transferUpAmount;

    /**
     * 总提现金额
     */
    @ApiModelProperty(value = "总提现金额(总代付金额统计)")
    private BigDecimal totalWithdrawAmount;

    /**
     * 总充值金额
     */
    @ApiModelProperty(value = "总充值金额(总代收金额统计)")
    private BigDecimal totalPayAmount;

    /**
     * 总充值次数(总代收次数)
     */
    @ApiModelProperty(value = "总充值次数(总代收次数)")
    private Long totalPayCount;

    /**
     * 总提现次数(总代付次数)
     */
    @ApiModelProperty(value = "总提现次数(总代付次数)")
    private Long totalWithdrawCount;

    /**
     * 支付总手续费(代收总手续费)
     */
    @ApiModelProperty(value = "支付总手续费(代收总手续费)")
    private BigDecimal totalPayFee;

    /**
     * 提现总手续费(代付总手续费)
     */
    @ApiModelProperty(value = "提现总手续费(代付总手续费)")
    private BigDecimal totalWithdrawFee;

    /**
     * 商户类型
     */
    private String merchantType;


    private Integer rechargeMatchExptime;

    private Integer withdrawalMatchExptime;

    private Integer confimExptime;

    @ApiModelProperty(value = "备注")
    private String remark;
    @ApiModelProperty(value = "最大金额")
    private BigDecimal maxCost;
    @ApiModelProperty(value = "最小金额")
    private BigDecimal minCost;
    @ApiModelProperty(value = "充值奖励")
    private BigDecimal rechargeReward;
    @ApiModelProperty(value = "出款奖励")
    private BigDecimal withdrawalRewards;
    @ApiModelProperty(value = "充值状态")
    private String rechargeStatus;
    @ApiModelProperty(value = "出款状态")
    private String withdrawalStatus;
    @ApiModelProperty(value = "时区")
    private String timeZone;

    @ApiModelProperty(value = "上次登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "密码提示")
    private String passwordTips;

    @ApiModelProperty(value = "API接口IP白名单, 多个IP以英文逗号, 分割")
    private String apiAllowedIps;

    @ApiModelProperty(value = "是否绑定谷歌验证码: 1-是 0-否")
    private Integer isBindGoogle;

    @ApiModelProperty(value = "快捷金额")
    private String quickAmount;
    @ApiModelProperty(value = "图标")
    private String icon;

    @TableField(exist = false)
    @ApiModelProperty(value = "商户余额总计")
    private BigDecimal balanceTotal;
    @TableField(exist = false)
    @ApiModelProperty(value = "trx余额总计")
    private BigDecimal trxBalanceTotal;
    @TableField(exist = false)
    @ApiModelProperty(value = "usdt余额统计")
    private BigDecimal usdtBalanceTotal;


    @ApiModelProperty(value = "充值奖励比例")
    private BigDecimal rechargeRewardRatio;


    /**
     * 余额退回冻结金额
     */
    @ApiModelProperty(value = "余额退回冻结金额")
    private BigDecimal cashBackFrozenAmount;


    /**
     * api地址
     */
    private String apiUrl;

    /**
     * 币种表system_currency的主键id
     */
    private Long currencyId;


    /**
     * USDT余额
     */
    private BigDecimal usdtBalance;


    /**
     * TRX余额
     */
    private BigDecimal trxBalance;


    /**
     * 法币交易中金额
     */
    private BigDecimal pendingBalance;

    /**
     * USDT交易中金额
     */
    private BigDecimal pendingUsdtBalance;

    /**
     * TRX交易金额
     */
    private BigDecimal pendingTrxBalance;

}