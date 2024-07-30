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
import org.uu.common.pay.dto.CollectionOrderRecordDTO;
import org.uu.common.pay.dto.CollectionOrderRecordExportDTO;
import org.uu.common.pay.dto.CollectionOrderRecordExportEnDTO;
import org.uu.common.pay.req.CollectionOrderRecordReq;
import org.uu.manager.api.CollectionOrderRecordClient;

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
@Api(description = "归集订单记录")
@RequestMapping(value = {"/collectionOrderRecord", "/collectionOrderRecord"})
@Slf4j
public class CollectionOrderRecordController {
    @Resource
    CollectionOrderRecordClient collectionOrderRecordClient;


    @PostMapping("/collectionOrderRecordPage")
    @ApiOperation(value = "归集订单记录分页列表")
    public RestResult<List<CollectionOrderRecordDTO>> collectionOrderRecordPage(@RequestBody @ApiParam CollectionOrderRecordReq req) {
        return collectionOrderRecordClient.collectionOrderRecordPage(req);
    }

    @PostMapping("/collectionOrderRecordPageExport")
    @ApiOperation(value = "归集订单记录导出")
    public void collectionOrderRecordPageExport(HttpServletResponse response, @RequestBody @ApiParam CollectionOrderRecordReq req) throws IOException {
        RestResult<List<CollectionOrderRecordExportDTO>> result = collectionOrderRecordClient.collectionOrderRecordPageExport(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "collectionOrderRecords";
        String sheetName = "sheet1";
        try {
            outputStream = response.getOutputStream();
            exportTotalSize = result.getTotal();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = "zh".equals(req.getLang()) ? CollectionOrderRecordExportDTO.class : CollectionOrderRecordExportEnDTO.class;
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
                result = collectionOrderRecordClient.collectionOrderRecordPageExport(req);// 获取下一页数据
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
