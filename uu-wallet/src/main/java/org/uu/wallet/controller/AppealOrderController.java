package org.uu.wallet.controller;


import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.SneakyThrows;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AppealOrderDTO;
import org.uu.common.pay.dto.AppealOrderExportDTO;
import org.uu.common.pay.dto.ApplyDistributedDTO;
import org.uu.common.pay.req.AppealOrderIdReq;
import org.uu.common.pay.req.AppealOrderPageListReq;
import org.uu.common.pay.req.ApplyDistributedListPageReq;
import org.uu.wallet.entity.AppealOrder;
import org.uu.wallet.req.AccountChangeReq;
import org.uu.wallet.service.IAppealOrderService;
import org.uu.wallet.vo.AccountChangeVo;
import org.uu.wallet.vo.AppealOrderVo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 申诉控制器
 * @author
 */
@RestController
@RequestMapping(value = {"/api/v1/appealOrder", "/appealOrder"})
@ApiIgnore
public class AppealOrderController {

    @Resource
    IAppealOrderService iAppealOrderService;

    @PostMapping("/query")
    @ApiOperation(value = "查询申诉详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "orderNo", value = "订单号", required = true, dataType = "String"),
            @ApiImplicitParam(name = "appealType", value = "申诉类型: 1-提现申诉 2-充值申诉", required = true, dataType = "Integer"),
    })
    public RestResult<AppealOrderVo> queryAppealOrder(String orderNo, Integer appealType) throws Exception {
        AppealOrderVo result = iAppealOrderService.queryAppealOrder(orderNo, appealType);
        return RestResult.ok(result);
    }



    @PostMapping("/pay")
    @ApiOperation(value = "已支付")
    public RestResult<AppealOrderDTO> pay(@RequestBody @ApiParam AppealOrderIdReq req)  {
        AppealOrderDTO appealOrderDTO = iAppealOrderService.pay(req);
        return RestResult.ok(appealOrderDTO);
    }


    @PostMapping("/nopay")
    @ApiOperation(value = "未支付")
    public RestResult<AppealOrderDTO> nopay(@RequestBody @ApiParam AppealOrderIdReq req)  {
        AppealOrderDTO appealOrderDTO= iAppealOrderService.nopay(req);
        return RestResult.ok(appealOrderDTO);
    }

    @SneakyThrows
    @PostMapping("/listpage")
    @ApiOperation(value = "申诉列表")
    public RestResult<List<AppealOrderDTO>> listpage(@RequestBody @ApiParam AppealOrderPageListReq req) {
        PageReturn<AppealOrderDTO> payConfigPage = iAppealOrderService.listPage(req);
        return RestResult.page(payConfigPage);
    }

    @SneakyThrows
    @PostMapping("/listpageExport")
    @ApiOperation(value = "申诉列表导出")
    public RestResult<List<AppealOrderExportDTO>> listpageExport(@RequestBody @ApiParam AppealOrderPageListReq req) {
        PageReturn<AppealOrderExportDTO> payConfigPage = iAppealOrderService.listPageExport(req);
        return RestResult.page(payConfigPage);
    }
}
