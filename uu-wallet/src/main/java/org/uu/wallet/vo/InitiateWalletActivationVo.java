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
@ApiModel(description = "激活钱包-返回数据")
public class InitiateWalletActivationVo implements Serializable {

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
     * 返回地址
     */
    @ApiModelProperty("返回地址")
    private String returnUrl;
}