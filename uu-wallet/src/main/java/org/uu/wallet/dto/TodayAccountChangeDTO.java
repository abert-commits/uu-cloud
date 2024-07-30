package org.uu.wallet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 今日帐变数据统计
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodayAccountChangeDTO {
    /*
    今日交易金额=今日卖出数量+今日买入数量
     */
    private BigDecimal todayTradeAmount;

    /*
    今日奖励=今日买入奖励+今日卖出奖励+今日团队奖励+今日分红奖励
     */
    private BigDecimal todayBonus;
}
