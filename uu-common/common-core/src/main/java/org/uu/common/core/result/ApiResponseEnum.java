package org.uu.common.core.result;

public enum ApiResponseEnum {
    SUCCESS("SUCCESS", "", "Operation successful"),
    PARAM_VALID_FAIL("FAIL", "1001", "Parameter validation failed"),
    SIGNATURE_ERROR("FAIL", "1002", "Signature error"),
    INVALID_REQUEST("FAIL", "1003", "Merchant does not exist"),
    INVALID_IP("FAIL", "1004", "Illegal IP"),
    DECRYPTION_ERROR("FAIL", "1005", "Invalid ciphertext"),
    INVALID_MERCHANT_PUBLIC_KEY("FAIL", "1006", "Invalid merchant public key"),
    DATA_DUPLICATE_SUBMISSION("FAIL", "1010", "Data duplicate submission"),
    ORDER_NOT_FOUND("FAIL", "1011", "Order not found"),
    INSUFFICIENT_MERCHANT_BALANCE("FAIL", "1012", "Insufficient merchant balance"),
    MERCHANT_COLLECTION_STATUS_DISABLED("FAIL", "1013", "Merchant collection status not enabled"),
    MERCHANT_PAYMENT_STATUS_DISABLED("FAIL", "1014", "Merchant payment status not enabled"),
    AMOUNT_EXCEEDS_LIMIT("FAIL", "1015", "Amount exceeds limit"),
    SYSTEM_EXECUTION_ERROR("FAIL", "9999", "System error"),
    ORDER_MATCHING_FAILED("FAIL", "1023", "Order matching failed"),
    TOO_FREQUENT("FAIL", "1024", "Frequent operation"),
    UNSUPPORTED_PAY_TYPE("FAIL", "1025", "Unsupported pay type"),
    INVALID_USDT_ADDRESS("FAIL", "1026", "Invalid USDT address");

    private final String status;
    private final String errorCode;
    private final String message;

    ApiResponseEnum(String status, String errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
