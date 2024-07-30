package org.uu.wallet.thirdParty;


import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageStatus {

    /**
     * 短信返送 结果 true:成功 false:失败
     */
    private Boolean status;

    /**
     * 当状态为发送失败的时候 才有值,为失败原因
     */
    private String msg;

    /**
     * 当状态为发送失败的时候才有值,为错误编码
     */
    private String code;

    /**
     * 当发送成功的时候才有值 为该发送任务的唯一标识
     */
    private String sendId;


    /**
     * 发送成功所花费的金额
     */
    private BigDecimal fee;

    /**
     * 目标电话号码---该值只有在模板批量发送的时候才会返回
     */
    private String telephone;

    public MessageStatus(JSONObject jsonObject) {
        if(jsonObject != null){
            this.status = jsonObject.getString("status")!=null && jsonObject.getString("status").equals("success")?true:false;
            this.msg = jsonObject.getString("msg");
            this.code= jsonObject.getString("code");
            this.sendId = jsonObject.getString("send_id");
            this.fee= jsonObject.getBigDecimal("fee");
            this.telephone = jsonObject.getString("to");
        }
    }

    public MessageStatus(LinkedHashMap linkedHashMap) {
        if(linkedHashMap != null){
            this.status = linkedHashMap.get("status")!=null && linkedHashMap.get("status").equals("success")?true:false;
            this.msg = (String) linkedHashMap.get("msg");
            this.code= (String) linkedHashMap.get("code");
            this.sendId = (String) linkedHashMap.get("send_id");
            this.fee= new BigDecimal(String.valueOf(linkedHashMap.get("fee")==null?0:linkedHashMap.get("fee")));
            this.telephone = (String) linkedHashMap.get("to");
        }

    }
}
