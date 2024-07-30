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
@ApiModel(description = "添加 KYC Partner 请求参数")
public class KycPartnerReq implements Serializable {

    private static final long serialVersionUID = 1L; // 显式序列化版本ID


    /**
     * 银行编码
     */
    @NotBlank(message = "bankCode cannot be empty")
    @ApiModelProperty(value = "银行编码")
    private String bankCode;


    /**
     * 账户姓名
     */
    @NotBlank(message = "name cannot be empty")
    @ApiModelProperty(value = "姓名")
    private String name;


    /**
     * 账户
     */
//    @NotBlank(message = "account cannot be empty")
    @ApiModelProperty(value = "账户")
    private String account;


    /**
     * upi_id
     */
    @NotBlank(message = "upiId cannot be empty")
    @ApiModelProperty(value = "upiId")
    private String upiId;

    /**
     * 来源 1-app 2-H5
     */
    @ApiModelProperty(value = "来源 1-app 2-H5")
    private Integer sourceType = 1;

    /**
     * token
     */
    @NotBlank(message = "token")
    @ApiModelProperty("token")
    private String token;

    /**
     * 收款类型
     */
    @ApiModelProperty("1-银行 3-upi")
    private String collectionType = "3";


    /**
     * 银行卡号
     */
    @ApiModelProperty(value = "银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty(value = "持卡人")
    private String bankCardOwner;

    /**
     * Ifsc
     */
    @ApiModelProperty(value = "Ifsc")
    private String bankCardIfsc;

    @ApiModelProperty(value = "支付密码")
    private String paymentPassword;


}
