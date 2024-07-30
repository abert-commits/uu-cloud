package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;

/**
 * @author admin
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "已支付/未支付请求参数")
public class PaidParamReq {

    @ApiModelProperty("订单id")
    private String id;
    @ApiModelProperty("类型 1-卖出 2-买入 3-代收 4-代付 5-usdt代收 6-usdt-代付")
    private String type;
    @ApiModelProperty("实际金额")
    private BigDecimal actualAmount;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("币种 TRC20 FCB TRX (usdt已支付未支付需要传)")
    private String balanceType;

    @ApiModelProperty("操作人")
    private String updateBy;

}