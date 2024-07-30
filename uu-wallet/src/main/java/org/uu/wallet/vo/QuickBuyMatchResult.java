package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.uu.wallet.Enum.PayTypeEnum;

import java.math.BigDecimal;
import java.util.List;

/**
 * 快捷买入匹配结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "快捷买入匹配结果")
public class QuickBuyMatchResult {

    /**
     * 匹配金额
     */
    @ApiModelProperty(value = "匹配金额")
    private BigDecimal amount;

    /**
     * 匹配订单号
     */
    @ApiModelProperty(value = "匹配订单号")
    private String orderNo;

    /**
     * 支付方式
     */
    @ApiModelProperty(value = "支付方式 1: 银行卡  3: UPI")
    private String payType;

    /**
     * 小于匹配金额的推荐金额列表
     */
    @ApiModelProperty(value = "小于匹配金额的推荐金额列表")
    private List<SuggestOrderItem> littleSuggestItems;

    /**
     * 大于匹配金额的推荐金额列表
     */
    @ApiModelProperty(value = "小于匹配金额的推荐金额列表")
    private List<SuggestOrderItem> bigSuggestItems;


    /**
     * 推荐金额项
     */
    @Data
    public static class SuggestOrderItem {

        /**
         * 金额
         */
        @ApiModelProperty(value = "金额")
        private BigDecimal amount;

        /**
         * 订单号
         */
        @ApiModelProperty(value = "订单号")
        private String orderNo;

        /**
         * 支付方式
         */
        @ApiModelProperty(value = "支付方式 1: 银行卡  3: UPI")
        private String payType;
    }


}
