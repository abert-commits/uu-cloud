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
import org.uu.common.pay.dto.TronRentEnergyDTO;
import org.uu.common.pay.dto.TronRentEnergyExportDTO;
import org.uu.common.pay.dto.TronRentEnergyExportEnDTO;
import org.uu.common.pay.dto.UsdtBuyOrderExportDTO;
import org.uu.common.pay.req.TronRentEnergyReq;
import org.uu.manager.api.TronRentEnergyClient;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author afei
 */
@RestController
@RequiredArgsConstructor
@Api(description = "能量租用记录")
@RequestMapping(value = {"/api/v1/tronRentEnergy", "/tronRentEnergy"})
@Slf4j
public class TronRentEnergyController {
    @Resource
    TronRentEnergyClient tronRentEnergyClient;


    @PostMapping("/tronRentEnergyListPage")
    @ApiOperation(value = "能量租用记录分页列表")
    public RestResult<List<TronRentEnergyDTO>> tronRentEnergyListPage(@RequestBody @ApiParam TronRentEnergyReq req) {
        return tronRentEnergyClient.tronRentEnergyListPage(req);
    }

    @PostMapping("/tronRentEnergyExport")
    @ApiOperation(value = "能量租用记录导出")
    public void tronRentEnergyExport(HttpServletResponse response, @RequestBody @ApiParam TronRentEnergyReq req) throws IOException {
        req.setPageSize(GlobalConstants.BATCH_SIZE);
        RestResult<List<TronRentEnergyExportDTO>> result = tronRentEnergyClient.tronRentEnergyExport(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "EnergyRentalRecords";
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
                clazz = TronRentEnergyExportEnDTO.class;
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
                RestResult<List<TronRentEnergyExportDTO>> resultList = tronRentEnergyClient.tronRentEnergyExport(req);
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


}
