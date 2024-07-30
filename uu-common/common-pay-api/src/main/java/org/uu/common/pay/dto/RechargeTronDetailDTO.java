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
public class RechargeTronDetailDTO implements Serializable {

    private Long id;


    @ApiModelProperty("商户ID")
    private String merchantId;


    @ApiModelProperty("会员ID")
    private String memberId;


    @ApiModelProperty("交易ID")
    private String txid;


    @ApiModelProperty("订单号")
    private String orderId;


    @ApiModelProperty("币种")
    private String symbol;


    @ApiModelProperty("付款地址")
    private String fromAddress;


    @ApiModelProperty("收款地址")
    private String toAddress;


    @ApiModelProperty("交易金额")
    private BigDecimal amount;


    @ApiModelProperty("转账时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime betTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}