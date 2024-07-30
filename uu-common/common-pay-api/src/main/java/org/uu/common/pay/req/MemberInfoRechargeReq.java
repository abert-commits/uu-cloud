package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "上分请求参数")
public class MemberInfoRechargeReq  implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;



    @ApiModelProperty("买入金额")
    private BigDecimal buyAmount;


    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}