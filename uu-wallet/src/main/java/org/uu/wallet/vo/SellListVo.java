package org.uu.wallet.vo;

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
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "卖出页面接口")
public class SellListVo implements Serializable {

    private static final long serialVersionUID = -4346412359258856532L;

    @ApiModelProperty(value = "可用余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "卖出余额")
    private BigDecimal sellBalance;

    @ApiModelProperty(value = "交易中")
    private BigDecimal inTransaction;

    @ApiModelProperty(value = "正在进行中的订单列表")
    private List<OnSellingOrderVO> onSellingOrderVOList;
}