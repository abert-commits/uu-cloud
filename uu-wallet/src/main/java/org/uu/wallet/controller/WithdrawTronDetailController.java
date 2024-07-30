package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.RechargeTronDetailDTO;
import org.uu.common.pay.dto.WithdrawTronDetailDTO;
import org.uu.common.pay.dto.WithdrawTronDetailExportDTO;
import org.uu.common.pay.req.WithdrawTronDetailReq;
import org.uu.wallet.service.IWithdrawTronDetailService;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 代付钱包交易记录 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-18
 */
@RestController
@RequestMapping("/withdraw-tron-detail")
public class WithdrawTronDetailController {
    @Resource
    private IWithdrawTronDetailService withdrawTronDetailService;

    @PostMapping("/withdrawTronDetailPage")
    @ApiOperation(value = "代付钱包交易记录分页列表")
    public RestResult<List<RechargeTronDetailDTO>> withdrawTronDetailPage(@RequestBody @ApiParam WithdrawTronDetailReq req) {
        PageReturn<WithdrawTronDetailDTO> pageReturn = withdrawTronDetailService.withdrawTronDetailPage(req);
        return RestResult.page(pageReturn);
    }

    @PostMapping("/withdrawTronDetailPageExport")
    @ApiOperation(value = "代付钱包交易记录分页列表导出")
    public RestResult<List<WithdrawTronDetailExportDTO>> withdrawTronDetailPageExport(@RequestBody @ApiParam WithdrawTronDetailReq req) {
        PageReturn<WithdrawTronDetailExportDTO> pageReturn = withdrawTronDetailService.withdrawTronDetailPageExport(req);
        return RestResult.page(pageReturn);
    }

}
