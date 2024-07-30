package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "代收钱包交易记录请求")
public class RechargeTronDetailReq extends PageRequest {

    @ApiModelProperty("交易ID")
    private String txid;

    @ApiModelProperty("订单号")
    private String orderId;

    /**
     * 订单开始时间
     */
    @ApiModelProperty("订单开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @ApiModelProperty("订单结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;

    @ApiModelProperty("语言")
    private String lang;

}