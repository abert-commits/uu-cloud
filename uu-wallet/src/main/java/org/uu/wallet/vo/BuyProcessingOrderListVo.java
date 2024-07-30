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
@ApiModel(description = "买入订单列表")
public class BuyProcessingOrderListVo implements Serializable {

    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 10: 买入失败, 11: 金额错误, 13: 支付超时")
    private String orderStatus;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 订单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    /**
     * UTR
     */
    @ApiModelProperty(value = "UTR")
    private String utr;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;

    /**
     * 奖励
     */
    @ApiModelProperty(value = "奖励")
    private BigDecimal bonus;

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