package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "trade-config")
public interface TradeConfigClient {


    /**
     * @param
     * @return
     */
    @PostMapping("/api/v1/tradeConfig/listpage")
    RestResult<List<TradeConfigDTO>> listpage(@RequestBody TradeConfigListPageReq req);

    /**
     * @param req
     * @return
     */
    @PostMapping("/api/v1/tradeConfig/updateBuy")
    RestResult<TradeConfigBuyDTO> updateBuy(@RequestBody TradeConfigBuyReq req);


    @PostMapping("/api/v1/tradeConfig/updateSell")
    RestResult<TradeConfigSellDTO> updateSell(@RequestBody TradeConfigSellReq req);

    @PostMapping("/api/v1/tradeConfig/updateVoiceEnable")
    RestResult<TradeConfigVoiceEnableDTO> updateVoiceEnable(@RequestBody TradeConfigVoiceEnableReq req);


    /**
     * 详情
     *
     * @param
     * @param
     * @return
     */
    @PostMapping("/api/v1/tradeConfig/detaill")
    RestResult<TradeConfigDTO> detaill(@RequestBody TradeConfigIdReq req);


    @PostMapping("/api/v1/tradeConfig/delete")
    RestResult delete(@RequestBody TradeConfigIdReq req);

    @PostMapping("/api/v1/tradeConfig/warningConfigDetail")
    RestResult<TradeWarningConfigDTO> warningConfigDetail(@RequestBody TradeConfigIdReq req);

    @PostMapping("/api/v1/tradeConfig/updateWarningConfig")
    RestResult<TradeWarningConfigDTO> updateWarningConfig(@RequestBody TradeConfigWarningConfigUpdateReq req);

    @PostMapping("/api/v1/tradeConfig/bankConfigDetail")
    RestResult<TradeBankConfigDTO> bankConfigDetail(@Validated @RequestBody TradeConfigIdReq req);

    @PostMapping("/api/v1/tradeConfig/updateBankCardConfig")
    RestResult<TradeBankConfigDTO> updateBankCardConfig(@Validated @RequestBody TradeBankConfigUpdateReq req);

    @GetMapping("/api/v1/tradeConfig/dividendConfigList")
    RestResult<List<DividendConfigDTO>> dividendConfigList();

    @PostMapping("/api/v1/tradeConfig/addDividendConfig")
    RestResult addDividendConfig(@RequestBody DividendConfigReq req);

    @PostMapping("/api/v1/tradeConfig/updateDividendConfig/{id}")
    RestResult updateDividendConfig(@PathVariable("id") Long id, @RequestBody DividendConfigReq req);

    @PostMapping("/api/v1/tradeConfig/updateTradeConfig")
    RestResult updateTradeConfig(@RequestBody TradeConfigUpdateReq req);
}
