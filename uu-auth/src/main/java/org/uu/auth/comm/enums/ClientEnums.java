package org.uu.auth.comm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;


@Getter
@AllArgsConstructor
public enum ClientEnums {
    MERCHANT_CLIENT("merchant", "商户客户端"),
    WALLET_CLIENT("wallet", "钱包客户端"),
    ADMIN_CLIENT("uu", "后台客户端"),
    APP_CLIENT("app", "app客户端"),
    MEMBER_CLIENT("member", "会员客户端"),
    UU("uu", "69pay客户端");

    private final String name;

    private final String desc;

    /**
     * 根据客户端名称构建ClientEnums
     * @param clientName 客户端名称
     * @return ClientEnums
     */
    public static ClientEnums buildClientEnumByClientName(String clientName) {
        if (StringUtils.isNotEmpty(clientName) && StringUtils.isNotEmpty(clientName.trim())) {
            for (ClientEnums clientEnums : ClientEnums.values()) {
                if (clientEnums.getName().equals(clientName)) {
                    return clientEnums;
                }
            }
        }
        return null;
    }
}
