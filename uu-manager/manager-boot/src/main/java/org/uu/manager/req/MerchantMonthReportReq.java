package org.uu.manager.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;


/**
 * @author Admin
 */
@Data
@ApiModel(description = "商户月报表请求对象")
public class MerchantMonthReportReq extends PageRequest {


    @ApiModelProperty(value = "开始时间", example = "时间格式：2023-05-23")
    private String startTime;

    @ApiModelProperty(value = "结束时间", example = "时间格式：2023-05-23")
    private String endTime;

    @ApiModelProperty(value = "商户名称", required = false)
    private Long merchantId;

    @ApiModelProperty(value = "商户code", required = false)
    private String merchantCode;

    @ApiModelProperty(value = "币种", required = false)
    private String currency;

    @ApiModelProperty("语言")
    private String lang ;


}
