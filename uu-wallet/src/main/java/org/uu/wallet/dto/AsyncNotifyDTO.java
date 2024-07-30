package org.uu.wallet.dto;

import lombok.Data;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MerchantCollectOrders;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.PaymentOrder;

/**
 * @author lukas
 */
@Data
public class AsyncNotifyDTO {
    private boolean success = false;
    private boolean needMqAck = false;
    private MerchantCollectOrders merchantCollectOrders;
    private PaymentOrder paymentOrder;
    private String errorInfo;
    private String request;
    private String response;
    private String type;
    private String tradeNotifyUrl;
    /**
     * 1 充值 2 提现
     */
    private Integer changType = 1;
    private MerchantPaymentOrders merchantPaymentOrders;
    private CollectionOrder collectionOrder;
}
