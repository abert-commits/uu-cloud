package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* 会员分组
*
* @author 
*/
    @Data
    public class MemberAuthListDTO implements Serializable {

            @ApiModelProperty("权限名称")
            private String name;

            /**
            * 金额
            */
            @ApiModelProperty("权限编码")
            private String code;




}