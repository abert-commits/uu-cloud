package org.uu.wallet.thirdParty;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TelephoneStatus {

    /**
     * true 成功,false失败
      */
    private Boolean status ;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 回调标识 针对每个手机号
     */
    private String voiceId;

    /**
     * 根据voiceid 查询发送状态是会置入对应的手机号
     */
    private String telephoneNum;


    public TelephoneStatus(Boolean status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public TelephoneStatus(Boolean status, String reason, String voiceId) {
        this.status = status;
        this.reason = reason;
        this.voiceId = voiceId;
    }

    public TelephoneStatus(Boolean status, String reason, String voiceId, String telephoneNum) {
        this.status = status;
        this.reason = reason;
        this.voiceId = voiceId;
        this.telephoneNum = telephoneNum;
    }
}
