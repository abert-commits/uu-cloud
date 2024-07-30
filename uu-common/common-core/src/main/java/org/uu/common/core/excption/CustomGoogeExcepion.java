package org.uu.common.core.excption;

public class CustomGoogeExcepion extends RuntimeException{
    private String errorCode;

    public CustomGoogeExcepion(String code,String msg){
        super(msg);
        this.errorCode =code;
    }

    public CustomGoogeExcepion(String msg){
        super(msg);

    }
}
