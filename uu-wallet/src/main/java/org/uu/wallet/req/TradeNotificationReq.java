package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.wallet.Enum.MemberNotificationTypeEnum;
import org.uu.wallet.Enum.OrderStatusEnum;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 交易通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("新增交易通知实体类")
public class TradeNotificationReq implements Serializable {
    private static final long serialVersionUID = -1632545617200629500L;

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "会员ID", required = true)
    @NotNull(message = "Please specify the id of current member")
    private Long memberId;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号", required = true)
    @NotNull(message = "Please specify the number of current order")
    private String orderNo;

    /**
     * 订单类型 与{@link MemberAccountChangeEnum}账变类型保持一致
     */
    @ApiModelProperty(value = "订单类型 与{@link MemberAccountChangeEnum}账变类型保持一致", required = true)
    @NotNull(message = "Please specify the type of current order")
    private MemberAccountChangeEnum orderType;

    /**
     * 订单状态 与{@link OrderStatusEnum}订单状态保持一致
     */
    @ApiModelProperty(value = "订单状态 与{@link OrderStatusEnum}订单状态保持一致", required = true)
    @NotNull(message = "Please specify the status of current order ")
    private OrderStatusEnum orderStatusEnum;


    /**
     * 通知类型 默认交易通知
     */
    @ApiModelProperty(value = "通知类型 默认交易通知")
    private MemberNotificationTypeEnum notificationType = MemberNotificationTypeEnum.TRADE_NOTIFICATION;
}
