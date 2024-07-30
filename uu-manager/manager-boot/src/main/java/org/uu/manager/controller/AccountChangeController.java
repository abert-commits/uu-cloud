package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.req.AccountChangeReq;
import org.uu.manager.api.AccountChangeFeignClient;
import org.uu.manager.dto.AccountChangeDTO;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author
 */
@RestController
@Api(description = "商户账变")
@RequestMapping(value = {"/api/v1/accountChangeAdmin", "/accountChangeAdmin"})
public class AccountChangeController {

    @Resource
    AccountChangeFeignClient accountChangeFeignClient;

    @PostMapping("/query")
    @ApiOperation(value = "商户账变列表")
    public RestResult<List<AccountChangeDTO>> listPage(@RequestBody @ApiParam AccountChangeReq accountChangeReq) {
        return accountChangeFeignClient.listPage(accountChangeReq);
    }


    @PostMapping("/queryTotal")
    @ApiOperation(value = "商户账变总订单合计")
    public RestResult<AccountChangeDTO> queryTotal(@RequestBody @ApiParam AccountChangeReq accountChangeReq) {
        RestResult<AccountChangeDTO> result = accountChangeFeignClient.queryTotal(accountChangeReq);
        return result;
    }


    @PostMapping("/fetchAccountType")
    @ApiOperation(value = "获取账变类型")
    public RestResult orderCallbackStatus() {

        RestResult<Map<Integer, String>> map = accountChangeFeignClient.fetchAccountType();
        return map;

    }
}
