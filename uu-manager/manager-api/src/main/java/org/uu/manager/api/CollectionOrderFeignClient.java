package org.uu.manager.api;

import io.swagger.annotations.ApiParam;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CallBackDetailDTO;
import org.uu.common.pay.dto.CollectionOrderDTO;
import org.uu.common.pay.dto.CollectionOrderExportDTO;
import org.uu.common.pay.dto.CollectionOrderInfoDTO;
import org.uu.common.pay.req.*;
import org.uu.manager.dto.AccountChangeDTO;
import org.uu.manager.req.AccountChangeReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "collection-order")
public interface CollectionOrderFeignClient {




    @PostMapping("/api/v1/collectionOrder/listRecordPage")
    RestResult<List<CollectionOrderDTO>> listRecordPage(@RequestBody CollectionOrderListPageReq req);


    @PostMapping("/api/v1/collectionOrder/listPage")
    RestResult<List<CollectionOrderDTO>> listPage(@RequestBody CollectionOrderListPageReq req);

    @PostMapping("/api/v1/collectionOrder/listPageExport")
    RestResult<List<CollectionOrderExportDTO>> listPageExport(@RequestBody CollectionOrderListPageReq req);


    @PostMapping("/api/v1/collectionOrder/listPageRecordTotal")
    RestResult<CollectionOrderDTO> listPageRecordTotal(@RequestBody CollectionOrderListPageReq req);

    @PostMapping("/api/v1/collectionOrder/getInfo")
    RestResult<CollectionOrderInfoDTO> getInfo(@RequestBody CollectionOrderGetInfoReq req);
    @PostMapping("/api/v1/collectionOrder/pay")
    RestResult<CollectionOrderDTO> pay(@RequestBody CollectionOrderIdReq req);

    @PostMapping("/api/v1/collectionOrder/manualCallback")
    RestResult<Boolean> manualCallback(@RequestBody @ApiParam CollectionOrderIdReq collectionOrderReq);


    @PostMapping("/api/v1/collectionOrder/paid")
    KycRestResult paid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/collectionOrder/unPaid")
    KycRestResult unPaid(@RequestBody @ApiParam PaidParamReq req);

    @PostMapping("/api/v1/collectionOrder/callBackDetail")
    KycRestResult<CallBackDetailDTO> callBackDetail(@RequestBody @ApiParam CollectionOrderIdReq req);
}
