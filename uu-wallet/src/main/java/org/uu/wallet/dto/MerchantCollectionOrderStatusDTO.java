package org.uu.wallet.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("商户代收订单DTO实体类")
public class MerchantCollectionOrderStatusDTO implements Serializable {
    private static final long serialVersionUID = -1734173004778133117L;

    @ApiModelProperty("订单状态 1-支付中 2-已完成 3-代收失败")
    private String orderStatus;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("同步通知地址")
    private String syncNotifyAddress;
}
