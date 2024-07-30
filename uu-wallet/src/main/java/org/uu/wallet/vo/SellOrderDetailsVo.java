package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "卖出订单详情")
public class SellOrderDetailsVo implements Serializable {

//    /**
//     * 订单状态
//     */
//    @ApiModelProperty("订单状态，取值说明： 1:匹配中, 2: 匹配超时, 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 9: 订单失效, 11: 金额错误, 13: 支付超时, 14: 进行中, 15: 已完成")
//    private String orderStatus;

    //TODO 状态设置和改变逻辑待完善
    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态,取值说明： 1: 等待,2: 支付中,3: 完成,4: 关闭")
    private String orderStatus;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * UTR
     */
    @ApiModelProperty(value = "UTR")
    private String utr;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "支付时间")
    private LocalDateTime paymentTime;

    /**
     * 支付凭证
     */
    @ApiModelProperty(value = "支付凭证")
    private String voucher;

    /**
     * 金额错误图片
     */
    @ApiModelProperty(value = "金额错误图片 多张图片以 ,逗号分割")
    private String amountErrorImage;

    /**
     * 金额错误视频
     */
    @ApiModelProperty(value = "金额错误视频")
    private String amountErrorVideo;

    /**
     * 实际金额
     */
    @ApiModelProperty("实际金额")
    private BigDecimal actualAmount;

    /**
     * 金额错误提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("金额错误提交时间")
    private LocalDateTime amountErrorSubmitTime;

    /**
     * 奖励
     */
    @ApiModelProperty("奖励")
    private BigDecimal bonus;


    /**
     * UPI_ID
     */
    @ApiModelProperty("UPI_ID")
    private String upiId;

    /**
     * UPI_NAME
     */
    @ApiModelProperty("UPI_NAME")
    private String upiName;


    /**
     * 匹配剩余时间
     */
    @ApiModelProperty(value = "匹配剩余时间  单位: 秒  如果值为null或负数 表示该笔订单已过期")
    private Long matchExpireTime;

    /**
     * 确认中剩余时间
     */
    @ApiModelProperty(value = "确认中剩余时间  单位: 秒  如果值为null或负数 表示该笔订单已过期")
    private Long confirmExpireTime;

    /**
     * 待支付剩余时间
     */
    @ApiModelProperty(value = "待支付剩余时间  单位: 秒  如果值为null或负数 表示该笔订单已过期")
    private Long paymentExpireTime;


    /**
     * 最小限额
     */
    @ApiModelProperty(value = "最小限额")
    private BigDecimal minimumAmount;


    /**
     * 已卖出数量
     */
    @ApiModelProperty("已卖出数量")
    private BigDecimal soldAmount;

    /**
     * 剩余金额
     */
    @ApiModelProperty("剩余数量")
    private BigDecimal remainingAmount;

    /**
     * 卖出子订单列表
     */
    @ApiModelProperty("卖出子订单列表")
    List<SellOrderListVo> sellOrderList;

    /**
     * 取消原因
     */
    @ApiModelProperty(value = "取消原因")
    private String cancellationReason;

    /**
     * 是否申诉 默认值 0
     */
    @ApiModelProperty(value = "是否经过申诉, 取值说明: 0: 未申诉, 1: 已申诉")
    private Integer isAppealed = 0;

    /**
     * 申诉类型 1: 未到账  2: 金额错误
     */
    @ApiModelProperty(value = "申诉类型, 取值说明: 1: 未到账, 2: 金额错误")
    private Integer displayAppealType = null;

    /**
     * 预计匹配时间
     */
    @ApiModelProperty(value = "预计匹配时间 单位: 分钟")
    private Integer estimatedMatchTime;

    /**
     * 是否是子订单
     */
    @ApiModelProperty(value = "是否是子订单, 取值说明: 0: 不是子订单, 1: 是子订单")
    private Integer isSubOrder = 0;

    /**
     * 失败原因
     */
    @ApiModelProperty(value = "失败原因")
    private String remark;

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
     * 支付方式
     */
    @ApiModelProperty("支付方式")
    private String payType;

    /**
     * 申诉原因
     */
    @ApiModelProperty(value = "申诉原因")
    private String reason;

    /**
     * 是否可取消卖出订单
     */
    @ApiModelProperty("是否可取消卖出订单,取值说明: true: 可取消, false: 不可取消")
    private Boolean cancellable;

    @ApiModelProperty("会员类型 1-内部 2-外部")
    private Integer memberType;

    @ApiModelProperty(value = "KYC账户")
    private String kycAccount;

    @ApiModelProperty(value = "KYC银行名称")
    private String kycBankName;
}