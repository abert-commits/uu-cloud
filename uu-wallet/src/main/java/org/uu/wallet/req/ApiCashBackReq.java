package org.uu.wallet.req;

import lombok.Data;

/**
 * @author admin
 * @date 2024/5/10 16:54
 */
@Data
public class ApiCashBackReq {
    private String merchantOrder;
    private String amount;
    private String userId;
    private String merchantCode;
}
