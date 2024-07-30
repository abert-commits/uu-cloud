package org.uu.wallet.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author
 */
@Data
@ApiModel(description = "买入下单接口返回数据")
public class BuyVo implements Serializable {


    /**
     * 转账金额
     */
    @ApiModelProperty(value = "转账金额")
    private BigDecimal amount;

    /**
     * 订单号
     */
    @ApiModelProperty(value = "订单号")
    private String platformOrder;

    /**
     * 支付剩余时间
     */
    @ApiModelProperty(value = "支付剩余时间 单位: 秒  如果值为null或负数 表示该笔订单已过期")
    private Long paymentExpireTime;

    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人姓名")
    private String bankCardOwner;


    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;


    /**
     * ifsc_code
     */
    @ApiModelProperty("ifsc_code")
    private String ifscCode;


    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式, 1: 银行卡, 3: UPI")
    private String payType;


    /**
     * 付款人 kyc bankName
     */
    @ApiModelProperty(value = "付款人 kyc bankName")
    private String kycBankName;

    /**
     * 付款人 kyc account
     */
    @ApiModelProperty(value = "付款人 kyc account")
    private String kycAccount;

    /**
     * 是否上传过支付凭证
     */
    @ApiModelProperty(value = "是否上传过支付凭证, 1: 已上传过, 0: 未上传")
    private String paymentReceiptUploaded;

    /**
     * 支付凭证
     */
    @ApiModelProperty(value = "支付凭证")
    private String voucher;
}