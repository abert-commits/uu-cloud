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
import org.uu.common.pay.dto.RechargeTronExportDTO;
import org.uu.common.pay.req.RechargeTronDetailReq;
import org.uu.wallet.service.IRechargeTronDetailService;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 代收钱包交易记录 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@RestController
@RequestMapping("/recharge-tron-detail")
public class RechargeTronDetailController {

    @Resource
    private IRechargeTronDetailService rechargeTronDetailService;

    @PostMapping("/rechargeTronDetailPage")
    @ApiOperation(value = "代收钱包交易记录分页列表")
    public RestResult<List<RechargeTronDetailDTO>> rechargeTronDetailPage(@RequestBody @ApiParam RechargeTronDetailReq req) {
        PageReturn<RechargeTronDetailDTO> pageReturn = rechargeTronDetailService.rechargeTronDetailPage(req);
        return RestResult.page(pageReturn);
    }

    @PostMapping("/rechargeTronDetailPageExport")
    @ApiOperation(value = "代收钱包交易记录分页列表导出")
    public RestResult<List<RechargeTronExportDTO>> rechargeTronDetailPageExport(@RequestBody @ApiParam RechargeTronDetailReq req) {
        PageReturn<RechargeTronExportDTO> pageReturn = rechargeTronDetailService.rechargeTronDetailPageExport(req);
        return RestResult.page(pageReturn);
    }
}
