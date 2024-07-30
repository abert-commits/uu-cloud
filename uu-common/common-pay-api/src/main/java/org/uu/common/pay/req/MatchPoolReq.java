package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配池
 *
 * @author
 */
@Data
@ApiModel(description ="匹配池参数说明")
public class MatchPoolReq extends PageRequest {


    private Long id;

    /**
     * 匹配订单号
     */
    @ApiModelProperty("匹配订单号")
    private String matchOrder;

    /**
     * UPI_ID
     */
    @ApiModelProperty("UPI_ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty("upiName")
    private String upiName;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal amount;

    /**
     * 最小限额
     */
    @ApiModelProperty("最小限额")
    private BigDecimal minimumAmount;

    /**
     * 已匹配订单数
     */
    @ApiModelProperty("已匹配订单数")
    private Integer orderMatchCount;

    /**
     * 进行中订单数
     */
    @ApiModelProperty("进行中订单数")
    private Integer inProgressOrderCount;

    /**
     * 已完成订单数
     */
    @ApiModelProperty("已完成订单数")
    private Integer completedOrderCount;

    /**
     * 已卖出金额
     */
    @ApiModelProperty("已卖出金额")
    private BigDecimal soldAmount;

    /**
     * 剩余金额
     */
    @ApiModelProperty("剩余金额")
    private BigDecimal remainingAmount;

    /**
     * 订单状态
     */
    @ApiModelProperty(" 1: 匹配中,  2: 匹配超时,  7: 已完成,  8: 已取消,  14: 进行中")
    private String orderStatus;



    /**
     * 匹配回调地址
     */
    @ApiModelProperty("匹配回调地址")
    private String matchNotifyUrl;

    /**
     * 匹配回调状态
     */
    @ApiModelProperty("匹配回调状态")
    private Integer matchCallbackStatus;

    /**
     * 匹配回调时间
     */
    @ApiModelProperty("匹配回调时间")
    private LocalDateTime matchCallbackTime;

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
     * 时间戳
     */
    private String timestamp;

    /**
     * 请求IP
     */
    @ApiModelProperty("请求IP")
    private String clientIp;

    /**
     * 奖励
     */
    @ApiModelProperty("奖励")
    private Integer bonus;

    /**
     * 匹配时长
     */
    @ApiModelProperty("匹配时长")
    private String matchDuration;

    /**
     * 完成时长
     */
    @ApiModelProperty("完成时长")
    private String completeDuration;


    /**
     * 交易回调地址
     */
    @ApiModelProperty("交易回调地址")
    private String tradeNotifyUrl;

    /**
     * 交易回调状态
     */
    @ApiModelProperty("交易回调地址")
    private String tradeCallbackStatus;

    /**
     * 交易回调时间
     */
    @ApiModelProperty("交易回调时间")
    private LocalDateTime tradeCallbackTime;

    /**
     * UTR
     */
    @ApiModelProperty("UTR")
    private String utr;

    /**
     * 凭证
     */
    @ApiModelProperty("凭证")
    private String voucher;

    private String  startTime;

    private String endTime;


    /**
     *
     */
    private BigDecimal minimumAmountStart;

    private BigDecimal minimumAmountEnd;

    private BigDecimal amountStart;

    private BigDecimal amountEnd;


}