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
@Data
@ApiModel(description = "银行信息请求参数")
public class BankInfoReq {

    /**
     * 银行名称
     */
    @NotNull(message = "银行名称不能为空")
    @ApiModelProperty("银行名称")
    @Pattern(regexp = "^[^\\u4e00-\\u9fa5]+$", message = "不能输入中文")
    private String bankName;


    /**
     * ifsc_code
     */
//    @NotNull(message = "ifscCode不能为空")
//    @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$", message = "ifscCode格式不正确")
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
