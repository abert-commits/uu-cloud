package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;
import java.util.List;

/**
* 会员分组
*
* @author 
*/
    @Data
    @ApiModel(description = "会员分组")
    @EqualsAndHashCode(callSuper = false)
    public class MemberGroupListPageReq extends PageRequest {


            /**
            * 分组名称
            */
            @ApiModelProperty("名称")
    private String name;







}