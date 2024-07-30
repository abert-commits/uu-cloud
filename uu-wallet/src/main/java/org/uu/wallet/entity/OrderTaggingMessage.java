package org.uu.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderTaggingMessage implements Serializable {

    /**
     * 风控类型
     */
    private String riskType;

    /**
     * 订单类型
     */
    private String orderType;

    /**
     * 订单号-tag原因
     */
    private Map<String, String> platformOrderTags;


}
