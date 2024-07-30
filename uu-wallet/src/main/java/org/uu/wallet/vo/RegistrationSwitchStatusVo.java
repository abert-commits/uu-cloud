package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "查询注册相关开关状态 接口返回数据")
public class RegistrationSwitchStatusVo implements Serializable {

    @ApiModelProperty("检查手机注册验证码开关是否启用, 取值说明: true表示开启，false表示关闭")
    private Boolean mobileRegistrationCaptchaEnabled = false;

    @ApiModelProperty("检查邀请码注册开关是否启用, 取值说明: true表示开启，false表示关闭")
    private Boolean invitationCodeRegistrationEnabled = false;
}