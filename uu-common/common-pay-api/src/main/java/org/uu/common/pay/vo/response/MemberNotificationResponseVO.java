package org.uu.common.pay.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("消息通知响应实体类")
public class MemberNotificationResponseVO implements Serializable {
    private static final long serialVersionUID = -4444239388325293215L;

    @ApiModelProperty("消息ID")
    private Long id;

    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("是否阅读 0未阅读 1已阅读")
    private Integer readFlag;

    @ApiModelProperty("订单类型 1-买入 2-卖出 3-USDT充值 0-其他")
    private Integer orderType;

    @ApiModelProperty("订单状态  3-待支付 7-已完成 8-已取消")
    private Integer orderStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
}
