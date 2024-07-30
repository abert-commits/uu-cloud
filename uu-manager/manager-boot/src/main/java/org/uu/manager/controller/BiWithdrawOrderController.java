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
import org.uu.common.pay.dto.*;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.entity.BiPaymentOrderMonth;
import org.uu.manager.entity.BiWithdrawOrderDaily;
import org.uu.manager.req.PaymentMonthOrderReportReq;
import org.uu.manager.req.PaymentOrderReportReq;
import org.uu.manager.req.WithdrawDailyOrderReportReq;
import org.uu.manager.service.IBiPaymentOrderMonthService;
import org.uu.manager.service.IBiWithdrawOrderDailyService;
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
@Slf4j
@RestController
@RequestMapping(value = {"/api/v1/biWithdrawOrder","/biWithdrawOrder"})
@Api(description = "卖出日报表控制器")
public class BiWithdrawOrderController {

    @Resource
    IBiWithdrawOrderDailyService iBiWithdrawOrderDailyService;

    @PostMapping("/query")
    @ApiOperation(value = "查询卖出日报表记录")
    public RestResult<List<BiWithdrawOrderDaily>> listPage(@Validated @RequestBody WithdrawDailyOrderReportReq req) {
        PageReturn<BiWithdrawOrderDaily> result = iBiWithdrawOrderDailyService.listPage(req);
        return RestResult.page(result);
    }

    @PostMapping("/export")
    @ApiOperation(value = "卖出报表导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam WithdrawDailyOrderReportReq req) throws IOException {
        req.setPageSize(GlobalConstants.BATCH_SIZE);
        PageReturn<BiWithdrawOrderDailyExportDTO> result = iBiWithdrawOrderDailyService.listPageForExport(req);
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
                PageReturn<BiWithdrawOrderDailyExportDTO> resultList = iBiWithdrawOrderDailyService.listPageForExport(req);
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
