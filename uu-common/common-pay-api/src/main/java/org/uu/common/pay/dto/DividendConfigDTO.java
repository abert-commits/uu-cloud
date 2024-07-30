package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 交易配置表
 *
 * @author
 */
@Data
@ApiModel(description = "交易配置表返回")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DividendConfigDTO implements Serializable {
    /**
     * 分红配置ID
     */
    @ApiModelProperty("主键")
    private Integer id;

    /**
     * 分红临界点
     */
    private Long criticalPoint;

    /**
     * 奖励比例
     */
    private BigDecimal rewardRatio;

    /**
     * 分红金额  criticalPoint * rewardRatio / 100
     */
    private BigDecimal dividendAmount;


}