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
@ApiModel(description = "会员信誉分请求参数")
public class MemberCreditScoreInfoIdReq implements Serializable {

    @ApiModelProperty("会员id")
    private Long id;


}