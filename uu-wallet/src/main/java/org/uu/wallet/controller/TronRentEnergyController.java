package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TronRentEnergyDTO;
import org.uu.common.pay.dto.TronRentEnergyExportDTO;
import org.uu.common.pay.req.TronRentEnergyReq;
import org.uu.wallet.service.ITronRentEnergyService;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 能量租用记录表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-13
 */
@RestController
@RequestMapping("/tron-rent-energy")
public class TronRentEnergyController {
    @Resource
    private ITronRentEnergyService tronRentEnergyService;

    @PostMapping("/tronRentEnergyListPage")
    @ApiOperation(value = "能量租用记录分页列表")
    public RestResult<List<TronRentEnergyDTO>> tronRentEnergyListPage(@RequestBody @ApiParam TronRentEnergyReq req) {
        PageReturn<TronRentEnergyDTO> tronRentEnergyDTOPageReturn = tronRentEnergyService.tronRentEnergyListPage(req);
        return RestResult.page(tronRentEnergyDTOPageReturn);
    }

    @PostMapping("/tronRentEnergyExport")
    @ApiOperation(value = "能量租用记录导出")
    public RestResult<List<TronRentEnergyExportDTO>> tronRentEnergyExport(@RequestBody @ApiParam TronRentEnergyReq req) {
        PageReturn<TronRentEnergyExportDTO> tronRentEnergyDTOPageReturn = tronRentEnergyService.tronRentEnergyExport(req);
        return RestResult.page(tronRentEnergyDTOPageReturn);
    }
}
