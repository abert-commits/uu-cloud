package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BankInfoDTO;
import org.uu.common.pay.req.*;
import org.uu.manager.BankInfoFeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 银行表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-06-07
 */
@RestController
@RequestMapping(value = {"/api/v1/bankInfoAdmin", "/bankInfoAdmin"})
@Validated
@Api(description = "银行表控制器")
public class BankInfoController {

    @Resource
    BankInfoFeignClient bankInfoFeignClient;

    @PostMapping("/add")
    @ApiOperation(value = "新增")
    public RestResult<BankInfoDTO> add(@RequestBody @ApiParam BankInfoReq req)  {
        return bankInfoFeignClient.add(req);
    }

    @PostMapping("/detail")
    @ApiOperation(value = "详情")
    public RestResult<BankInfoDTO> detail(@RequestBody @ApiParam BankInfoIdReq req)  {
         return bankInfoFeignClient.detail(req);
    }

    @PostMapping("/listPage")
    @ApiOperation(value = "列表")
    public RestResult<List<BankInfoDTO>> listPage(@RequestBody @ApiParam @Valid BankInfoListPageReq req)  {
        return bankInfoFeignClient.listPage(req);
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新")
    public RestResult update(@RequestBody @ApiParam BankInfoUpdateReq req)  {
        return bankInfoFeignClient.update(req);
    }

    @PostMapping("/deleteInfo")
    @ApiOperation(value = "删除")
    public RestResult deleteInfo(@RequestBody @ApiParam BankInfoIdReq req)  {
        return bankInfoFeignClient.deleteInfo(req);
    }

    @PostMapping("/getBankCodeMap")
    @ApiOperation(value = "获取银行代码对应银行")
    public RestResult<Map<String, String>> getBankCodeMap()  {
        return bankInfoFeignClient.getBankCodeMap();
    }
}
