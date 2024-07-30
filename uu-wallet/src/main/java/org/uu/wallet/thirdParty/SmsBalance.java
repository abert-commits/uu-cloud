package org.uu.wallet.thirdParty;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SmsBalance implements Serializable {

    /**
     * 状态码，0成功
     */
    private String status;

    /**
     * 失败原因说明
     */
    private String reason;

    /**
     * 实际账户的余额
     */
    private String balance;

    /**
     * 赠送账户余额
     */
    private String gift;

    /**
     * 信用额度
     */
    private String credit;


}
