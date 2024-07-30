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
@ApiModel(description = "获取USDT汇率和支付类型返回数据")
public class UsdtCurrencyAndPayTypeVo implements Serializable {

    /**
     * 支付类型
     */
    @ApiModelProperty(value = "支付类型")
    private List<PaymentTypeVo> paymentTypeVo;

    /**
     * USDT汇率
     */
    @ApiModelProperty("USDT汇率")
    private BigDecimal usdtCurrency;

}