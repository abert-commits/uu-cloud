package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
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
@ApiModel(description = "波场钱包地址管理返回信息")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronWalletAddressDTO implements Serializable {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("usdt地址")
    private String address;

    @ApiModelProperty("trx余额")
    private BigDecimal trxBalance;

    @ApiModelProperty("USDT余额")
    private BigDecimal usdtBalance;

    @ApiModelProperty("账户类型, 1: 中转账户, 2: 资金账户, 3: 出款账户")
    private Integer walletType;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}