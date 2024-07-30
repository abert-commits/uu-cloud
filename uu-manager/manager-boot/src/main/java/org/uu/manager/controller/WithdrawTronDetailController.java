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
import org.uu.common.pay.dto.WithdrawTronDetailDTO;
import org.uu.common.pay.dto.WithdrawTronDetailExportDTO;
import org.uu.common.pay.dto.WithdrawTronDetailExportEnDTO;
import org.uu.common.pay.req.WithdrawTronDetailReq;
import org.uu.manager.api.WithdrawTronDetailClient;

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
@Api(description = "代付钱包交易记录")
@RequestMapping(value = {"/withdrawTronDetail", "/withdrawTronDetail"})
@Slf4j
public class WithdrawTronDetailController {
    @Resource
    WithdrawTronDetailClient withdrawTronDetailClient;


    @PostMapping("/withdrawTronDetailPage")
    @ApiOperation(value = "代付钱包交易记录分页列表")
    public RestResult<List<WithdrawTronDetailDTO>> withdrawTronDetailPage(@RequestBody @ApiParam WithdrawTronDetailReq req) {
        return withdrawTronDetailClient.withdrawTronDetailPage(req);
    }


    @PostMapping("/withdrawTronDetailPageExport")
    @ApiOperation(value = "代付钱包交易记录分页列表导出")
    public void withdrawTronDetailPageExport(HttpServletResponse response, @RequestBody @ApiParam WithdrawTronDetailReq req) throws IOException {
        RestResult<List<WithdrawTronDetailExportDTO>> result = withdrawTronDetailClient.withdrawTronDetailPageExport(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "withdrawTronRecords";
        String sheetName = "sheet1";
        try {
            outputStream = response.getOutputStream();
            exportTotalSize = result.getTotal();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = "zh".equals(req.getLang()) ? WithdrawTronDetailExportDTO.class : WithdrawTronDetailExportEnDTO.class;
            ExcelUtil.setResponseHeader(response, fileName);
            excelWriter = EasyExcel.write(bos, clazz).build();
            head = ExcelUtil.parseHead(clazz);

            WriteSheet testSheet = EasyExcel.writerSheet(sheetName)
                    .head(head)
                    .build();
            excelWriter.write(result.getData(), testSheet);

            // 分批次导出数据
            long pageNo = 1;
            long totalSize = ExcelUtil.getTotalSize(result.getTotal());
            for (int i = 0; i < totalSize; i++) {
                pageNo++;
                req.setPageNo(pageNo);
                req.setPageSize(GlobalConstants.BATCH_SIZE);
                exportTotalSize = exportTotalSize + result.getData().size();
                if (exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE) {
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet(sheetName)
                        .build();
                result = withdrawTronDetailClient.withdrawTronDetailPageExport(req);// 获取下一页数据
                excelWriter.write(result.getData(), testSheet1);
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
