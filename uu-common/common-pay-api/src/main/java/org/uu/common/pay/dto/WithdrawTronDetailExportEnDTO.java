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
public class WithdrawTronDetailExportEnDTO implements Serializable {


    @ApiModelProperty("order id")
    private String orderId;


    @ApiModelProperty("trade id")
    private String txid;


    @ApiModelProperty("symbol")
    private String symbol;


    @ApiModelProperty("toAddress")
    private String toAddress;


    @ApiModelProperty("amount")
    private BigDecimal amount;


    /**
     * 0转账中 1成功 2 失败 默认1
     */
    @ApiModelProperty("status")
    private String status;


    @ApiModelProperty("create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}