package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberLoginLogsDTO;
import org.uu.common.pay.dto.MemberOperationLogsDTO;
import org.uu.common.pay.dto.TradeIpBlackListPageDTO;
import org.uu.common.pay.dto.UserVerificationCodeslistPageDTO;
import org.uu.common.pay.req.MemberLoginLogsReq;
import org.uu.common.pay.req.MemberOperationLogsReq;
import org.uu.common.pay.req.TradeIpBlackListReq;
import org.uu.common.pay.req.UserTextMessageReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "tradeIpBlack")
public interface TradeIpBlackFeignClient {

    @PostMapping("/api/v1/tradeIpBlacklist/listPage")
    RestResult<List<TradeIpBlackListPageDTO>> listPage(@RequestBody TradeIpBlackListReq req);
    @PostMapping("/api/v1/tradeIpBlacklist/del")
    RestResult del(@RequestParam(value = "id") String id);
    @PostMapping("/api/v1/tradeIpBlacklist/save")
    RestResult save(TradeIpBlackListReq req);
}
