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
public class SellProcessingOrderListVo implements Serializable {

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 1:匹配中, 2: 匹配超时, 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 9: 订单失效, 11: 金额错误, 13: 支付超时, 14: 进行中, 已完成")
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
     * 倒计时
     */
    @ApiModelProperty(value = "倒计时")
    private Long countdown;

    /**
     * 倒计时
     */
    @ApiModelProperty(value = "倒计时限")
    private Long countdownLimit;

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
}