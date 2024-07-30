package org.uu.wallet.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberNotificationTypeEnum {
    TRADE_NOTIFICATION(1, "交易通知"),

    OTHER_NOTIFICATION(2, "其他通知");

    private final Integer notificationType;

    private final String desc;

}
