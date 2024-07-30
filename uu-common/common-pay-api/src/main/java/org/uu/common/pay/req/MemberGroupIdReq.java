package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

/**
* 会员分组
*
* @author 
*/
    @Data
    @ApiModel(description = "分组请求参数")
    @EqualsAndHashCode(callSuper = false)
    public class MemberGroupIdReq implements Serializable {


            /**
            * 分组名称
            */
            @ApiModelProperty("主键")
     private Long id;






}