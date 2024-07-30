package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "上分请求参数")
public class MemberInfoWithdrawalReq implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;



    @ApiModelProperty("卖出金额")
    private BigDecimal sellAmount;


    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}