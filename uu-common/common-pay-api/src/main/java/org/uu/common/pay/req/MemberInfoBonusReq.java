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
public class MemberInfoBonusReq implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;



    /**
     * 买入奖励比例
     */
    @ApiModelProperty("买入奖励比例")
    private BigDecimal buyBonusProportion;

    /**
     * 卖出奖励比例
     */
    @ApiModelProperty("卖出奖励比例")
    private BigDecimal sellBonusProportion;



    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}