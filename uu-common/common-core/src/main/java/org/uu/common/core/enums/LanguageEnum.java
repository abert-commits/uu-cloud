package org.uu.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Objects;

@Getter
@AllArgsConstructor
public enum LanguageEnum {
    ENGLISH(1, "英语"),

    INDY(2, "印地语"),
    ;

    private final Integer langType;

    private final String desc;

    public static LanguageEnum buildLanguageEnumByLangType(Integer langType) {
        if (Objects.nonNull(langType)) {
            for (LanguageEnum languageEnum : LanguageEnum.values()) {
                if (languageEnum.getLangType().equals(langType)) {
                    return languageEnum;
                }
            }
        }
        return null;
    }
}
