package org.uu.manager.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.ExcelUtil;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.AppealOrderIdReq;
import org.uu.common.pay.req.AppealOrderPageListReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.AppealOrderFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * 申诉控制器
 * @author
 */
@Slf4j
@RestController
@Api(description = "申诉控制器")
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/appealOrderAdmin", "/appealOrderAdmin"})
public class AppealOrderController {

    private final AppealOrderFeignClient appealOrderFeignClient;



    @PostMapping("/pay")
    @ApiOperation(value = "已支付")
    @SysLog(title="申诉控制器",content = "已支付")
    public RestResult<AppealOrderDTO> pay(@RequestBody @ApiParam AppealOrderIdReq req)  {
        RestResult<AppealOrderDTO> result = appealOrderFeignClient.pay(req);
        return result;
    }


    @PostMapping("/nopay")
    @ApiOperation(value = "未支付")
    @SysLog(title="申诉控制器",content = "未支付")
    public RestResult<AppealOrderDTO> nopay(@RequestBody @ApiParam AppealOrderIdReq req)  {
        RestResult<AppealOrderDTO> result = appealOrderFeignClient.nopay(req);
        return result;
    }


    @PostMapping("/listpage")
    @ApiOperation(value = "列表")
    public RestResult<List<AppealOrderDTO>> listpage(@RequestBody @ApiParam AppealOrderPageListReq req)  {
        RestResult<List<AppealOrderDTO>> result = appealOrderFeignClient.listpage(req);
        return result;
    }

    @PostMapping("/export")
    @ApiOperation(value = "申诉列表导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam AppealOrderPageListReq req) throws IOException {
        req.setPageSize(GlobalConstants.BATCH_SIZE);
        RestResult<List<AppealOrderExportDTO>> result = appealOrderFeignClient.listpageExport(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "AppealOrderRecords";
        String sheetName = "sheet1";
        try{
            outputStream = response.getOutputStream();
            exportTotalSize = result.getData().size();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = AppealOrderExportDTO.class;
            ExcelUtil.setResponseHeader(response, fileName);
            if (!"zh".equals(req.getLang())) {
                clazz = AppealEnOrderDTO.class;
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
                RestResult<List<AppealOrderExportDTO>> resultList = appealOrderFeignClient.listpageExport(req);
                exportTotalSize = exportTotalSize + resultList.getData().size();
                if(exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE){
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet(sheetName)
                        .build();
                excelWriter.write(resultList.getData(), testSheet1);
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
