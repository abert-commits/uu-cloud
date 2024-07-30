package org.uu.wallet.webSocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.websocket.send.member.MemberWebSocketSendMessage;
import org.uu.common.core.enums.MemberWebSocketMessageTypeEnum;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * 订单交易状态改变通知 推送消息给前端
 *
 * @author Simon
 * @date 2023/11/08
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MemberMessageSender {

    private final MemberMessageWebSocketService notifyOrderStatusChangeWebSocketService;

    /**
     * 订单交易状态改变通知 推送消息给前端
     */
    @SuppressWarnings("all")
    public <T> void send(MemberWebSocketSendMessage<T> memberWebSocketMessage) {
        try {
            if (Objects.isNull(memberWebSocketMessage)) {
                log.error("[会员通知] WebSocket推送前端失败 消息体为空");
                return;
            }
            String messageToJson = JSON.toJSONString(
                    memberWebSocketMessage,
                    SerializerFeature.WriteMapNullValue
            );
            MemberWebSocketMessageTypeEnum memberWebSocketMessageTypeEnum
                    = MemberWebSocketMessageTypeEnum.buildMemberWebSocketMessageTypeEnumByMessageType(memberWebSocketMessage.getMessageType());
            if (Objects.isNull(memberWebSocketMessageTypeEnum)) {
                log.error("[会员通知] WebSocket推送前端失败 未知消息体类型");
                return;
            }
            boolean sendResult = notifyOrderStatusChangeWebSocketService.memberMessageSendToWebSocket(messageToJson);
            if (! sendResult) {
                log.error("[会员通知 - {}] WebSocket推送前端失败 内容: {}", memberWebSocketMessageTypeEnum.getDesc(), messageToJson);
            }
        } catch (Exception e) {
            log.error("[订单交易状态改变通知]webSocket推送前端失败, e: {}", e.getMessage());
        }
    }
}
