package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lukas
 */
@Data
@ApiModel(description = "KYC Partner 请求参数")
public class KycPartnerMemberReq {

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;
}
