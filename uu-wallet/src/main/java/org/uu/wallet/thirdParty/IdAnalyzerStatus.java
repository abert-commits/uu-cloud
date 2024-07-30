package org.uu.wallet.thirdParty;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdAnalyzerStatus {

    /**
     * 短信返送 结果 success:成功 false:失败
     */
    private Boolean status;

    /**
     * 返回错误信息
     */
    private String msg;

    /**
     * 返回错误编码
     */
    private String code;


    /**
     * 1: 来自于error
     * 2: 来自于verification
     */
    private String type;

    public IdAnalyzerStatus(Boolean status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public IdAnalyzerStatus(String type, JSONObject jsonObject) {
        if("1".equals(type)){
            this.status = false;
            this.code = jsonObject.getString("code");
            this.msg = jsonObject.getString("message");
            this.type= "1";
        }else if("2".equals(type)){
            if(jsonObject.getBoolean("passed")){
                this.status = true;
            }else{
                this.status = false;
                JSONObject resultJsonObject = jsonObject.getJSONObject("result");
                this.msg = resultJsonObject.keySet().stream().map(k->{
                    if(resultJsonObject.getBoolean(k))return "" ;
                    return k+":"+resultJsonObject.getString(k);
                }).collect(Collectors.joining(","));
            }
        }
    }
}
