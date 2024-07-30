package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

/**
 * 添加 KYC Partner 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "KYC Partner 更新")
public class KycPartnerUpdateReq implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("id")
    private Long id;

    /**
     * 银行名称
     */
    @ApiModelProperty("银行名称")
    private String bankName;

    /**
     * upi_id
     */
    @ApiModelProperty("upiId")
    private String upiId;


    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人")
    private String bankCardOwner;

    /**
     * ifsc
     */
    @ApiModelProperty("ifsc")
    private String bankCardIfsc;

}
