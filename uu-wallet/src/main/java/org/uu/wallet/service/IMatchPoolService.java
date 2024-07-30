package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.MatchPoolListPageDTO;
import org.uu.common.pay.dto.PaymentOrderChildDTO;
import org.uu.common.pay.req.MatchPoolGetChildReq;
import org.uu.common.pay.req.MatchPoolListPageReq;
import org.uu.wallet.entity.MatchPool;

import java.util.List;

/**
 * @author
 */
public interface IMatchPoolService extends IService<MatchPool> {

    /**
     * 根据订单号获取订单信息
     *
     * @param orderNo
     * @return {@link MatchPool}
     */
    MatchPool getMatchPoolOrderByOrderNo(String orderNo);


    PageReturn<MatchPoolListPageDTO> listPage(MatchPoolListPageReq req);

    MatchPoolListPageDTO matchPooTotal(MatchPoolListPageReq req);


    List<PaymentOrderChildDTO> getChildren(MatchPoolGetChildReq req);
}
