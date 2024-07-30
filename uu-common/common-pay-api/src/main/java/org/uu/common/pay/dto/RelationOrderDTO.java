package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description = "关联订单信息返回")
public class RelationOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("会员id")
    private Long id;


    /**
     * 充值平台订单号
     */
    @ApiModelProperty("买入订单号")
    private String buyOrderNo;


    /**
     * 提现平台订单号
     */
    @ApiModelProperty("卖出订单号")
    private String sellOrderNo;

    /**
     * 匹配订单号
     */
    @ApiModelProperty("匹配订单号")
    private String matchOrder;

    /**
     * 订单提交金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal amount;

    /**
     * 订单实际金额
     */
    @ApiModelProperty("订单实际金额")
    private BigDecimal actualAmount;


    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;



    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 商户code
     */
    @ApiModelProperty("商户code")
    private String merchantCode;

    @ApiModelProperty("商户会员ID")
    private String memberId;


    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态")
    private String orderStatus;


}