package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CreditScoreLogsDTO;
import org.uu.common.pay.req.CreditScoreLogsListPageReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author admin
 * @date 2024/4/9 15:21
 */
@FeignClient(value = "uu-wallet", contextId = "credit-score-logs")
public interface CreditScoreLogsClient {

    @PostMapping("/api/v1/creditScoreLogs/listPage")
    RestResult<List<CreditScoreLogsDTO>> listPage(@RequestBody CreditScoreLogsListPageReq req);

}
