package org.uu.wallet.vo;

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
@ApiModel(description = "卖出订单列表")
public class SellOrderListVo implements Serializable {

//    /**
//     * 订单状态
//     */
//    @ApiModelProperty("订单状态，取值说明： 1:匹配中, 2: 匹配超时, 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 9: 订单失效, 11: 金额错误, 13: 支付超时, 14: 进行中, 已完成")
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
     * 是否申诉 默认值 0
     */
    @ApiModelProperty(value = "是否经过申诉, 取值说明: 0: 未申诉, 1: 已申诉")
    private Integer isAppealed = 0;

    /**
     * 是否拆单 默认值 0
     */
    @ApiModelProperty(value = "是否拆单, 取值说明: 0: 未拆单, 1: 已拆单")
    private Integer isSplitOrder = 0;


    /**
     * 是否是母订单 默认值 0
     */
    @ApiModelProperty(value = "是否是母订单, 取值说明: 0: 非母订单, 1: 是母订单")
    private Integer isParentOrder = 0;

    /**
     * 最小限额
     */
    @ApiModelProperty(value = "最小限额")
    private BigDecimal minimumAmount;

    /**
     * 匹配订单号
     */
    @ApiModelProperty(value = "匹配订单号")
    private String matchOrder;


    /**
     * 实际金额
     */
    @ApiModelProperty("实际金额")
    private BigDecimal actualAmount;

    /**
     * 人工审核截至时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditDelayTime;


    /**
     * 申诉类型
     */
    @ApiModelProperty(value = "申诉类型 1: 未到账  2: 金额错误")
    private Integer displayAppealType;

    /**
     * 银行卡号
     */
    @ApiModelProperty(value = "银行卡号")
    private String bankCardNumber;


    /**
     * 收款类型 1: 银行卡, 3: UPI
     */
    @ApiModelProperty(value = "支付类型, 1: 银行卡, 3: UPI, 5:UPI和银行卡")
    private String payType;
}