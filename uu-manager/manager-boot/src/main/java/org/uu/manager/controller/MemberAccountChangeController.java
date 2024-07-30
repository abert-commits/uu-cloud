package org.uu.manager.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.ExcelUtil;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.MemberAccountChangeReq;
import org.uu.manager.api.MemberAccountChangeClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.manager.config.AdminMapStruct;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@Api(description = "会员账变控制器")
@RequestMapping(value = {"/api/v1/memberAccounthangeAdmin", "/memberAccounthangeAdmin"})
@Slf4j
public class MemberAccountChangeController {
    private final MemberAccountChangeClient memberAccountChangeClient;

    private final AdminMapStruct adminMapStruct;


    @PostMapping("/listpage")
    @ApiOperation(value = "会员账变列表")
    public RestResult<List<MemberAccountChangeDTO>> listpage(@RequestBody @ApiParam @Valid MemberAccountChangeReq memberAccountChangeReq) {
        RestResult<List<MemberAccountChangeDTO>> result = memberAccountChangeClient.listpage(memberAccountChangeReq);
        return result;
    }

    @PostMapping("/export")
    @ApiOperation(value = "会员账变导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam MemberAccountChangeReq req) throws IOException {
        RestResult<List<MemberAccountChangeExportDTO>> result = memberAccountChangeClient.listpageForExport(req);
        List<MemberAccountChangeExportDTO> data = result.getData();
        // 获取class
        Class<?> clazz = getExportModeClass(req.getLang(), req.getSource(), MemberAccountChangeExportDTO.class, MemberAccountChangeExportEnDTO.class,
                MemberAccountChangeExportForMerchantDTO.class, MemberAccountChangeExportForMerchantEnDTO.class);
        // 根据source转化对象
        List<?> exportData = getExportAccountChangeModeData(req.getSource(), data);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "BiWithdrawOrderRecords";
        String sheetName = "sheet1";
        try{
            outputStream = response.getOutputStream();
            exportTotalSize = result.getTotal();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            ExcelUtil.setResponseHeader(response, fileName);
            excelWriter = EasyExcel.write(bos, clazz).build();
            head = ExcelUtil.parseHead(clazz);
            WriteSheet testSheet = EasyExcel.writerSheet(sheetName)
                    .head(head)
                    .build();
            excelWriter.write(exportData, testSheet);
            // 写入数据
            long pageNo = 1;
            long totalSize = ExcelUtil.getTotalSize(result.getTotal());
            for (int i = 0; i < totalSize; i++) {
                pageNo++;
                req.setPageNo(pageNo);
                req.setPageSize(GlobalConstants.BATCH_SIZE);
                RestResult<List<MemberAccountChangeExportDTO>> resultList = memberAccountChangeClient.listpageForExport(req);
                List<MemberAccountChangeExportDTO> data1 = resultList.getData();
                List<?> exportAccountChangeModeData = getExportAccountChangeModeData(req.getSource(), data1);
                exportTotalSize = exportTotalSize + resultList.getData().size();
                if(exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE){
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet(sheetName)
                        .build();
                excelWriter.write(exportAccountChangeModeData, testSheet1);
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

    private List<?> getExportAccountChangeModeData(String source, List<MemberAccountChangeExportDTO> data) {
        if ("1".equals(source)) {
            return data;
        }
        return adminMapStruct.toMemberAccountChangeExportForMerchantDTO(data);
    }

    private Class<?> getExportModeClass(String lang, String source, Class<?> zhBase, Class<?> enBase, Class<?> zhOther, Class<?> enOther) {
        Class<?> clazz = Objects.equals(lang, "zh") ? zhBase : enBase;
        if ("2".equals(source)) {
            clazz = Objects.equals(lang, "zh") ? zhOther : enOther;
        }
        return clazz;
    }


}
