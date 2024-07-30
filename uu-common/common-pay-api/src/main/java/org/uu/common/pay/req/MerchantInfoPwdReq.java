package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Admin
 */
@Data
@ApiModel(description = "商户密码信息")
public class MerchantInfoPwdReq {

    @ApiModelProperty(value = "id", required = true)
    private Long id;

    /**
     * 商户名
     */
    @ApiModelProperty(value = "原密码", required = false)
    private String originalPwd;

    /**
     * 新密码
     */
    @ApiModelProperty(value = "新密码", required = true)
    private String newPwd;

    /**
     * 确认密码
     */
    @ApiModelProperty(value = "确认密码", required = true)
    private String confirmNewPwd;

    /**
     * 确认密码
     */
    @ApiModelProperty(value = "密码提示", required = false)
    private String pwdTips;

}
