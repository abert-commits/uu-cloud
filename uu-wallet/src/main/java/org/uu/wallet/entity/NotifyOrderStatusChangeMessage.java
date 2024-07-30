package org.uu.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotifyOrderStatusChangeMessage implements Serializable {

    //会员id
    private String memberId;

    //通知类型
    private String type; // 使用枚举的code

    //订单号
    private String platformOrder;
}
