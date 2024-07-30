package org.uu.common.pay.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FieldOrderByTypeEnum {

    DESC(1, "降序"),

    ASC(2, "升序"),
    ;

    private final Integer orderByType;

    private final String desc;
}
