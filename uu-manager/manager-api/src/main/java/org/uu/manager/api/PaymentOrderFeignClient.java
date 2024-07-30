package org.uu.manager.api;

import io.swagger.annotations.ApiParam;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "payment-order")
public interface PaymentOrderFeignClient {




    @PostMapping("/api/v1/paymentOrder/listRecordPage")
    RestResult<List<PaymentOrderListPageDTO>> listRecordPage(@RequestBody PaymentOrderListPageReq req);


    @PostMapping("/api/v1/paymentOrder/listPage")
    RestResult<List<PaymentOrderListPageDTO>> listPage(@RequestBody PaymentOrderListPageReq req);

    @PostMapping("/api/v1/paymentOrder/listPageExport")
    RestResult<List<PaymentOrderExportDTO>> listPageExport(@RequestBody PaymentOrderListPageReq req);


    @PostMapping("/api/v1/paymentOrder/listRecordTotalPage")
    RestResult<PaymentOrderListPageDTO> listRecordTotalPage(@RequestBody PaymentOrderListPageReq req);

    @PostMapping("/api/v1/paymentOrder/getInfo")
    RestResult<PaymentOrderInfoDTO> getInfo(@RequestBody PaymentOrderGetInfoReq req);
    @PostMapping("/api/v1/paymentOrder/cancel")
    RestResult<PaymentOrderListPageDTO> cancel(@RequestBody PaymentOrderIdReq req);

    @PostMapping("/api/v1/paymentOrder/manualCallback")
    RestResult<Boolean> manualCallback(@RequestBody @ApiParam PaymentOrderIdReq req);


    @PostMapping("/api/v1/paymentOrder/paid")
    KycRestResult paid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/paymentOrder/unPaid")
    KycRestResult unPaid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/paymentOrder/callBackDetail")
    KycRestResult<CallBackDetailDTO> callBackDetail(@RequestBody @ApiParam PaymentOrderIdReq req);
}
