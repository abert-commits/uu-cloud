package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@ApiModel(description = "添加收款信息请求参数")
public class CollectionInfoIdReq implements Serializable {

    @ApiModelProperty(value = "主键")
    private Long id;

//    @ApiModelProperty(value = "会员ID")
//    private String memberId;
}
