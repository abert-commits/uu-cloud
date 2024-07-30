package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;


/**
 * @author Admin
 */
@Data
@ApiModel(description = "银行信息请求参数")
public class BankInfoUpdateStatusReq extends PageRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 更新状态 0-未启用 1-启用
     */
    @ApiModelProperty(value = "更新状态 0-未启用 1-启用")
    private Integer status;

}
