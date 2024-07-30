package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

/**
 * @author admin
 * @date 2024/4/9 14:48
 */
@Data
@ApiModel(description = "信用分记录列表请求参数")
public class CreditScoreLogsListPageReq extends PageRequest {
    @ApiModelProperty(value = "会员id")
    private Long memberId;

    @ApiModelProperty(value = "交易类型 1-买入 2-卖出")
    private Integer tradeType;

    @ApiModelProperty(value = "订单类型 1-支付超时 2-自动完成 3-提交申诉成功 4-提交申诉失败 5-被申诉成功 6-被申诉失败 7-确认超时48小时 8-确认到账")
    private Integer eventType;

    @ApiModelProperty(value = "分数变化")
    private Integer changeScore;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private String endTime;


    @ApiModelProperty(value = "变化类型 1-增加 2-减少")
    private Integer changeType;

}
