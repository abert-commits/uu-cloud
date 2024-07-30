package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "usdt查询请求参数")
public class UsdtConfigIdReq implements Serializable {
    @ApiModelProperty("主键")
    private long id;


}