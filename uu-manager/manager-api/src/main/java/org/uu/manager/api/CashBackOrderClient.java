package org.uu.manager.api;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.CashBackOrderListPageDTO;
import org.uu.common.pay.req.CashBackOrderListPageReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "cash-back-order")
public interface CashBackOrderClient {

    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/cashBackOrder/listPage")
    PageReturn<CashBackOrderListPageDTO> listPage(@RequestBody CashBackOrderListPageReq req);
}
