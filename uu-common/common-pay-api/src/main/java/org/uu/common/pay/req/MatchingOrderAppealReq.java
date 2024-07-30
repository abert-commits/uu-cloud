package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description ="撮合列表")
public class MatchingOrderAppealReq implements Serializable {


    @ApiModelProperty("撮合Id")
    private Long id;
    @ApiModelProperty("备注")
    private String  remark;
    @ApiModelProperty("实际金额")
    private BigDecimal orderActualAmount;
    @ApiModelProperty("更新人")
    private String updateBy;







}