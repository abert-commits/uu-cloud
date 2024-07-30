package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "能量租用记录数据统计返回")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronRentEnergyStatisticsDTO implements Serializable {


    /**
     * 租用数量
     */
    private Long pageAmount;

    /**
     * 租用数量
     */
    private Long amountTotal;


}