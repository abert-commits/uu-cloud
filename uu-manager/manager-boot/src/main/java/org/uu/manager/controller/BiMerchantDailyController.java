package org.uu.manager.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.ExcelUtil;
import org.uu.common.pay.dto.BiMerchantDailyDTO;
import org.uu.common.pay.dto.BiMerchantDailyEnDTO;
import org.uu.manager.entity.BiMerchantDaily;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.service.IBiMerchantDailyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 商户日报表
 * @author
 */
@Slf4j
@RestController
@Api(description = "商户日报表控制器")
@RequestMapping(value = {"/api/v1/biMerchantDaily", "/biMerchantDaily"})
public class BiMerchantDailyController {
    @Resource
    IBiMerchantDailyService iBiMerchantDailyService;

    @PostMapping("/query")
    @ApiOperation(value = "查询商户日报表记录")
    public RestResult<List<BiMerchantDaily>> listPage(@Validated @RequestBody MerchantDailyReportReq req) {

        PageReturn<BiMerchantDaily> result = iBiMerchantDailyService.listPage(req);
        return RestResult.page(result);
    }

    @PostMapping("/export")
    @ApiOperation(value = "商户报表导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam MerchantDailyReportReq req) throws IOException {
        req.setPageSize(GlobalConstants.BATCH_SIZE);
        PageReturn<BiMerchantDailyDTO> result = iBiMerchantDailyService.listPageForExport(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "BiMerchantRecords";
        String sheetName = "sheet1";
        try{
            outputStream = response.getOutputStream();
            exportTotalSize = result.getList().size();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = BiMerchantDailyDTO.class;
            ExcelUtil.setResponseHeader(response, fileName);
            if (!"zh".equals(req.getLang())) {
                clazz = BiMerchantDailyEnDTO.class;
            }
            excelWriter = EasyExcel.write(bos, clazz).build();
            head = ExcelUtil.parseHead(clazz);
            WriteSheet testSheet = EasyExcel.writerSheet(sheetName)
                    .head(head)
                    .build();
            excelWriter.write(result.getList(), testSheet);
            // 写入数据
            long pageNo = 1;
            long totalSize = ExcelUtil.getTotalSize(result.getTotal());
            for (int i = 0; i < totalSize; i++) {
                pageNo++;
                req.setPageNo(pageNo);
                req.setPageSize(GlobalConstants.BATCH_SIZE);
                PageReturn<BiMerchantDailyDTO> resultList = iBiMerchantDailyService.listPageForExport(req);
                exportTotalSize = exportTotalSize + resultList.getList().size();
                if(exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE){
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet(sheetName)
                        .build();
                excelWriter.write(resultList.getList(), testSheet1);
            }
        }catch (Exception e) {
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
