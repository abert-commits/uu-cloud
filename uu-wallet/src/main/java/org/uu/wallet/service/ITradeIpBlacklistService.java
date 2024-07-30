package org.uu.wallet.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TradeIpBlackListPageDTO;
import org.uu.common.pay.req.TradeIpBlackListReq;
import org.uu.wallet.entity.TradIpBlackMessage;
import org.uu.wallet.entity.TradeIpBlacklist;

/**
 * <p>
 * 交易IP黑名单表，用于存储不允许进行交易的IP地址 服务类
 * </p>
 *
 * @author
 * @since 2024-02-21
 */
public interface ITradeIpBlacklistService extends IService<TradeIpBlacklist> {

    /**
     * 查看交易ip是否在黑名单中
     *
     * @param ip
     * @return {@link Boolean}
     */
    Boolean isIpBlacklisted(String ip);

    PageReturn<TradeIpBlackListPageDTO> listPage(TradeIpBlackListReq req);

    RestResult save(TradeIpBlackListReq req);

    boolean del(String id);

    /**
     * 添加Ip黑名单回调方法
     *
     * @param tradIpBlackMessage
     * @return
     */
    void addBlackIpCallback(TradIpBlackMessage tradIpBlackMessage);
}
