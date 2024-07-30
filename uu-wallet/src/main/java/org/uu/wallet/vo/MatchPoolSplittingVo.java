package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "匹配中(拆单)页面返回数据")
public class MatchPoolSplittingVo implements Serializable {


    /**
     * 总卖出数量
     */
    @ApiModelProperty(value = "总卖出数量")
    private BigDecimal amount;

    /**
     * 最小限额
     */
    @ApiModelProperty(value = "最小限额")
    private BigDecimal minimumAmount;

    /**
     * UPI_ID
     */
    @ApiModelProperty("UPI_ID")
    private String upiId;

    /**
     * 已卖出数量
     */
    @ApiModelProperty("已卖出数量")
    private BigDecimal soldAmount;

    /**
     * 剩余金额
     */
    @ApiModelProperty("剩余数量")
    private BigDecimal remainingAmount;

    /**
     * 卖出子订单列表
     */
    @ApiModelProperty("卖出子订单列表")
    List<SellOrderListVo> sellOrderList;
}