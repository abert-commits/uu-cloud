package org.uu.common.core.utils;

/**
 * 获取客户端设备类型工具类
 *
 * @author Simon
 * @date 2023/11/29
 */
public class UserAgentUtil {

    private static final String ANDROID_PATTERN = ".*Android.*";
    private static final String IPHONE_PATTERN = ".*iPhone.*";
    private static final String IPAD_PATTERN = ".*iPad.*";
    private static final String POSTMAN_PATTERN = ".*PostmanRuntime.*";
    private static final String APIFOX_PATTERN = ".*Apifox.*";
    private static final String PC_PATTERN = ".*(Windows NT|Macintosh|Linux).*";
    private static final String CURL_PATTERN = ".*Curl.*";
    private static final String INSOMNIA_PATTERN = ".*Insomnia.*";
    private static final String SWAGGER_UI_PATTERN = ".*SwaggerUI.*";
    private static final String SOAP_UI_PATTERN = ".*SoapUI.*";
    private static final String ARC_PATTERN = ".*ARC.*";
    private static final String HTTPIE_PATTERN = ".*HTTPie.*";
    private static final String PAW_PATTERN = ".*Paw.*";
    private static final String FIDDLER_PATTERN = ".*Fiddler.*";
    private static final String THUNDER_CLIENT_PATTERN = ".*ThunderClient.*";
    private static final String JMETER_PATTERN = ".*JMeter.*";


    public enum DeviceType {
        ANDROID_PHONE("1", "Android"),
        IPHONE("2", "iPhone"),
        IPAD("3", "iPad"),
        PC("4", "PC"),
        POSTMAN("5", "Postman"),
        APIFOX("6", "Apifox"),
        CURL("7", "Curl"),
        INSOMNIA("8", "Insomnia"),
        SWAGGER_UI("9", "SwaggerUI"),
        SOAP_UI("10", "SoapUI"),
        ARC("11", "ARC"),
        HTTPIE("12", "HTTPie"),
        PAW("13", "Paw"),
        FIDDLER("14", "Fiddler"),
        THUNDER_CLIENT("15", "ThunderClient"),
        JMETER("16", "JMeter"),
        UNKNOWN("17", "未知");

        private final String code;

        private final String name;

        DeviceType(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static String getNameByCode(String code) {
            for (DeviceType c : DeviceType.values()) {
                if (c.getCode().equals(code)) {
                    return c.getName();
                }
            }
            return "";
        }
    }

    public static String getDeviceType(String userAgent) {
        if (userAgent == null) {
            return DeviceType.UNKNOWN.getName();
        }

        if (userAgent.matches(ANDROID_PATTERN)) {
            return DeviceType.ANDROID_PHONE.getName();
        } else if (userAgent.matches(IPHONE_PATTERN)) {
            return DeviceType.IPHONE.getName();
        } else if (userAgent.matches(IPAD_PATTERN)) {
            return DeviceType.IPAD.getName();
        } else if (userAgent.matches(POSTMAN_PATTERN)) {
            return DeviceType.POSTMAN.getName();
        } else if (userAgent.matches(APIFOX_PATTERN)) {
            return DeviceType.APIFOX.getName();
        } else if (userAgent.matches(PC_PATTERN)) {
            return DeviceType.PC.getName();
        } else if (userAgent.matches(CURL_PATTERN)) {
            return DeviceType.CURL.getName();
        } else if (userAgent.matches(INSOMNIA_PATTERN)) {
            return DeviceType.INSOMNIA.getName();
        } else if (userAgent.matches(SWAGGER_UI_PATTERN)) {
            return DeviceType.SWAGGER_UI.getName();
        } else if (userAgent.matches(SOAP_UI_PATTERN)) {
            return DeviceType.SOAP_UI.getName();
        } else if (userAgent.matches(ARC_PATTERN)) {
            return DeviceType.ARC.getName();
        } else if (userAgent.matches(HTTPIE_PATTERN)) {
            return DeviceType.HTTPIE.getName();
        } else if (userAgent.matches(PAW_PATTERN)) {
            return DeviceType.PAW.getName();
        } else if (userAgent.matches(FIDDLER_PATTERN)) {
            return DeviceType.FIDDLER.getName();
        } else if (userAgent.matches(THUNDER_CLIENT_PATTERN)) {
            return DeviceType.THUNDER_CLIENT.getName();
        } else if (userAgent.matches(JMETER_PATTERN)) {
            return DeviceType.JMETER.getName();
        } else {
            return DeviceType.UNKNOWN.getName();
        }
    }
}

