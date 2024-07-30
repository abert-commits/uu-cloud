package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author admin
 * @date 2024/3/9 15:37
 */
@Data
@ApiModel(description = "订单状态统计")
public class OrderStatusOverviewDTO {

    @ApiModelProperty(value = "匹配超时")
    private Long matchOverTimeNum;

    @ApiModelProperty(value = "取消支付")
    private Long cancelPayNum;

    @ApiModelProperty(value = "取消支付")
    private Long payOverTimeNum;

    @ApiModelProperty(value = "确认超时")
    private Long confirmOverTimeNum;

    @ApiModelProperty(value = "订单申诉-成功")
    private Long orderAppealSuccessNum;

    @ApiModelProperty(value = "订单申诉-失败")
    private Long orderAppealFailedNum;

    @ApiModelProperty(value = "金额错误-已处理")
    private Long amountErrorNum;

    @ApiModelProperty(value = "申诉订单-合计")
    private Long appealTotalNum;

    @ApiModelProperty(value = "已取消")
    private Long cancelNum;

    @ApiModelProperty(value = "已完成")
    private Long finishNum;

    @ApiModelProperty(value = "成功率")
    private BigDecimal successRate;
}
