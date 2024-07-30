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
 * 人工审核信息
 * </p>
 *
 * @author 
 * @since 2024-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "人工审核信息")
public class TradeManualConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预警余额
     */
    @ApiModelProperty("是否人工审核 0-未开启 1-开启")
    private Integer isManualReview;

    /**
     * 确认超时未操作
     */
    @ApiModelProperty("审核时间")
    private Integer manualReviewTime;


}
