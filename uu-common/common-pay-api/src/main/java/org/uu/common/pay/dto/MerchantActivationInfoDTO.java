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
@ApiModel(description = "商户信息")
public class MerchantActivationInfoDTO implements Serializable {



    @ApiModelProperty(value = "新增激活用户")
    private Long todayActivationNum;

    /**
     * 商户名
     */
    @ApiModelProperty(value = "总激活用户")
    private Long activationTotalNum;

    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * 商户code
     */
    @ApiModelProperty(value = "商户code")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;


    /**
     * 商户类型
     */
    @ApiModelProperty(value = "商户类型")
    private String merchantType;


}