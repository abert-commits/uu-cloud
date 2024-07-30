package org.uu.common.core.result;

public class DisabledResponse {
    private String code;
    private Object data;
    private String msg;

    // 构造方法私有化，不允许外部直接创建实例
    private DisabledResponse(String code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    // 提供一个公共的静态方法来获取实例
    public static DisabledResponse create() {
        return new DisabledResponse("1115", null, "Account is disabled！");
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

