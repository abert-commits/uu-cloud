package org.uu.common.core.result;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
public enum ResultCode implements Serializable {

    SUCCESS("1", "Success"),
    SYSTEM_EXECUTION_ERROR("999999", "System execution error"),
    USERNAME_OR_PASSWORD_ERROR("A00100", "Username or password error"),
    USER_NOT_EXIST("A00101", "User does not exist"),
    MERCHANT_NOT_EXIST("A00102", "Merchant does not exist"),
    MERCHANT_OUTSTANDING_BALANCE("A00103", "Merchant balance is insufficient"),

    TIME_RANG_ERROR("A00104", "The start time is greater than the end time"),

    MERCHANT_WRONG_ID("A00105", "Wrong id"),

    MERCHANT_PASSWORDS_INCONSISTENT("A00106", "Passwords are inconsistent"),

    MERCHANT_ORIGINAL_PASSWORDS_WRONG("A00107", "Wrong original password"),

    MEMBER_OUTSTANDING_BALANCE("A00108", "Member balance is insufficient"),

    MEMBER_NOT_EXIST("A00109", "Member does not exist"),

    BUCKET_NOT_EXIST("A00110", "aliyun oss bucket is not exist"),

    ORDER_ALREADY_CALLBACK("A00111", "Order has been recalled."),

    ORDER_NOT_EXIST("A00112", "Order number does not exist."),

    ORDER_STATUS_ERROR("A00113", "Abnormal status."),

    WITHDRAW_ORDER_NOT_EXIST("A00114", "Unable to find withdrawal order number"),

    RECHARGE_ORDER_NOT_EXIST("A00115", "Unable to find recharge order number"),

    USDT_EMPTY("A00116", "USDT address is empty"),

    AMOUNT_ERROR("A00117", "Amount abnormal"),

    AMOUNT_ERROR1("A00118", "Actual amount cannot be greater than order amount"),

    CLIENT_AUTHENTICATION_FAILED("A00212", "Client authentication failed"),
    ACCESS_UNAUTHORIZED("A00213", "Unauthorized access"),
    TOKEN_ACCESS_FORBIDDEN("A00215", "Token access forbidden"),
    FLOW_LIMITING("B0210", "System flow limiting"),
    DEGRADATION("B0220", "System feature degradation"),
    SERVICE_NO_AUTHORITY("B0221", "Service not authorized"),
    PARAM_IS_NOT_EMPTY("C00001", "Parameter cannot be empty"),

    RELOGIN("401", "Token illegal or invalid, please re-login"),

    PARAM_VALID_FAIL("000001", "Parameter validation failed"),
    DATA_NOT_FOUND("000002", "Data not found"),


    MOBILE_ALREADY_REGISTERED("1001", "Mobile number already registered"),
    EMAIL_ALREADY_REGISTERED("1002", "Email account already registered"),
    SEND_VERIFICATION_CODE_FAILED("1003", "Failed to send verification code"),
    VERIFICATION_CODE_ERROR("1004", "Verification code error"),
    ACCOUNT_INPUT_ERROR("1006", "Incorrect account input"),
    SEND_VERIFICATION_CODE_FREQUENTLY("1007", "Verification code sent too frequently"),
    BUY_COOLDOWN_PERIOD("1008", "Member in buy cooldown period"),
    NO_PERMISSION("1009", "No permission"),
    ACCOUNT_ALREADY_VERIFIED("1014", "Account already verified"),
    VERIFICATION_INFO_ALREADY_USED("1015", "Verification info already used"),
    FILE_UPLOAD_FAILED("1016", "File upload failed"),
    REAL_NAME_VERIFICATION_FAILED("1017", "Real name verification failed"),
    MOBILE_NUMBER_ALREADY_USED("1018", "Mobile number already used"),
    EMAIL_ALREADY_USED("1019", "Email account already used"),
    MEMBER_ACCOUNT_ALREADY_USED("1020", "Member account already used"),
    INVITATION_CODE_ERROR("1021", "Invitation code error"),
    REGISTRATION_FAILED("1022", "Registration failed"),
    ILLEGAL_OPERATION_COLLECTION_INFO_CHECK_FAILED("1023", "Illegal operation - collection info check failed"),
    COLLECTION_DAILY_LIMIT_REACHED("1024", "Collection info daily limit reached"),
    COLLECTION_DAILY_AMOUNT_LIMIT_REACHED("1025", "Collection info daily amount limit reached"),
    MATCHING_ORDER_IN_PROGRESS("1026", "Matching order in progress"),
    UNFINISHED_ORDER_EXISTS("1027", "Unfinished order exists"),
    ORDER_NUMBER_ERROR("1028", "Order number error"),
    ORDER_ALREADY_USED_BY_OTHERS("1029", "Order already used by others"),
    ORDER_AMOUNT_ERROR("1030", "Order amount error"),
    ORDER_AMOUNT_MUST_BE_INTEGER("1031", "Order amount must be in hundreds"),
    MEMBER_STATUS_NOT_AVAILABLE("1032", "Member status not available"),
    ORDER_AMOUNT_EXCEEDS_LIMIT("1033", "Order amount exceeds limit"),
    MEMBER_BUY_STATUS_NOT_ENABLED("1034", "Member buy status not enabled"),
    MATCHING_FAILED("1036", "Matching failed"),
    USDT_RATE_ERROR("1037", "USDT rate error"),
    USDT_AMOUNT_TOO_LOW("1038", "USDT amount exceeds the limit"),
    ORDER_EXPIRED("1040", "Order expired"),
    FILE_VERIFICATION_FAILED("1041", "File verification failed"),
    FILE_CANNOT_BE_EMPTY("1042", "File cannot be empty"),
    FILE_SIZE_EXCEEDS_LIMIT("1043", "File size exceeds limit"),
    FILE_MUST_BE_IMAGE("1044", "File must be an image"),
    FILE_MUST_BE_VIDEO("1045", "File must be a video"),
    ORDER_VERIFICATION_FAILED("1046", "Illegal operation - order verification failed"),
    SELL_AMOUNT_TOO_LOW("1048", "Sell amount too low"),
    MINIMUM_LIMIT_TOO_LOW("1049", "Minimum limit too low"),
    COLLECTION_INFO_NOT_ENABLED("1050", "Collection info not enabled"),
    SELL_AMOUNT_MUST_BE_INTEGER("1052", "Sell amount must be in hundreds"),
    MINIMUM_LIMIT_MUST_BE_INTEGER("1053", "Minimum limit must be in hundreds"),
    MINIMUM_LIMIT_EXCEEDS_SELL_AMOUNT("1054", "Minimum limit exceeds sell amount"),
    CURRENT_STATUS_NOT_AVAILABLE("1055", "Current status not available"),
    EXCEEDS_MAXIMUM_SPLIT_ORDER_COUNT("1056", "Exceeds maximum split order count"),
    ORDER_AMOUNT_NOT_MEET_COLLECTION_LIMIT("1057", "Order amount does not meet collection limit"),
    MINIMUM_LIMIT_NOT_MEET_COLLECTION_LIMIT("1058", "Minimum limit does not meet collection limit"),
    COLLECTION_INFO_EXCEEDS_DAILY_MAX_COUNT_LIMIT("1059", "Collection info exceeds daily max count limit"),
    COLLECTION_INFO_EXCEEDS_DAILY_MAX_AMOUNT_LIMIT("1060", "Collection info exceeds daily max amount limit"),
    SELL_AMOUNT_EXCEEDS_LIMIT("1061", "Sell amount exceeds limit"),
    EXCEEDS_MAX_ORDER_COUNT_LIMIT("1062", "Exceeds max order count limit"),
    INSUFFICIENT_BALANCE("1063", "Insufficient balance"),
    ORDER_STATUS_VERIFICATION_FAILED("1064", "Order status verification failed"),
    DATA_DUPLICATE_SUBMISSION("1074", "Data duplicate submission"),
    MEMBER_NOT_VERIFIED("1075","Member not verified"),
    PASSWORD_VERIFICATION_FAILED("1076","Password verification failed"),
    UNFINISHED_USDT_ORDER_EXISTS("1077", "Unfinished USDT order exists"),
    INSUFFICIENT_BALANCE_2("1078", "Insufficient ARB balance, please check balance"),
    UTR_VALIDATION_FAILED("1079", "UTR duplicate"),
    DUPLICATE_UPI_ERROR("1080", "CollectionInfo duplicate"),
    INVALID_REQUEST("1081", "Merchant does not exist"),
    EXPIRED("1082", "Page expired"),
    TOO_FREQUENT("1083", "Frequent operation"),
    SELL_AMOUNT_MUST_BE_INTEGER_10("1084", "The sell amount must be in multiples of 10"),
    IP_BLACKLISTED("1085", "Account is disabled, please contact customer service"),
    UPLOAD_CREDENTIALS_GENERATION_FAILED("1086", "Failed to generate upload credentials"),
    SORT_ORDER_DUPLICATED("1087", "The provided sort order is already in use, please choose a different one"),
    FILE_UPLOAD_REQUIRED("1088", "Please upload the file"),
    UPI_RECEIPT_LIMIT_REACHED("1089", "Members have reached the maximum number of receipt in a single day."),

    SORT_ORDER_LIMIT("1090", "The sort order must greater than 0"),

    ORDER_AMOUNT_TOO_LOW("1091", "The order amount is too low."),

    SWITCH_IS_ACTIVATED("1092", "The switch is already present and activated."),
    TASK_IS_ACTIVATED("1093", "The type of task is already present and activated."),
    PARAM_IS_EMPTY_OR_ERROR("1094", "Parameter maybe empty or error"),
    ACTIVITY_NOT_STARTED("1095", "Activity not started"),
    CONTENT_NOT_FOUND("1096", "Content not found"),
    ELIGIBILITY_NOT_MET("1097", "Eligibility not met"),
    DAILY_REAL_NAME_AUTH_LIMIT_REACHED("1098", "Today's real name authentication submission limit has been reached"),
    MEMBER_BUY_STATUS_NOT_AVAILABLE("1099", "Member buy status not available"),
    MEMBER_SELL_STATUS_NOT_AVAILABLE("1100", "Member sell status not available"),
    BUY_FAILED_OVER_TIMES("1101", "Member buy failed over times"),
    NOT_VOUCHER_IMAGE("1102", "Not a voucher image"),
    UPDATE_CREDIT_SCORE_FAILED("1103", "Failed to change credit score."),
    PAY_OSR_FAIL_OVER_TIMES("1104", "Voucher image failed over times"),
    NOT_MATCHED_QUICK_BUY_ORDER("1105", "Match failed, please input another amount"),
    NOT_MORE_THAN_MIN_LIMIT("1106", "Amount must be more than min limit amount"),
    NOT_LESS_THAN_MAX_LIMIT("1107", "Amount must be less than max limit amount"),
    INVALID_NEW_USER_GUID_TYPE("1108", "Invalid type of new user guide"),
    LOW_CREDIT_SCORE("1109", "Low credit score"),
    UPDATE_CREDIT_CONFIG_FAILED("1110", "Update credit config failed"),
    KYC_BANK_NOT_FOUND("1111", "KYC bank not found"),
    KYC_CONNECTION_FAILED("1112", "KYC connection failed"),
    KYC_BANK_ALREADY_EXISTS_FAILED("1113", "KYC bank already exists"),
    INVITATION_CODE_INVALID("1114", "Invalid invitation code"),
    AMOUNT_EXCEEDS_ORDER_AMOUNT("1115", "Amount cannot be greater than order amount"),
    ACCOUNT_DISABLED("1116", "Account has been disabled!"),
    CASH_BACK_PROCESS_MISTAKE("1117", "cash back process mistake"),
    BANK_CODE_HAS_BEEN_EXISTS("1118", "bank code has been exists"),
    WRONG_FORMAT_BANK_CARD_CODE("1119", "Wrong format bank card code"),
    TOO_MANY_REQUESTS("1120", "Too many requests, please try again later"),
    DUPLICATE_BANK_ERROR("1121", "Bank Card duplicate"),
    DUPLICATE_IFSC_CODE_ERROR("1121", "Ifsc code duplicate"),
    KYC_API_REQUEST_FAILED("1122", "Kyc Api Request Failed"),
    KYC_LINK_FAILED("1123", "Kyc Link Partner Failed"),
    KYC_ACCOUNT_DUPLICATE("1124", "Kyc Account Duplicate"),
    DELEGATION_FAILED("1125", "Delegation failed"),
    DELEGATION_SUCCESS("1126", "Delegation success"),
    DELEGATION_STATUS("1127", "Delegation status update failed"),
    KYC_BANK_INFO_MISSING("1128", "Kyc Bank Info Missing"),
    KYC_NOT_CONNECTED("1129", "KYC not connected"),
    UTR_FORMAT_INCORRECT("1130", "UTR format is incorrect"),
    CURRENTLY_IN_DELEGATION("1131", "Currently in delegation"),
    CALL_BACK_FAILED("1133", "Callback failed"),
    OTP_INVALID("1134", "opt invalid"),
    USDT_ORDER_NOT_RECEIPT("1135", "USDT Order not Receipt"),
    TRADE_CONFIG_NOT_EXIST("1136", "Trade Config Is Not Exist"),
    ACTIVATE_STATUS_ERROR("1137", "activate status error"),
    ;



    public String getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }

    private String code;

    private String msg;

    @Override
    public String toString() {
        return "{" +
                "\"code\":\"" + code + '\"' +
                ", \"msg\":\"" + msg + '\"' +
                '}';
    }

}