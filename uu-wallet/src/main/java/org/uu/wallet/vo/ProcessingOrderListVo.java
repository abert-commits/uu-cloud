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
@ApiModel(description = "进行中的订单-列表")
public class ProcessingOrderListVo implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明：1、匹配中 2-匹配超时 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 7: 已完成, 8: 已取消, 9、订单失效 10: 买入失败, 11: 金额错误, 12-未支付 13: 支付超时 14、进行中 15、手动完成 16、人工审核")
    private String orderStatus;


    /**
     * 订单状态
     */
    @ApiModelProperty("订单类型 1-买入 2-卖出")
    private Integer orderType;



}