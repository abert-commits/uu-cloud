package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.Pattern;


/**
 * @author Admin
 */
@Data
@ApiModel(description = "银行信息请求参数")
public class BankInfoUpdateReq extends PageRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 银行名称
     */
    @ApiModelProperty("银行名称")
    private String bankName;

    /**
     * 银行编码
     */
    @ApiModelProperty("银行编码")
    private String bankCode;


    /**
     * ifsc_code
     */
    @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$", message = "ifscCode格式不正确")
    @ApiModelProperty("ifsc_code")
    private String ifscCode;


    /**
     * 银行logo
     */
    @ApiModelProperty("银行logo")
    private String iconUrl;

}
