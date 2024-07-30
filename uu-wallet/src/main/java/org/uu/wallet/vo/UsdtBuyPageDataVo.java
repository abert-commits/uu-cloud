package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "USDT买入页面接口返回数据")
public class UsdtBuyPageDataVo implements Serializable {

    /**
     * 主网络
     */
    @ApiModelProperty(value = "主网络")
    private List<String> networkProtocolList;

    /**
     * USDT汇率
     */
    @ApiModelProperty(value = "USDT汇率")
    private BigDecimal usdtCurrency;

    /**
     * USDT买入记录
     */
    @ApiModelProperty(value = "USDT买入记录")
    private List<UsdtBuyOrderVo> UsdtBuyOrder;

}