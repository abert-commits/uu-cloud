package org.uu.common.core.websocket.receive.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * 接收客户端订阅WebSocket的类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MemberWebSocketReceiveMessage implements Serializable {
    private static final long serialVersionUID = -5465630177993450700L;

    /**
     * 客户端订阅消息类型 详情请见{@link org.uu.common.core.enums.MemberWebSocketMessageTypeEnum}
     */
    private String subscribeType;
}
