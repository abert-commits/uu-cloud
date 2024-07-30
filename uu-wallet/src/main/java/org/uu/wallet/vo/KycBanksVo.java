package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取银行列表返回数据")
public class KycBanksVo implements Serializable {

    /**
     * 银行名称
     */
    @ApiModelProperty("银行名称")
    private String bankName;

    /**
     * 银行编码
     */
    @ApiModelProperty("银行编码")
    private String bankCode;

    /**
     * 服务编码
     */
    @ApiModelProperty("服务编码")
    private String serviceCode;

    /**
     * 图标地址
     */
    @ApiModelProperty("图标地址")
    private String iconUrl;

    /**
     * 状态
     */
    @ApiModelProperty("状态 1: 开启, 0: 关闭")
    private Integer status;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 连接地址
     */
    @ApiModelProperty("连接地址")
    private String linkUrl;

    /**
     * 连接方式, 1: 唤醒APP, 2: 跳转H5
     */
    private String linkType;

    /**
     * 来源 1: app 2: H5
     */
    @ApiModelProperty("来源 1: app 2: H5")
    private Integer sourceType;

    @ApiModelProperty("sms短信验证码长度")
    private Integer smsCodeLength;

    /**
     * kyc类型
     */
    @ApiModelProperty(value = "1：银行卡 3：upi")
    private Integer type;
}