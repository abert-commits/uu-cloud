package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("商户代收订单状态Req实体")
public class MerchantCollectionOrderStatusReq implements Serializable {
    private static final long serialVersionUID = -4307819529558698469L;

    @ApiModelProperty("商户代收订单号")
    @NotEmpty(message = "Please specify the merchantCollection orderNo")
    private String merchantCollectionOrderNo;

    @ApiModelProperty("代收订单币种 如：USDT、TRX、INR")
    @NotEmpty(message = "Please specify the currency")
    private String currency;
}
