package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "matching-order")
public interface MatchingOrderClient {


    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/matchingOrder/listpage")
    RestResult<List<MatchingOrderPageListDTO>> listpage(@RequestBody MatchingOrderReq req);

    @PostMapping("/api/v1/matchingOrder/listpageExport")
    RestResult<List<MatchingOrderExportDTO>> listpageExport(@RequestBody MatchingOrderReq req);

    /**
     *
     * @param req
     * @return
     */
    @PostMapping("/api/v1/matchingOrder/update")
    RestResult<MatchingOrderDTO> update(@RequestBody MatchingOrderReq req);


    /**
     * 详情
     * @param
     * @param
     * @return
     */
    @PostMapping("/api/v1/matchingOrder/appealDetail")
    RestResult<MatchingOrderVoucherDTO> appealDetail(@RequestBody MatchingOrderIdReq req);


    @PostMapping("/api/v1/matchingOrder/getInfo")
    RestResult<MatchingOrderInfoDTO> getInfo(@RequestBody MatchingOrderIdReq req);


    @PostMapping("/api/v1/matchingOrder/getMatchingOrderTotal")
    RestResult<MatchingOrderDTO> getMatchingOrderTotal(@RequestBody MatchingOrderReq req);


    @PostMapping("/api/v1/matchingOrder/appealSuccess")
    RestResult<MatchingOrderDTO> appealSuccess(@RequestBody MatchingOrderIdReq req);

    @PostMapping("/api/v1/matchingOrder/appealFailure")
    RestResult<MatchingOrderDTO> appealFailure(@RequestBody MatchingOrderIdReq req);

    @PostMapping("/api/v1/matchingOrder/pay")
    RestResult<MatchingOrderDTO> pay(@RequestBody MatchingOrderAppealReq req);

    @PostMapping("/api/v1/matchingOrder/nopay")
    RestResult<MatchingOrderDTO> nopay(@RequestBody MatchingOrderAppealReq req);

    @PostMapping("/api/v1/matchingOrder/incorrectVoucher")
    RestResult<MatchingOrderVoucherDTO> incorrectVoucher(@RequestBody MatchingOrderIdReq req);

    @PostMapping("/api/v1/matchingOrder/pscheck")
    RestResult<MatchingOrderVoucherUrlDTO> pscheck(@RequestBody MatchingOrderIdReq req);

    @PostMapping("/api/v1/matchingOrder/manualReview")
    RestResult manualReview(@RequestBody MatchingOrderManualReq req);


    @PostMapping("/api/v1/matchingOrder/incorrectTransfer")
    RestResult<MatchingOrderDTO> incorrectTransfer(@RequestBody MatchingOrderAppealReq req);

    @PostMapping("/api/v1/matchingOrder/export")
    void export(@RequestBody MatchingOrderReq matchingOrderReq);

    @PostMapping("/api/v1/matchingOrder/relationOrderList")
    RestResult<List<RelationOrderDTO>> relationOrderList(RelationshipOrderReq matchingOrderReq);
}
