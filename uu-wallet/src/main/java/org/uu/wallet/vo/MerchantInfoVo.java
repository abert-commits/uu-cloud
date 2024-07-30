package org.uu.wallet.vo;

import com.baomidou.mybatisplus.annotation.TableField;
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
@ApiModel(description = "商户信息")
public class MerchantInfoVo implements Serializable {


    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String username;

    /**
     * 账号
     */
    @ApiModelProperty(value = "商户账号")
    private String account;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private String status;

    /**
     * 商户编码
     */
    @ApiModelProperty(value = "商户编码")
    private String code;

    /**
     * 商户公钥
     */
    @ApiModelProperty(value = "商户公钥")
    private String publicKey;

    /**
     * 商户私钥
     */
    @ApiModelProperty(value = "商户私钥")
    private String privateKey;

    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * 国家
     */
    @ApiModelProperty(value = "国家")
    private String country;

    /**
     * 冻结金额
     */
    @ApiModelProperty(value = "冻结金额")
    private BigDecimal frozenAmount;

    /**
     * 回调地址
     */
    @ApiModelProperty(value = "回调地址")
    private String notifyUrl;

//    @Size(min = 1, message = "roleIds 不能为空")
//    @ApiModelProperty(value = "商户角色Id")
//    private List<Long> roleIds;

//    @TableField(exist = false)
//    @ApiModelProperty(value = "商户菜单Id")
//    private List<Long> menuIds;

//    @TableField(exist = false)
//    @ApiModelProperty(value = "商户权限")
//    private List<String> permissions;

    @TableField(exist = false)
//    @ApiModelProperty(value = "商户角色")
//    private List<String> roles;

    @ApiModelProperty(value = "删除状态")
    private String deleted;

    @ApiModelProperty(value = "移动电话")
    private String mobile;

    @ApiModelProperty(value = "电子邮件")
    private String email;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "白名单")
    private String whiteList;

    /**
     * 谷歌身份验证密钥
     */
    private String googlesecret;

    /**
     * 商家网址
     */
    @ApiModelProperty(value = "商家网址")
    private String website;

    /**
     * 登录次数
     */
    @ApiModelProperty(value = "登录次数")
    private Integer logins;

    /**
     * 支付费率
     */
    @ApiModelProperty(value = "代收费率")
    private String payRate;

    /**
     * 代付费率
     */
    @ApiModelProperty(value = "代付费率")
    private String transferRate;

    /**
     * 下发usdt地址
     */
    @ApiModelProperty(value = "下发usdt地址")
    private String usdtAddress;


    /**
     * 总下分次数: 统计商户自己下分次数
     */
    @ApiModelProperty(value = "总下分次数")
    private Integer transferDownCount;

    /**
     * 总下分金额: 统计商户自己提现金额
     */
    @ApiModelProperty(value = "总下分金额")
    private BigDecimal transferDownAmount;

    /**
     * 总上分次数: 统计商户自己上分次数
     */
    @ApiModelProperty(value = "总上分次数")
    private Integer transferUpCount;

    /**
     * 总上分金额: 统计商户自己充值金额
     */
    @ApiModelProperty(value = "总上分金额")
    private BigDecimal transferUpAmount;


    /**
     * 开通时间
     */
    @ApiModelProperty(value = "开通时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * md5Key
     */
    @ApiModelProperty(value = "md5Key")
    private String md5Key;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 总提现金额
     */
    @ApiModelProperty(value = "总提现金额")
    private BigDecimal totalWithdrawAmount;

    /**
     * 总充值金额
     */
    @ApiModelProperty(value = "总充值金额")
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

}