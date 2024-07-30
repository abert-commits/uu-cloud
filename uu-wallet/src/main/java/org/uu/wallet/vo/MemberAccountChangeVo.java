package org.uu.wallet.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author pom
 * 当前会员用户帐变记录数据VO类
 */
@Data
@ApiModel(description = "当前会员用户帐变记录数据")
public class MemberAccountChangeVo implements Serializable {

    /**
     * 买入金额、卖出金额、买入奖励、卖出奖励 收款账号 付款账号
     * 类型 订单号 UPI名称 'UPI ID' UTR 备注 订单时间
     */

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currentcy;

    /**
     * 账变类型: add-增加, sub-支出
     */
    @ApiModelProperty(value = "账变类型: add-增加, sub-支出")
    private String changeMode;

    /**
     * 账变类型: 1-买入, 2-卖出, 3-usdt充值,4-人工上分,5-人工下分
     */
    @ApiModelProperty(value = "账变类型:1-买入, 2-卖出, 3-usdt充值,...")
    private String changeType;

    /**
     * 平台订单号
     */
    @ApiModelProperty(value = "平台订单号")
    private String orderNo;

    /**
     * 变化金额
     */
    @ApiModelProperty(value = "变化金额")
    private BigDecimal amountChange;

    /**
     * 备注: 账变状态文案内容
     */
    @ApiModelProperty(value = "备注: 账变状态文案内容")
    private String remark;

    /**
     * 账变时间
     */
    @ApiModelProperty(value = "账变时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * upiId
     */
    @ApiModelProperty(value = "upiId")
    private String upiId;

    /**
     * upiName
     */
    @ApiModelProperty(value = "upiName")
    private String upiName;

    /**
     * UTR
     */
    @ApiModelProperty(value = "UTR")
    private String utr;

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

    /**
     * USDT地址
     */
    @ApiModelProperty(value = "USDT地址")
    private String usdtAddr;

    /**
     * USDT实际数量
     */
    @ApiModelProperty(value = "USDT实际数量")
    private BigDecimal usdtActualNum;

}

