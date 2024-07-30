package org.uu.wallet.Enum;

/**
 * 短信运营商枚举类
 *
 * @author Simon
 * @date 2024/03/06
 */
public enum SmsProviderEnum {
    SL("1", "sl", "颂量"),
    BK("2", "bk", "不卡"),
    SUBMAIL("3", "submail", "Submail");

    private final String code;
    private final String id;
    private final String name;

    SmsProviderEnum(String code, String id, String name) {
        this.code = code;
        this.id = id;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // 根据code查找枚举
    public static SmsProviderEnum fromCode(String code) {
        for (SmsProviderEnum provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

    // 根据id查找枚举
    public static SmsProviderEnum fromId(String id) {
        for (SmsProviderEnum provider : values()) {
            if (provider.getId().equalsIgnoreCase(id)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown id: " + id);
    }

    // 根据name查找枚举
    public static SmsProviderEnum fromName(String name) {
        for (SmsProviderEnum provider : values()) {
            if (provider.getName().equalsIgnoreCase(name)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown name: " + name);
    }
}

