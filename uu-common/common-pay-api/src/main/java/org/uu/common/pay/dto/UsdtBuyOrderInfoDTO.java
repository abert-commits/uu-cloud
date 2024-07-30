package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "ustd买入订单返回")
public class UsdtBuyOrderInfoDTO implements Serializable {

    private  Long id;



    /**
     * USDT支付凭证
     */
    @ApiModelProperty("USDT支付凭证")
    private String usdtProof;
}