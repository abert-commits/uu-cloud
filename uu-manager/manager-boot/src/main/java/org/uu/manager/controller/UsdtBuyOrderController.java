package org.uu.manager.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.ExcelUtil;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.UsdtBuyOrderGetInfoReq;
import org.uu.common.pay.req.UsdtBuyOrderIdReq;
import org.uu.common.pay.req.UsdtBuyOrderReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.UsdtBuyOrderFeignClient;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "usdt买入订单")
@RequestMapping(value = {"/api/v1/usdtBuyOrderAdmin", "/usdtBuyOrderAdmin"})
public class UsdtBuyOrderController {
    private final UsdtBuyOrderFeignClient usdtBuyOrderFeignClient;

    @PostMapping("/listpage")
    @ApiOperation(value = "usdt订单买入列表")
    public RestResult<List<UsdtBuyOrderDTO>> listpage(@RequestBody @ApiParam UsdtBuyOrderReq req) {
        RestResult<List<UsdtBuyOrderDTO>> result = usdtBuyOrderFeignClient.listpage(req);
        return result;
    }

    @PostMapping("/listpageExport")
    @ApiOperation(value = "usdt订单买入列表")
    public void listpageExport(HttpServletResponse response, @RequestBody @ApiParam UsdtBuyOrderReq req) throws IOException {
        req.setPageSize(GlobalConstants.BATCH_SIZE);
        RestResult<List<UsdtBuyOrderExportDTO>> result = usdtBuyOrderFeignClient.listpageForExport(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "UsdtBuyOrder";
        String sheetName = "sheet1";
        try {
            outputStream = response.getOutputStream();
            exportTotalSize = result.getData().size();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = UsdtBuyOrderExportDTO.class;
            ExcelUtil.setResponseHeader(response, fileName);
            if (!"zh".equals(req.getLang())) {
                clazz = UsdtBuyOrderExportEnDTO.class;
            }
            excelWriter = EasyExcel.write(bos, clazz).build();
            head = ExcelUtil.parseHead(clazz);
            WriteSheet testSheet = EasyExcel.writerSheet(sheetName)
                    .head(head)
                    .build();
            excelWriter.write(result.getData(), testSheet);
            // 写入数据
            long pageNo = 1;
            long totalSize = ExcelUtil.getTotalSize(result.getTotal());
            for (int i = 0; i < totalSize; i++) {
                pageNo++;
                req.setPageNo(pageNo);
                req.setPageSize(GlobalConstants.BATCH_SIZE);
                RestResult<List<UsdtBuyOrderExportDTO>> resultList = usdtBuyOrderFeignClient.listpageForExport(req);
                exportTotalSize = exportTotalSize + resultList.getData().size();
                if (exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE) {
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet(sheetName)
                        .build();
                excelWriter.write(resultList.getData(), testSheet1);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            assert bos != null;
            bos.flush();
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "查看")
    public RestResult<UsdtBuyOrderInfoDTO> getInfo(@RequestBody @ApiParam UsdtBuyOrderGetInfoReq req) {

        RestResult<UsdtBuyOrderInfoDTO> result = usdtBuyOrderFeignClient.getInfo(req);
        return result;
    }


    @PostMapping("/pay")
    @SysLog(title = "usdt买入订单", content = "支付")
    @ApiOperation(value = "支付")
    public RestResult<UsdtBuyOrderDTO> pay(@RequestBody @ApiParam UsdtBuyOrderIdReq req) {
        RestResult<UsdtBuyOrderDTO> result = usdtBuyOrderFeignClient.pay(req);

        return result;
    }

    @PostMapping("/nopay")
    @SysLog(title = "usdt买入订单", content = "未支付")
    @ApiOperation(value = "未支付")
    public RestResult<UsdtBuyOrderDTO> nopay(@RequestBody @ApiParam UsdtBuyOrderIdReq req) {
        RestResult<UsdtBuyOrderDTO> result = usdtBuyOrderFeignClient.nopay(req);
        return result;
    }

    @PostMapping("/successOrderListPage")
    @ApiOperation(value = "usdt交易成功订单")
    public RestResult<List<UsdtBuySuccessOrderDTO>> successOrderListPage(@RequestBody @ApiParam UsdtBuyOrderReq req) {
        return usdtBuyOrderFeignClient.successOrderListPage(req);
    }

    @PostMapping("/merchantSuccessOrderPage")
    @ApiOperation(value = "商户会员usdt交易成功订单")
    public RestResult<List<UsdtBuySuccessOrderDTO>> merchantSuccessOrderPage(@RequestBody @ApiParam UsdtBuyOrderReq req) {
        return usdtBuyOrderFeignClient.merchantSuccessOrderPage(req);
    }

}
