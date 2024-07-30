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
@ApiModel(description = "校验手机号是否被使用接口返回数据")
public class CheckPhoneNumberAvailabilityVo implements Serializable {

    @ApiModelProperty("手机号是否已经被使用, 取值说明: true表示已使用，false表示未使用")
    private Boolean isUsed = false;
}