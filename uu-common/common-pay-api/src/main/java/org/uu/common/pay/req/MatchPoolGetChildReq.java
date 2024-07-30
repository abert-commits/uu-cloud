package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配池
 *
 * @author
 */
@Data
@ApiModel(description ="匹配池参数说明")
public class MatchPoolGetChildReq implements Serializable {




    /**
     * 匹配订单号
     */
    @ApiModelProperty("匹配订单号")
    private String matchOrder;




}