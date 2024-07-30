package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@ApiModel(description = "添加收款信息请求参数")
public class CollectionInfoListPageReq extends PageRequest {

    @ApiModelProperty(value = "UPI_ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "UPI_Name")
    private String upiName;

    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;


    /**
     * 银行编码
     */
    @ApiModelProperty(value = "银行编码")
    private String bankCode;

    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人姓名")
    private String bankCardOwner;

    /**
     * 付款类型 1-印度银行卡 2-印度USDT 3-印度upi 4-印度pix
     */
    @ApiModelProperty("付款类型 1-印度银行卡 2-印度USDT 3-印度upi 4-印度pix")
    private Integer type;

    @ApiModelProperty(value = "会员ID")
    private String memberId;
}
