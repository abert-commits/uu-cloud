package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TradeConfigSchemeDTO;
import org.uu.common.pay.req.TradeConfigIdReq;
import org.uu.common.pay.req.TradeConfigSchemeListPageReq;
import org.uu.common.pay.req.TradeConfigSchemeReq;
import org.uu.wallet.service.ITradeConfigSchemeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * <p>
 * 交易配置方案表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
@RestController
@RequiredArgsConstructor
@Api("配置方案控制器")
@RequestMapping(value = {"/api/v1/tradeConfigScheme", "/tradeConfigScheme"})
@ApiIgnore
public class TradeConfigSchemeController {
    private final ITradeConfigSchemeService tradeConfigSchemeService;

    @PostMapping("/listPage")
    @ApiOperation(value = "获取配置方案列表")
    public RestResult<TradeConfigSchemeDTO> listPage(@RequestBody @ApiParam TradeConfigSchemeListPageReq req) {
        PageReturn<TradeConfigSchemeDTO> payConfigSchemePage = tradeConfigSchemeService.listPage(req);
        return RestResult.page(payConfigSchemePage);
    }

    @PostMapping("/detail")
    @ApiOperation(value = "查看配置方案")
    public RestResult<TradeConfigSchemeDTO> detail(@Validated @RequestBody TradeConfigIdReq req) {
        TradeConfigSchemeDTO tradeConfigSchemeDTO = tradeConfigSchemeService.getDetail(req.getId());
        return RestResult.ok(tradeConfigSchemeDTO);
    }

    @PostMapping("/updateScheme")
    @ApiOperation(value = "修改配置方案")
    public RestResult<TradeConfigSchemeDTO> updateScheme(@Validated @RequestBody TradeConfigSchemeReq req) {
        TradeConfigSchemeDTO tradeConfigSchemeDTO = tradeConfigSchemeService.updateScheme(req);
        return RestResult.ok(tradeConfigSchemeDTO);
    }

}
