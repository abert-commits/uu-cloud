package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配池
 *
 * @author
 */
@Data
@ApiModel(description = "匹配池")
public class MatchPoolDTO implements Serializable {

    @ApiModelProperty("主键")
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
    @ApiModelProperty("UPI_Name")
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
    @ApiModelProperty("订单状态")
    private String orderStatus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @ApiModelProperty("时间戳")
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
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    /**
     * 交易回调地址
     */
    @ApiModelProperty("tradeNotifyUrl")
    private String tradeNotifyUrl;

    /**
     * 交易回调状态
     */
    @ApiModelProperty("交易回调状态")
    private String tradeCallbackStatus;

    /**
     * 交易回调时间
     */
    @ApiModelProperty("交易回调时间")
    private LocalDateTime tradeCallbackTime;

    /**
     * UTR
     */
    @ApiModelProperty("utr")
    private String utr;

    /**
     * 凭证
     */
    @ApiModelProperty("凭证")
    private String voucher;


}