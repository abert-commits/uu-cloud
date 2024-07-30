package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;


/**
 * 会员信息表
 *
 * @author admin
 */
@Data
@ApiModel(description = "会员列表请求")
public class CashBackOrderListPageReq extends PageRequest {

    @ApiModelProperty("主键")
    private Long id;

    /**
     * 商户订单号
     */
    @ApiModelProperty("商户订单号")
    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty("平台订单号")
    private String platformOrder;


    /**
     * 订单状态 1-退回中 2-退回成功 3-退回失败
     */
    @ApiModelProperty("订单状态 1-退回中 2-退回成功 3-退回失败")
    private String orderStatus;

    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;

    /**
     * 商户会员id
     */
    @ApiModelProperty("商户会员id")
    private String merchantMemberId;

    /**
     * 钱包会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    @ApiModelProperty(value = "开始时间" , required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "结束时间", required = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}