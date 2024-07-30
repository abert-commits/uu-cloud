package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BankInfoDTO;
import org.uu.common.pay.req.*;
import org.uu.wallet.service.IBankInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 银行表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-06-07
 */
@RestController
@RequestMapping(value = {"/api/v1/bankInfo", "/bankInfo"})
@Validated
@ApiIgnore
public class BankInfoController {

    @Resource
    IBankInfoService bankInfoService;

    @PostMapping("/add")
    @ApiOperation(value = "新增")
    public RestResult<BankInfoDTO> add(@RequestBody @ApiParam @Valid BankInfoReq req)  {
        return bankInfoService.add(req);
    }

    @PostMapping("/detail")
    @ApiOperation(value = "详情")
    public RestResult<BankInfoDTO> detail(@RequestBody @ApiParam BankInfoIdReq req)  {
         return bankInfoService.detail(req);
    }

    @PostMapping("/listPage")
    @ApiOperation(value = "列表")
    public RestResult<List<BankInfoDTO>> listPage(@RequestBody @ApiParam BankInfoListPageReq req)  {
        PageReturn<BankInfoDTO> bankInfo = bankInfoService.listPage(req);
        return RestResult.page(bankInfo);
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新")
    public RestResult update(@RequestBody @ApiParam BankInfoUpdateReq req)  {
        return bankInfoService.update(req);
    }

    @PostMapping("/deleteInfo")
    @ApiOperation(value = "删除")
    public RestResult deleteInfo(@RequestBody @ApiParam BankInfoIdReq req)  {
        return bankInfoService.deleteInfo(req);
    }

    @PostMapping("/updateStatus")
    @ApiOperation(value = "启用/关闭")
    public RestResult updateStatus(@RequestBody @ApiParam BankInfoUpdateStatusReq req)  {
        return bankInfoService.updateStatus(req);
    }

    @PostMapping("/getBankCodeMap")
    @ApiOperation(value = "获取银行代码对应银行")
    public RestResult<Map<String, String>> getBankCodeMap()  {
        return bankInfoService.getBankCodeMap();
    }
}
