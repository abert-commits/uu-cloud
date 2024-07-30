package org.uu.wallet.Enum;

public enum ContentTypeEnum{


    JSON("aplication/json"),
    FORM("application/x-www-form-urlencoded");



    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }



    private ContentTypeEnum(String content){
        this.content = content;
    }

}
