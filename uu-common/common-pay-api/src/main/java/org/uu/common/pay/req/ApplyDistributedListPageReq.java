package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "下发申请参数")
public class ApplyDistributedListPageReq extends PageRequest {


    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("商户名")
    private String username;

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("商户号")
    private String merchantCode;

    @ApiModelProperty("商户号列表")
    private List<String> merchantCodes;

    @ApiModelProperty(value = "币种")
    private String currency;
}