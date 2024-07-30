package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;

import org.uu.common.pay.req.AccountChangeReq;
import org.uu.wallet.service.IAccountChangeService;
import org.uu.wallet.vo.AccountChangeVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author
 */
@RestController
@RequestMapping("/accountChange")
@ApiIgnore
public class AccountChangeController {

    @Resource
    IAccountChangeService iAccountChangeService;

    @PostMapping("/query")
    @ApiOperation(value = "查询商户账变记录")
    public RestResult<List<AccountChangeVo>> listPage(@Validated @RequestBody AccountChangeReq accountChangeReq) {
        PageReturn<AccountChangeVo> result = iAccountChangeService.queryAccountChangeList(accountChangeReq);
        return RestResult.page(result);
    }

    @PostMapping("/fetchAccountType")
    @ApiOperation(value = "获取账变类型")
    public RestResult fetchAccountType() {

        Map<Integer, String> map =  iAccountChangeService.fetchAccountType();
        return RestResult.ok(map);

    }

    @PostMapping("/queryTotal")
    @ApiOperation(value = "账变汇总")
    public RestResult<AccountChangeVo> queryTotal(@Validated @RequestBody AccountChangeReq accountChangeReq) {
        AccountChangeVo result = iAccountChangeService.queryTotal(accountChangeReq);
        return RestResult.ok(result);
    }


}
