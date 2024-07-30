package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


/**
 * @author Admin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "银行信息请求参数")
public class BankInfoListPageReq extends PageRequest {

    /**
     * 银行名称
     */
    @ApiModelProperty("银行名称")
    private String bankName;


    /**
     * ifsc_code
     */
    @ApiModelProperty("ifsc_code")
    private String ifscCode;

    /**
     * 银行代码
     */
    @ApiModelProperty("银行代码")
    private String bankCode;

    /**
     * 银行logo
     */
    @ApiModelProperty("银行logo")
    private String iconUrl;

    /**
     * 状态 0-未启用 1-启用
     */
    @ApiModelProperty("状态 0-未启用 1-启用")
    private Integer status;

}
