package org.uu.wallet.Enum;

public enum HttpMothodEnum {
    POST("post"),
    GET("get");

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private String code;



    HttpMothodEnum(String code){
        this.code = code;
    }

}
