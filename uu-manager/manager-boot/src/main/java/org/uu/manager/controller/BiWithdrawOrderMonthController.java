package org.uu.manager.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.core.utils.ExcelUtil;
import org.uu.common.pay.dto.BiWithdrawEnOrderDailyDTO;
import org.uu.common.pay.dto.BiWithdrawOrderDailyExportDTO;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.entity.BiWithdrawOrderDaily;
import org.uu.manager.entity.BiWithdrawOrderMonth;
import org.uu.manager.req.WithdrawDailyOrderReportReq;
import org.uu.manager.req.WithdrawMonthOrderReportReq;
import org.uu.manager.service.IBiWithdrawOrderDailyService;
import org.uu.manager.service.IBiWithdrawOrderMonthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author
 */
@RestController
@Slf4j
@RequestMapping(value = {"/api/v1/biWithdrawOrderMonth","/biWithdrawOrderMonth"})
@Api(description = "卖出月报表控制器")
public class BiWithdrawOrderMonthController {

    @Resource
    IBiWithdrawOrderMonthService iBiWithdrawOrderMonthService;



    @PostMapping("/query")
    @ApiOperation(value = "卖出代付月报表记录")
    public RestResult<List<BiWithdrawOrderMonth>> listPage(@Validated @ApiParam @RequestBody WithdrawMonthOrderReportReq req) {
        PageReturn<BiWithdrawOrderMonth> result = iBiWithdrawOrderMonthService.listPage(req);
        return RestResult.page(result);
    }

    @PostMapping("/export")
    @ApiOperation(value = "卖出报表导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam WithdrawMonthOrderReportReq req) throws IOException {
        req.setPageSize(GlobalConstants.BATCH_SIZE);
        PageReturn<BiWithdrawOrderDailyExportDTO> result = iBiWithdrawOrderMonthService.listPageForExport(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "BiWithdrawOrderRecords";
        String sheetName = "sheet1";
        try{
            outputStream = response.getOutputStream();
            exportTotalSize = result.getList().size();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = BiWithdrawOrderDailyExportDTO.class;
            ExcelUtil.setResponseHeader(response, fileName);
            if (!"zh".equals(req.getLang())) {
                clazz = BiWithdrawEnOrderDailyDTO.class;
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
                PageReturn<BiWithdrawOrderDailyExportDTO> resultList = iBiWithdrawOrderMonthService.listPageForExport(req);
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
