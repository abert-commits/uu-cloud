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
@ApiModel(description = "获取支付类型接口返回数据")
public class PaymentTypeVo implements Serializable {

    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式 取值说明: 1: 印度银行卡, 3: 印度UPI")
    private String  payType;

    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式 名称说明")
    private String  name;
}