package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "充值下单接口返回数据")
public class CollectionResVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 商户订单号
     */
    @ApiModelProperty(value = "商户订单号")
    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty(value = "平台订单号")
    private String platformOrder;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;

    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private String merchantCode;

    /**
     * 时间戳
     */
    @ApiModelProperty(value = "时间戳")
    private String timestamp;

    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI_ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "UPI_Name")
    private String upiName;
}