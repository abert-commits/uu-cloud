package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(description = "分红配置请求参数")
public class DividendConfigReq {

    /**
     * 分红临界点
     */
    private Long criticalPoint;

    /**
     * 奖励比例
     */
    private BigDecimal rewardRatio;
}
