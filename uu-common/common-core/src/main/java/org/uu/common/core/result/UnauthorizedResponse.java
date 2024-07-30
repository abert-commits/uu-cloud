package org.uu.common.core.result;

public class UnauthorizedResponse {
    private String code;
    private Object data;
    private String msg;

    // 构造方法私有化，不允许外部直接创建实例
    private UnauthorizedResponse(String code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    // 提供一个公共的静态方法来获取实例
    public static UnauthorizedResponse create() {
        return new UnauthorizedResponse("401", null, "Token illegal or invalid, please re-login");
    }

    // Getter方法
    public String getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }
}

