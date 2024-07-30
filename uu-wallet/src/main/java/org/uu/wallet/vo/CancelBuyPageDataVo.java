package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "获取买入取消原因列表接口返回数据")
public class CancelBuyPageDataVo implements Serializable {


    /**
     * UPI_ID
     */
    @ApiModelProperty("upiId")
    private String upiId;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal amount;

    /**
     * 平台订单号
     */
    @ApiModelProperty("平台订单号")
    private String platformOrder;

    /**
     * 订单时间
     */
    @ApiModelProperty("订单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 取消原因列表
     */
    @ApiModelProperty(value = "取消原因列表")
    private List<String> reason;

    /**
     * 收款类型 1: 银行卡, 3: UPI
     */
    @ApiModelProperty(value = "支付类型, 1: 银行卡, 3: UPI")
    private String payType;


    @ApiModelProperty(value = "银行卡号")
    private String bankCardNumber;
}