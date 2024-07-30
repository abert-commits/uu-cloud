package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import scala.Int;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 连接 KYC Partner 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "连接 KYC Bank 请求参数")
public class KycBankReq implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 来源
     */
    @ApiModelProperty(value = "1: app 2: H5")
    private Integer sourceType;



    /**
     * kyc类型 1：银行卡 3：upi
     */
    @ApiModelProperty(value = "1：银行卡 3：upi")
    private Integer type;
}
