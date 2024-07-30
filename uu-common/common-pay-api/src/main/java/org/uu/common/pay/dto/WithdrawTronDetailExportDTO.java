package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "代付钱包交易记录返回")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawTronDetailExportDTO implements Serializable {


    @ApiModelProperty("订单号")
    private String orderId;


    @ApiModelProperty("交易ID")
    private String txid;


    @ApiModelProperty("币种")
    private String symbol;


    @ApiModelProperty("目标地址")
    private String toAddress;


    @ApiModelProperty("交易金额")
    private BigDecimal amount;


    /**
     * 0转账中 1成功 2 失败 默认1
     */
    @ApiModelProperty("转账状态")
    private String status;


    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}