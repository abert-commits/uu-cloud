package org.uu.wallet.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum NotificationReadFlagEnum {
    READ_N(0, "未读"),

    READ_Y(1, "已读");

    private final Integer readFlag;

    private final String desc;

    public static NotificationReadFlagEnum buildNotificationReadFlagEnumByReadFlag(Integer readFlag) {
        if (Objects.nonNull(readFlag)) {
            for (NotificationReadFlagEnum readFlagEnum : NotificationReadFlagEnum.values()) {
                if (readFlagEnum.getReadFlag().equals(readFlag)) {
                    return readFlagEnum;
                }
            }
        }
        return null;
    }
}
