package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 激活钱包接口 返回数据
 *
 * @author Simon
 * @date 2023/12/29
 */
@Data
@ApiModel(description = "手机号注册-返回数据")
public class PhoneSignUpVo implements Serializable {

    /**
     * 钱包地址
     */
    @ApiModelProperty("钱包地址")
    private String walletAccessUrl;


    /**
     * 会员ID
     */
    @ApiModelProperty("会员ID")
    private String memberId;


    /**
     * token
     */
    @ApiModelProperty("token")
    private String token;

    /**
     * token
     */
    @ApiModelProperty("refreshToken")
    private String refreshToken;

}