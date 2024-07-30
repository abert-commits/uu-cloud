package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UsdtBuyOrderDTO;
import org.uu.common.pay.dto.UsdtBuyOrderExportDTO;
import org.uu.common.pay.dto.UsdtBuyOrderInfoDTO;
import org.uu.common.pay.dto.UsdtBuySuccessOrderDTO;
import org.uu.common.pay.req.UsdtBuyOrderGetInfoReq;
import org.uu.common.pay.req.UsdtBuyOrderIdReq;
import org.uu.common.pay.req.UsdtBuyOrderReq;

import java.util.List;

@FeignClient(value = "uu-wallet", contextId = "usdt-buy")
public interface UsdtBuyOrderFeignClient {

    @PostMapping("/api/v1/usdtBuyOrder/listpage")
    RestResult<List<UsdtBuyOrderDTO>> listpage(@RequestBody UsdtBuyOrderReq req);

    @PostMapping("/api/v1/usdtBuyOrder/listpageForExport")
    RestResult<List<UsdtBuyOrderExportDTO>> listpageForExport(@RequestBody UsdtBuyOrderReq req);

    @PostMapping("/api/v1/usdtBuyOrder/getInfo")
    RestResult<UsdtBuyOrderInfoDTO> getInfo(@RequestBody UsdtBuyOrderGetInfoReq req);

    @PostMapping("/api/v1/usdtBuyOrder/pay")
    RestResult<UsdtBuyOrderDTO> pay(@RequestBody UsdtBuyOrderIdReq req);

    @PostMapping("/api/v1/usdtBuyOrder/nopay")
    RestResult<UsdtBuyOrderDTO> nopay(@RequestBody UsdtBuyOrderIdReq req);

    @PostMapping("/api/v1/usdtBuyOrder/successOrderListPage")
    RestResult<List<UsdtBuySuccessOrderDTO>> successOrderListPage(@RequestBody UsdtBuyOrderReq req);

    @PostMapping("/merchant-collect-orders/merchantSuccessOrderPage")
    RestResult<List<UsdtBuySuccessOrderDTO>> merchantSuccessOrderPage(@RequestBody UsdtBuyOrderReq req);
}
