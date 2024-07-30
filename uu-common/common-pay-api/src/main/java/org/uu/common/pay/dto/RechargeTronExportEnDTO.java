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
@ApiModel(description = "代收钱包交易记录")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RechargeTronExportEnDTO implements Serializable {

    @ApiModelProperty("member id")
    private String memberId;

    @ApiModelProperty("merchant id")
    private String merchantId;


    @ApiModelProperty("trade id")
    private String txid;


    @ApiModelProperty("order id")
    private String orderId;


    @ApiModelProperty("symbol")
    private String symbol;

    @ApiModelProperty("amount")
    private BigDecimal amount;

    @ApiModelProperty("fromAddress")
    private String fromAddress;

    @ApiModelProperty("toAddress")
    private String toAddress;

    @ApiModelProperty("bet time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime betTime;

    @ApiModelProperty("create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}