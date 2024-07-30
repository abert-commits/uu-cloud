package org.uu.common.core.websocket.send.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.uu.common.core.enums.MemberWebSocketMessageTypeEnum;

/**
 * 用户WebSocket消息体
 * @param <T> 消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MemberWebSocketSendMessage<T> {
    /**
     * 消息类型
     * 严格参考{@link MemberWebSocketMessageTypeEnum}枚举类
     */
    private String messageType;

    /**
     * 用户ID
     */
    private String memberId;

    /**
     * 消息体
     */
    private T messageBody;

    /**
     * 构建用户WebSocket消息体
     * @param messageType 消息类型
     * @param messageBody 消息体
     */
    public static <T> MemberWebSocketSendMessage<T> buildMemberWebSocketMessage(
            String messageType,
            String memberId,
            T messageBody
    ) {
        return MemberWebSocketSendMessage.<T>builder()
                .messageType(messageType)
                .memberId(memberId)
                .messageBody(messageBody)
                .build();
    }
}
