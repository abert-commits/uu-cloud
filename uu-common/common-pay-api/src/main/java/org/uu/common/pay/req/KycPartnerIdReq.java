package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 添加 KYC Partner 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "KYC Partner id请求参数")
public class KycPartnerIdReq implements Serializable {

    private static final long serialVersionUID = 1L; // 显式序列化版本ID
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    private Long id;


}
