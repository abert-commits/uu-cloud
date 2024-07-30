package org.uu.manager.controller;


import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TradeIpBlackListPageDTO;
import org.uu.common.pay.req.TradeIpBlackListReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.TradeIpBlackFeignClient;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@Api(description = "交易黑名单控制器")
@RequestMapping("/tradeIpBlack")
public class TradeIpBlackController {

    private final TradeIpBlackFeignClient tradeIpBlackFeignClient;


    @PostMapping("/listPage")
    @ApiOperation(value = "黑名单分页列表")
    public RestResult<List<TradeIpBlackListPageDTO>> listPage(@RequestBody TradeIpBlackListReq req) {
        RestResult<List<TradeIpBlackListPageDTO>> result = tradeIpBlackFeignClient.listPage(req);
        return result;
    }

    @PostMapping("/save")
    @SysLog(title = "黑名单控制器", content = "新增")
    @ApiOperation(value = "保存")
    public RestResult save(@RequestBody TradeIpBlackListReq req) {
        TradeIpBlackListPageDTO sysWhite = new TradeIpBlackListPageDTO();
        BeanUtils.copyProperties(req, sysWhite);
        return tradeIpBlackFeignClient.save(req);
    }


    @PostMapping("/del")
    @SysLog(title = "黑名单控制器", content = "删除")
    @ApiOperation(value = "删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "行id", required = true, dataType = "String")
    })
    public RestResult del(@RequestParam(value = "id") String id) {
        return tradeIpBlackFeignClient.del(id);
    }
}
