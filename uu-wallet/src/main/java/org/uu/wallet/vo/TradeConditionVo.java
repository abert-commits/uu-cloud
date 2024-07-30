package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 买入\卖出 交易入口页面VO类
 *
 * @author
 */
@Data
@ApiModel(description = "交易入口页面-当前用户交易数据信息")
public class TradeConditionVo {
    /*
    会员账户现有ARB的数量
     */
    @ApiModelProperty("账户现有ARB的数量")
    private BigDecimal balance;

    /*
    总奖励
     */
    @ApiModelProperty("累计奖励")
    private BigDecimal totalBonus;

    /*
    今日交易中(冻结金额)
     */
    @ApiModelProperty("交易中")
    private BigDecimal frozenAmount;

    /*
    今日交易金额
     */
    @ApiModelProperty("今日交易金额")
    private BigDecimal todayTradeAmount = BigDecimal.ZERO;

    /*
    今日奖励
     */
    @ApiModelProperty("今日奖励")
    private BigDecimal todayBonus = BigDecimal.ZERO;

    /*
    买入奖励比例
     */
    @ApiModelProperty("买入奖励比例")
    private BigDecimal buyBonusProportion = BigDecimal.ZERO;

    /*
    USDT汇率
     */
    @ApiModelProperty("USDT汇率")
    private BigDecimal usdtRate = BigDecimal.ZERO;
}
