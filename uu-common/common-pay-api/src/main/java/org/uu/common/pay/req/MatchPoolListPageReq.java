package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配池
 *
 * @author
 */
@Data
@ApiModel(description = "匹配池参数说明")
public class MatchPoolListPageReq extends PageRequest {


    /**
     * 匹配订单号
     */
    @ApiModelProperty("匹配订单号")
    private String matchOrder;


    /**
     * 订单状态
     */
    @ApiModelProperty(" 1: 匹配中,  2: 匹配超时,  7: 已完成,  8: 已取消,  14: 进行中")
    private String orderStatus;

    /**
     *
     */
    @ApiModelProperty("订单提交时间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @ApiModelProperty("订单提交时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;


    /**
     *
     */
    @ApiModelProperty("最小限额开始")
    private BigDecimal minimumAmountStart;
    @ApiModelProperty("最小限额结束")
    private BigDecimal minimumAmountEnd;
    @ApiModelProperty("订单金额开始")
    private BigDecimal amountStart;
    @ApiModelProperty("订单金额结束")
    private BigDecimal amountEnd;


}