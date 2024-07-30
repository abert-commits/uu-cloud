package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author admin
 * @date 2024/4/24 15:22
 */
@Data
@ApiModel(description = "kyc银行交易记录")
public class BankKycTransactionVo implements Serializable {


    /**
     * 付款人UPI
     */
    @ApiModelProperty("付款人UPI")
    private String payerUPI;

    /**
     * 收款人UPI
     */
    @ApiModelProperty("收款人UPI")
    private String recipientUPI;

    /**
     * 金额
     */
    @ApiModelProperty("金额")
    private BigDecimal amount;

    /**
     * 交易状态 1: 交易成功
     */
    @ApiModelProperty("交易状态")
    private String orderStatus;

    /**
     * UTR
     */
    @ApiModelProperty("UTR")
    private String UTR;

    /**
     * 交易类型, 1: 收入, 2: 支出
     */
    @ApiModelProperty("mode")
    private String mode;

    /**
     * 银行交易时间
     */
    @ApiModelProperty("银行交易时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    /**
     * 详情
     */
    @ApiModelProperty("detail")
    private String detail;

}
