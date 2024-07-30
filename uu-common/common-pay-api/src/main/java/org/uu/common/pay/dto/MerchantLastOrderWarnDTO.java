package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "商户最后一笔代收/代付订单发生时间过久告警")
public class MerchantLastOrderWarnDTO implements Serializable {

    /**
     * 商户代收最后一笔订单列表
     */
    @ApiModelProperty("商户代收最后一笔订单列表")
    private String merchantName;

    /**
     * 是否告警
     */
    @ApiModelProperty("是否告警")
    private boolean isWarn;

    /**
     * 阈值
     */
    @ApiModelProperty(value = "阈值")
    private Integer threshold;

}
