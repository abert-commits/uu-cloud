package org.uu.manager.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotNull;


/**
 * @author Admin
 */
@Data
@ApiModel(description = "代收月报表请求对象")
public class PaymentMonthOrderReportReq extends PageRequest {


    @ApiModelProperty(value = "开始时间", example = "时间格式：2023-05")
    private String startTime;

    @ApiModelProperty(value = "结束时间", example = "时间格式：2023-05")
    private String endTime;

    @ApiModelProperty(value = "商户code,商户后台必传", required = false)
    private String merchantCode;

    @ApiModelProperty("语言")
    private String lang ;
}
