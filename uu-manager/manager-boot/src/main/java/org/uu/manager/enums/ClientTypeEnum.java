package org.uu.manager.enums;

import java.util.Objects;

/**
 * @author admin
 * @date 2024/3/5 17:32
 * 客户端类型枚举类
 */
public enum ClientTypeEnum {
    /**
     * 内部商户访问
     */
    AR("1", "uu", "总后台"),
    /**
     * 商户访问
     */
    MERCHANT("2", "merchant", "商户后台");

    private final String type;
    private final String clientCode;
    private final String clientName;


    public String getType() {
        return type;
    }

    public String getClientCode() {
        return clientCode;
    }

    public String getClientName() {
        return clientName;
    }

    ClientTypeEnum(String type, String clientCode, String clientName) {
        this.type = type;
        this.clientCode = clientCode;
        this.clientName = clientName;
    }

    public static String getTypeByClientCode(String clientCode) {
        if(Objects.isNull(clientCode)){
            return null;
        }
        for (ClientTypeEnum value : ClientTypeEnum.values()) {
            if (clientCode.equals(value.getClientCode())) {
                return value.type;
            }
        }
        return null;
    }
}
