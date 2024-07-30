package org.uu.wallet.thirdParty;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class MessageMultiObject {

    private JSONArray jsonArray;

    public MessageMultiObject() {
        jsonArray = new JSONArray();
    }

    public void add(String telephone, JSONObject vars) {
        JSONObject jobject = new JSONObject();
        jobject.put("to", telephone);
        jobject.put("vars", vars);
        jsonArray.add(jobject);
    }

    public String toJSONString() {
        return jsonArray.toJSONString();
    }
}
