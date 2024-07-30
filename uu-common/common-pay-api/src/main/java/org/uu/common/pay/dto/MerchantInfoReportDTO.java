package org.uu.common.pay.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel("商户报表")
public class MerchantInfoReportDTO implements Serializable {


    /**
     * 商户名
     */
     @ApiModelProperty("商户名")
    private String username;


    /**
     * 商户编码
     */
    @ApiModelProperty("商户编码")
    private String code;
    @ApiModelProperty("日期")
    private String dateInterval;
    @ApiModelProperty("商户类型")
    private String merchantType;
    @ApiModelProperty("数量")
    private long quantity;
    @ApiModelProperty("完成")
    private long finish;
    
    private BigDecimal cost;
    private BigDecimal amount;
    private  BigDecimal successRate;
    private BigDecimal psuccessRate;
    private BigDecimal pamount;
    private long pquantity;
    private long pfinish;
    private String currency;

}