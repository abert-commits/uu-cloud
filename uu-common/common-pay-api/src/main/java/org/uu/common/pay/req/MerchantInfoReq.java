package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;
import java.math.BigDecimal;


/**
 * @author
 */
@Data
@ApiModel(description = "商户请求参数说明")
public class MerchantInfoReq extends PageRequest {



    @ApiModelProperty(value = "主键")
    private Long id;

    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String username;

    /**
     * 支付费率
     */
    @ApiModelProperty(value = "支付费率")
    private String payRate;

    /**
     * 代付费率
     */
    @ApiModelProperty(value = "代付费率")
    private String transferRate;
    /**
     * 商户编码
     */
    @ApiModelProperty(value = "appid就是商户号")
    private String code;


    @ApiModelProperty(value = "商户类型 1内部商户 2 外部商户")
    private String merchantType;

    @ApiModelProperty(value = "时区")
    private String timeZone;

    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "充值过期时间")
    private Integer rechargeMatchExptime;
    @ApiModelProperty(value = "出款过期时间")
    private Integer withdrawalMatchExptime;
    @ApiModelProperty(value = "确认过期时间")
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
    @ApiModelProperty(value = "充值状态 0禁止 1正常")
    private String rechargeStatus;
    @ApiModelProperty(value = "出款状态 0禁止 1正常")
    private String withdrawalStatus;


    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;

    /**
     * 账号
     */
    @ApiModelProperty(value = "商家号")
    private String account;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态 0禁用 1启用")
    private String status;


    /**
     * 商户公钥
     */
    @ApiModelProperty(value = "公钥")
    private String publicKey;

    /**
     * 商户私钥
     */
    @ApiModelProperty(value = "私钥匙")
    private String privateKey;



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




    /**
     * 手机号
     */
    @ApiModelProperty(value = "移动电话")
    private String mobile;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "电子邮箱")
    private String email;

    /**
     * 头像
     */
    @ApiModelProperty(value = "头像")
    private String avatar;

    /**
     * 登录账号
     */
    @ApiModelProperty(value = "昵称")
    private String nickname;

    /**
     * 白名单
     */
    @ApiModelProperty(value = "白名单")
    private String whiteList;

    /**
     * md5Key
     */
    @ApiModelProperty(value = "md5")
    private String md5Key;

    /**
     * 谷歌身份验证密钥
     */
    @ApiModelProperty(value = "谷歌身份验证")
    private String googlesecret;

    /**
     * 商家网址
     */
    @ApiModelProperty(value = "商家网站")
    private String website;

    /**
     * 登录次数
     */
    @ApiModelProperty(value = "登录次数")
    private String logins;


    /**
     * 下发usdt地址
     */
    @ApiModelProperty(value = "下发usdt地址")
    private String usdtAddress;

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;

    /**
     * 登录IP
     */
    @ApiModelProperty(value = "loginIp")
    private String loginIp;

    /**
     * 总代收统计
     */

    @ApiModelProperty(value = "总代收统计")
    private String allCollection;

    /**
     * 总付统计
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "总付统计")
    private String allTransfer;

    /**
     * 总下发次数
     */
    @ApiModelProperty(value = "总下发次数")
    private String transferCount;

    /**
     * 总下发金额
     */
    @ApiModelProperty(value = "总下发金额")
    private String transferAmount;


    private String beginTime;






}