package org.uu.wallet.Enum;

/**
 * 客服系统类型枚举
 *
 * @author Simon
 * @date 2024/04/21
 */
public enum CustomerServiceEnum {
    LIVE_CHAT("1", "livechat"),
    TWAK("2", "twak");

    private final String code;
    private final String name;

    CustomerServiceEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取对应的客服系统类型名称
     *
     * @param code 客服系统类型的编码
     * @return 客服系统类型的名称，如果没有找到匹配的code则返回null
     */
    public static String getNameByCode(String code) {
        for (CustomerServiceEnum c : CustomerServiceEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }
        }
        return null;
    }
}
