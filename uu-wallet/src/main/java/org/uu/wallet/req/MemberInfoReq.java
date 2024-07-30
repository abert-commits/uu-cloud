package org.uu.wallet.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "创建会员请求参数")
public class MemberInfoReq {

    /**
     * 会员账号  目前仅支持手机号创建
     */
    @NotBlank(message = "Account(Phone Number) cannot be empty")
    @ApiModelProperty(value = "会员账号(手机号)")
    private String memberAccount;

    /**
     * 登录密码
     */
    @NotBlank(message = "password can not be blank")
    @ApiModelProperty(value = "登录密码")
    private String password;

    /**
     * 买入奖励比例(%)  如：0.1% 则传入0.1
     */
    @NotBlank(message = "Buy reward ratio can not be blank")
    @ApiModelProperty(value = "买入奖励比例(%)  如：0.1% 则传入0.1")
    private BigDecimal buyRewardRatio;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

}