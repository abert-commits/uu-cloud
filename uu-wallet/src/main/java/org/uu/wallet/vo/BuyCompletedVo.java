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
@ApiModel(description = "确认到账接口返回")
public class BuyCompletedVo implements Serializable {

    @ApiModelProperty("人工审核截至时间(分钟)")
    private Long delayMinutes;

    /**
     * 奖励
     */
    @ApiModelProperty("奖励")
    private BigDecimal bonus = new BigDecimal(0);
}