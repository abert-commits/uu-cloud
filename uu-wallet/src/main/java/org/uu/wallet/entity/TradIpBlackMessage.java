package org.uu.wallet.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TradIpBlackMessage implements Serializable {

    /**
     * 黑名单启用/禁用
     */
    private String type;

    /**
     * 黑名单自动/手动 1-自动 2-手动
     */
    private String autoFlag;

    /**
     * 黑名单信息
     */
    private TradeIpBlacklist tradeIpBlacklist;
}
