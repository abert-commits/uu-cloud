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
@ApiModel(description = "usdt地址管理返回信息")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsdtAddressDTO implements Serializable {

    @ApiModelProperty("会员id")
    private String memberId;

    @ApiModelProperty("商户ID")
    private String merchantId;

    @ApiModelProperty("商户名称")
    private String merchantName;


    @ApiModelProperty("网络")
    private String networkProtocol;

    @ApiModelProperty("usdt地址")
    private String usdtAddr;


    @ApiModelProperty("订单总数")
    private Long orderTotal;

    @ApiModelProperty("订单成功数")
    private Long orderSuccessNum;

    @ApiModelProperty("USDT余额")
    private BigDecimal usdtBalance;

    @ApiModelProperty("TRX余额")
    private BigDecimal trxBalance;


    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}