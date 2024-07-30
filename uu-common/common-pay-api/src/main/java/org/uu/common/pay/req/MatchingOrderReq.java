package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description ="撮合列表")
public class MatchingOrderReq extends PageRequest {


    @ApiModelProperty("撮合Id")
    private Long id;
    /**
     * 提现会员ID
     */
    @ApiModelProperty("提现会员ID")
    private String paymentMemberId;


    /**
     * 充值会员ID
     */
    @ApiModelProperty("充值会员ID")
    private String collectionMemberId;


    /**
     * 充值平台订单号
     */
    @ApiModelProperty("卖出订单号")
    private String collectionPlatformOrder;



    /**
     * 提现平台订单号
     */
    @ApiModelProperty("提现平台订单号")
    private String paymentPlatformOrder;






    /**
     * 状态
     */
    @ApiModelProperty("撮合订单状态 11 金额错误  3 待支付  4 确认中 5 确认超时 6申诉中  7 已完成 8已取消  12 未支付 15 手动完成 16 人工审核")
    private String status;





    @ApiModelProperty("撮合区间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;
    @ApiModelProperty("撮合区间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;
    @ApiModelProperty("支付开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTimeStart;
    @ApiModelProperty("支付结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTimeEnd;
    @ApiModelProperty("订单完成时间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTimeStart;
    @ApiModelProperty("订单完成时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTimeEnd;
    @ApiModelProperty("语言: zh-中文 en-英文")
    private String lang;

    @ApiModelProperty("风控标记: 1-操作超时 2-IP黑名单 3-正常")
    private String riskTag;

    /**
     * 是否通过KYC自动完成 1: 是
     */
    @ApiModelProperty(value = "是否通过KYC自动完成 0：否 1: 是")
    private Integer kycAutoCompletionStatus;


    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty(value = "支付方式")
    private String payType;
}