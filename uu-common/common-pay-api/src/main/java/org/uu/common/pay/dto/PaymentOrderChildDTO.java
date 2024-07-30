package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@ApiModel(description = "子订单返回")
public class PaymentOrderChildDTO implements Serializable {


    @ApiModelProperty("主键")
    @TableId(type = IdType.AUTO)
    private Long id;


//    /**
//     * 支付方式 默认值: UPI
//     */
//    @ApiModelProperty("UPI")
//    private String payType = "UPI";
//
//    /**
//     * 商户号
//     */
//    @ApiModelProperty("商户号")
//    private String merchantCode;
//
//    /**
//     * 商户订单号
//     */
//    @ApiModelProperty("商户订单号")
//    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty("平台订单号")
    private String platformOrder;

//    /**
//     * 匹配订单号
//     */
//    @ApiModelProperty("匹配订单号")
//    private String matchOrder;
//
//    /**
//     * UPI_ID
//     */
//    @ApiModelProperty("UPI_ID")
//    private String upiId;
//
//    /**
//     * UPI_Name
//     */
//    @ApiModelProperty("UPI_Name")
//    private String upiName;
//
//    /**
//     * 会员ID
//     */
//    @ApiModelProperty("会员ID")
//    private String memberId;
//
//    /**
//     * 会员账号
//     */
//    private String memberAccount;
//
//    /**
//     * 真实姓名
//     */
//    @ApiModelProperty("真实姓名")
//    private String realName;
//
//    /**
//     * 手机号
//     */
//    @ApiModelProperty("手机号")
//    private String mobileNumber;
//
//    /**
//     * 订单费率
//     */
//    @ApiModelProperty("订单费率")
//    private BigDecimal orderRate;
//
//    /**
//     * 订单金额
//     */
//    @ApiModelProperty("订单金额")
//    private BigDecimal amount;
//
//    /**
//     * 订单状态 默认状态: 待匹配
//     */
//    @ApiModelProperty("待匹配")
//    private String orderStatus;
//
//    /**
//     * 匹配回调地址
//     */
//    @ApiModelProperty("匹配回调地址")
//    private String matchNotifyUrl;
//
//    /**
//     * 匹配回调状态 默认状态: 未回调
//     */
//    @ApiModelProperty("未回调")
//    private String matchCallbackStatus;
//
//    /**
//     * 匹配回调时间
//     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime matchCallbackTime;
//
//    /**
//     * 交易回调地址
//     */
//    private String tradeNotifyUrl;
//
//    /**
//     * 交易回调状态 默认状态: 未回调
//     */
//    private String tradeCallbackStatus;
//
//    /**
//     * 交易回调时间
//     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime tradeCallbackTime;
//
//    /**
//     * 费用
//     */
//    private BigDecimal cost;
//
//    /**
//     * 商户名称
//     */
//    private String merchantName;
//
//    /**
//     * 时间戳
//     */
//    private String timestamp;
//
//    /**
//     * 客户端ip
//     */
//    private String clientIp;
//
//    /**
//     * 奖励
//     */
//    private String bonus;
//
//    /**
//     * 匹配时长
//     */
//    private String matchDuration;
//
//    /**
//     * 完成时长
//     */
//    private String completeDuration;
//
//    /**
//     * UTR
//     */
//    private String utr;
//
//    /**
//     * 凭证
//     */
//    private String voucher;



}