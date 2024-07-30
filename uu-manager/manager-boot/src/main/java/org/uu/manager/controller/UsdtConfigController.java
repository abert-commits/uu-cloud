package org.uu.manager.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.ExcelUtil;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.UsdtConfigClient;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@Api(description = "usdt管理")
@RequestMapping(value = {"/api/v1/usdtConfigAdmin", "/usdtConfigAdmin"})
@Slf4j
public class UsdtConfigController {
    @Resource
    UsdtConfigClient usdtConfigClient;

    @PostMapping("/listpage")
    @ApiOperation(value = "usdt管理列表")
    public RestResult<List<UsdtConfigDTO>> list(@RequestBody @ApiParam UsdtConfigPageReq req) {
        RestResult<List<UsdtConfigDTO>> result = usdtConfigClient.listpage(req);
        return result;
    }

    @PostMapping("/addressListPage")
    @ApiOperation(value = "usdt地址管理分页列表")
    public RestResult<List<UsdtAddressDTO>> addressListPage(@RequestBody @ApiParam UsdtAddrPageReq req) {
        return usdtConfigClient.addressListPage(req);
    }

    @PostMapping("/export")
    @ApiOperation(value = "usdt地址管理会员列表导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam UsdtAddrPageReq req) throws IOException {
        RestResult<List<UsdtAddressMemberDTO>> result = usdtConfigClient.addressExportPage(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "platformMembersRecords";
        String sheetName = "sheet1";
        try {
            outputStream = response.getOutputStream();
            exportTotalSize = result.getTotal();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = "zh".equals(req.getLang()) ? UsdtAddressMemberDTO.class : UsdtAddressEnDTO.class;
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
                result = usdtConfigClient.addressExportPage(req);// 获取下一页数据
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

    @PostMapping("/merchantExport")
    @ApiOperation(value = "usdt地址管理商户列表导出")
    public void merchantExport(HttpServletResponse response, @RequestBody @ApiParam UsdtAddrPageReq req) throws IOException {
        RestResult<List<UsdtAddressMerchantDTO>> result = usdtConfigClient.addressMerchantExportPage(req);
        OutputStream outputStream;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        int exportTotalSize;
        String fileName = "merchantMembersRecords";
        String sheetName = "sheet1";
        try {
            outputStream = response.getOutputStream();
            exportTotalSize = result.getTotal();
            // 写入head
            List<List<String>> head;
            bos = new BufferedOutputStream(outputStream);
            Class<?> clazz = "zh".equals(req.getLang()) ? UsdtAddressMerchantDTO.class : UsdtAddressMerchantEnDTO.class;
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
                result = usdtConfigClient.addressMerchantExportPage(req); // 获取下一页数据
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


    @PostMapping("/collectFundsForAccounts")
    @ApiOperation(value = "批量归集地址")
    public RestResult collectFundsForAccounts(@RequestBody @ApiParam BatchCollectAddrReq req) {
        List<String> usdtAddressList = req.getUsdtAddressList().stream()
                .filter(usdtAddress -> usdtAddress.getUsdtBalance().compareTo(BigDecimal.ONE) >= 0 ||
                        usdtAddress.getTrxBalance().compareTo(BigDecimal.ONE) >= 0)
                .map(BatchCollectAddrReq.UsdtAddress::getUsdtAddress)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(usdtAddressList)) {
            return RestResult.failed("usdt balance must be greater than 0");
        }
        return usdtConfigClient.collectFundsForAccounts(usdtAddressList);

    }


    @PostMapping("/create")
    @SysLog(title = "usdt管理", content = "新增usdt配置")
    @ApiOperation(value = "新增usdt配置")
    public RestResult<UsdtConfigDTO> create(@RequestBody @ApiParam UsdtConfigCreateReq req) {
        RestResult result = usdtConfigClient.create(req);
        return result;
    }

    @PostMapping("/update")
    @SysLog(title = "usdt管理", content = "修改usdt配置")
    @ApiOperation(value = "修改usdt配置")
    public RestResult<UsdtConfigDTO> update(@RequestBody @ApiParam UsdtConfigReq req) {
        RestResult<UsdtConfigDTO> result = usdtConfigClient.update(req);

        return result;
    }

    @PostMapping("/changeStatus")
    @SysLog(title = "usdt管理", content = "修改状态")
    @ApiOperation(value = "修改状态")
    public RestResult<UsdtConfigDTO> changeStatus(@RequestBody @ApiParam UsdtConfigQueryReq req) {
        RestResult<UsdtConfigDTO> result = usdtConfigClient.changeStatus(req);
        return result;
    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "获取配置详情")
    public RestResult<UsdtConfigDTO> getInfo(@RequestBody @ApiParam UsdtConfigIdReq req) {
        RestResult<UsdtConfigDTO> result = usdtConfigClient.getInfo(req);

        return result;
    }

    @PostMapping("/delete")
    @SysLog(title = "usdt管理", content = "删除")
    @ApiOperation(value = "删除")
    public RestResult<UsdtConfigDTO> delete(@RequestBody @ApiParam UsdtConfigIdReq req) {
        RestResult<UsdtConfigDTO> result = usdtConfigClient.delete(req);
        return result;
    }

}
