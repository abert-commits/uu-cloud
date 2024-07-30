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
public class MemberInfoIdReq implements Serializable {

    @ApiModelProperty("主键: 非必传")
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id: 必填")
    private String memberId;


    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}