package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author
 */
@Data
@ApiModel(description = "获取当前会员kyc绑定情况")
public class KycBindLinkStatusVo {

    /*
    是否有绑定的kyc: true有绑定kyc,false没绑定kyc
     */
    @ApiModelProperty(value = "是否有绑定的kyc: true有绑定kyc,false没绑定kyc")
    private Boolean hasBindKyc;

    /*
    是否有活跃的kyc: true有活跃kyc,false没活跃kyc
     */
    @ApiModelProperty(value = "是否有链接活跃的kyc: true有链接活跃的kyc,false没有链接活跃的kyc")
    private Boolean hasEnableKyc;
}
