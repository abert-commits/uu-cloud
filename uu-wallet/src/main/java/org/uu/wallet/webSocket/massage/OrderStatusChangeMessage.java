package org.uu.wallet.webSocket.massage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * 订单状态改变消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class OrderStatusChangeMessage implements Serializable {
    private static final long serialVersionUID = 5832828309415283363L;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态
     */
    private String orderStatus;
}
