package org.uu.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 会员WebSocket消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum MemberWebSocketMessageTypeEnum {
    KEEP_LIVE("0", "心跳检测"),

    BUY_INR("1", "INR买入"),

    BUY_USDT("2", "USDT买入"),

    SELL_INR("3", "INR卖出"),
    ;

    /**
     * 消息类型
     */
    private final String  messageType;

    /**
     * 消息类型描述
     */
    private final String desc;

    /**
     * 通过消息类型构建会员WebSocket消息类型枚举类
     * @param messageType 消息类型
     */
    public static MemberWebSocketMessageTypeEnum buildMemberWebSocketMessageTypeEnumByMessageType(String messageType) {
        if (Objects.nonNull(messageType) && StringUtils.isNotEmpty(messageType) && StringUtils.isNotEmpty(messageType.trim())) {
            for (MemberWebSocketMessageTypeEnum memberWebSocketMessageTypeEnum : MemberWebSocketMessageTypeEnum.values()) {
                if (memberWebSocketMessageTypeEnum.getMessageType().equals(messageType)) {
                    return memberWebSocketMessageTypeEnum;
                }
            }
        }
        return null;
    }
}
