package org.uu.common.core.result;

import java.io.Serializable;

public class ApiResponse<T> implements Serializable {
    private String status;
    private String errorCode;
    private String msg;
    private T data;

    // 构造方法
    private ApiResponse(String status, String errorCode, String msg, T data) {
        this.status = status;
        this.errorCode = errorCode;
        this.msg = msg;
        this.data = data;
    }

    // 静态工厂方法
    public static <T> ApiResponse<T> of(ApiResponseEnum responseEnum, T data) {
        return new ApiResponse<>(responseEnum.getStatus(), responseEnum.getErrorCode(), responseEnum.getMessage(), data);
    }

    // 静态工厂方法
    public static <T> ApiResponse<T> ofMsg(ApiResponseEnum responseEnum, String msg ,T data) {
        return new ApiResponse<>(responseEnum.getStatus(), responseEnum.getErrorCode(), msg, data);
    }

    // Getter 和 Setter 方法
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
