package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 预警参数配置表
 * </p>
 *
 * @author 
 * @since 2024-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "预警信息")
public class TradeBankConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警余额
     */
    @ApiModelProperty("最小银行卡号长度")
    private Integer minBankCodeNumber;

    /**
     * 最大银行卡号长度
     */
    @ApiModelProperty("最大银行卡号长度")
    private Integer maxBankCodeNumber;


}
