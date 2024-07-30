package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MatchPoolDTO;
import org.uu.common.pay.dto.MatchPoolListPageDTO;
import org.uu.common.pay.dto.PaymentOrderChildDTO;
import org.uu.common.pay.dto.PaymentOrderDTO;
import org.uu.common.pay.req.MatchPoolGetChildReq;
import org.uu.common.pay.req.MatchPoolListPageReq;
import org.uu.common.pay.req.MatchPoolReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "match-pool")
public interface MatchPoolClient {


    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/matchPool/listpage")
    RestResult<List<MatchPoolListPageDTO>> listpage(@RequestBody MatchPoolListPageReq req);

    /**
     *
     * @param req
     * @return
     */
    @PostMapping("/api/v1/matchPool/matchPooTotal")
    RestResult<MatchPoolListPageDTO> matchPooTotal(@RequestBody MatchPoolListPageReq req);


    /**
     * 详情
     * @param
     * @param
     * @return
     */
    @PostMapping("/api/v1/matchPool/getChildren")
    RestResult<List<PaymentOrderChildDTO>> getChildren(@RequestBody MatchPoolGetChildReq req);





}
