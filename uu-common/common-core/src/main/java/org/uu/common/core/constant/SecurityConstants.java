package org.uu.common.core.constant;

public interface SecurityConstants {


    String AUTHORIZATION_KEY = "Authorization";


    String JWT_PREFIX = "Bearer ";


    String BASIC_PREFIX = "Basic ";


    String JWT_PAYLOAD_KEY = "payload";



    String AUTHORITY_PREFIX = "ROLE_";


    String JWT_AUTHORITIES_KEY = "authorities";


    String CLIENT_ID_KEY = "client_id";



    String BLACKLIST_TOKEN_PREFIX = "AUTH:BLACKLIST_TOKEN:";


    String LOGIN_USER_ID = "AUTH:USER_ID";

    String LOGIN_USER_NAME = "AUTH:USER_NAME";

    String LOGIN_LAST_LOGIN_TIME = "lastLoginTime";

    String LOGIN_LAST_LOGIN_IP = "loginIp";

    /**
     * 登录次数
     */
    String LOGIN_COUNT = "LOGIN_COUNT";


    /**
     * JWT增强内容
     */
    String REDIS_ENHANCER_KEY_PREFIX = "auth:token:enhancer:";

    /**
     * 用户最后活动时间
     */
    String LAST_ACTIVITY_TIME_PREFIX = "user_activity:last_activity_time:";

    /**
     * token有效期 3600秒 (1个小时)
     */
    Integer TOKEN_EXPIRATION_TIME_IN_SECONDS = 3600;


    /**
     * refresh_token
     */
    String REFRESH_TOKEN_PREFIX = "AUTH:REFRESH_TOKEN:";

    /**
     * 退出标识
     */
    String LOGOUT_TOKEN_PREFIX = "logout";
}
