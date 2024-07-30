package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.util.List;


/**
 * @author
 */
@Data
@ApiModel(description = "商户请求参数说明")
public class MerchantInfoListPageReq extends PageRequest {


    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String username;


    /**
     * 商户编码
     */
    @ApiModelProperty(value = "appid就是商户号")
    private String code;


    @ApiModelProperty(value = "商户类型 1内部商户 2 外部商户")
    private String merchantType;


    /**
     * 账号
     */
    @ApiModelProperty(value = "商家号")
    private String account;

    /**
     * 状态
     */
    @ApiModelProperty(value = "0-禁止代收 1-正常代收 2-禁止代付 3-代付正常")
    private String status;

    @ApiModelProperty(value = "充值状态")
    private String rechargeStatus;
    @ApiModelProperty(value = "出款状态")
    private String withdrawalStatus;

    @ApiModelProperty(value = "0-余额过低 3-正常")
    private String riskTag;

    @ApiModelProperty("商户号列表")
    private List<String> merchantCodes;

    @ApiModelProperty(value = "币种")
    private String currency;

}