package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.CashBackOrderApiDTO;
import org.uu.common.pay.dto.CashBackOrderListPageDTO;
import org.uu.common.pay.req.CashBackOrderListPageReq;
import org.uu.wallet.entity.CashBackOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * 退回订单表 服务类
 * </p>
 *
 * @author 
 * @since 2024-05-09
 */
public interface ICashBackOrderService extends IService<CashBackOrder> {
    CashBackOrder getCashBackOrder(String orderNo);

    CashBackOrder getProcessingCashBackOrderByMemberId(Long memberId);

    CashBackOrder getCashBackOrderByMemberIdAndOrderStatus(Long memberId, String orderStatus);

    boolean cashBack(String orderNo, String processUserName);

    boolean generateOrder(String thOrder, BigDecimal amount, String memberId, String merchantName, String merchantCode, String merchantMemberId);

    CashBackOrderApiDTO cashBackApi(String thOrder, String merchantMemberId, BigDecimal amount, String merchantCode, String merchantPublicKeyStr);

    PageReturn<CashBackOrderListPageDTO> listPage(CashBackOrderListPageReq req) throws ExecutionException, InterruptedException;
}
