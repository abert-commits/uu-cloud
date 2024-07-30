package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "上分请求参数")
public class MemberInfoIdGetInfoReq implements Serializable {

    @ApiModelProperty("主键")
    private Long id;




}