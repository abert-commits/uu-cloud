package org.uu.wallet.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 添加 KYC Partner 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "获取KYC Partner列表 请求参数")
public class KycPartnerListReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收款类型
     */
    @ApiModelProperty(value = "收款类型 1-银行卡 3-upi")
    private Integer collectType;


}
