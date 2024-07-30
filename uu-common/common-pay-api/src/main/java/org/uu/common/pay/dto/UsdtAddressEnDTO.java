package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsdtAddressEnDTO implements Serializable {

    @ApiModelProperty("member id")
    private String memberId;


    @ApiModelProperty("network protocol")
    private String networkProtocol;

    @ApiModelProperty("usdt address")
    private String usdtAddr;


    @ApiModelProperty("order total")
    private Long orderTotal;

    @ApiModelProperty("order success number")
    private Long orderSuccessNum;

    @ApiModelProperty("usdt balance")
    private BigDecimal usdtBalance;

    @ApiModelProperty("trx balance")
    private BigDecimal trxBalance;


    @ApiModelProperty("create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}