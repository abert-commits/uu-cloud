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
import org.uu.common.pay.req.*;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MatchingOrderClient;
import org.uu.manager.util.PsCheckUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * @author Admin
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(description = "撮合列表信息控制器")
@RequestMapping(value = {"/api/v1/matchingOrderAdmin", "/matchingOrderAdmin"})
public class MatchingOrderController {
    private final MatchingOrderClient matchingOrderClient;

    private final PsCheckUtils psCheckUtils;

    /**
     * 批次大小
     */

    @PostMapping("/listpage")
    @ApiOperation(value = "获取撮合列表")
    public RestResult<List<MatchingOrderPageListDTO>> listpage(@RequestBody @ApiParam MatchingOrderReq matchingOrderReq) {
        RestResult<List<MatchingOrderPageListDTO>> result = matchingOrderClient.listpage(matchingOrderReq);
        return result;
    }


    /**
     * 批次大小
     */

    @PostMapping("/relationOrderList")
    @ApiOperation(value = "查询关联订单信息")
    public RestResult<List<RelationOrderDTO>> relationOrderList(@RequestBody @ApiParam RelationshipOrderReq matchingOrderReq) {
        RestResult<List<RelationOrderDTO>> result = matchingOrderClient.relationOrderList(matchingOrderReq);
        return result;
    }

    @PostMapping("/export")
    @ApiOperation(value = "撮合列表导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam MatchingOrderReq matchingOrderReq) throws IOException {
        matchingOrderReq.setPageSize(GlobalConstants.BATCH_SIZE);
        RestResult<List<MatchingOrderExportDTO>> result = matchingOrderClient.listpageExport(matchingOrderReq);
        OutputStream outputStream = null;
        BufferedOutputStream bos = null;
        ExcelWriter excelWriter = null;
        Integer exportTotalSize = 0;
        try {
            outputStream = response.getOutputStream();
            exportTotalSize = result.getData().size();
            //必须放到循环外，否则会刷新流
            excelWriter = null;
            List<List<String>> head = null;
            bos = new BufferedOutputStream(outputStream);
            if (matchingOrderReq.getLang().equals("zh")) {
                ExcelUtil.setResponseHeader(response, "MatchingList");
                excelWriter = EasyExcel.write(bos, MatchingOrderExportDTO.class).build();
                head = ExcelUtil.parseHead(MatchingOrderExportDTO.class);
            } else {
                ExcelUtil.setResponseHeader(response, "MatchingList");
                excelWriter = EasyExcel.write(bos, MatchingOrderExportDTO.class).build();
                head = ExcelUtil.parseHead(MatchingOrderExportDTO.class);
            }

            WriteSheet testSheet = EasyExcel.writerSheet("sheet1")
                    .head(head)
                    .build();
            excelWriter.write(result.getData(), testSheet);
            long pageNo = 1;
            long totalSize = 0;
            // startTime <= time < endTime
            if (result.getTotal() > GlobalConstants.BATCH_SIZE && result.getTotal() % GlobalConstants.BATCH_SIZE > 0) {
                totalSize = (result.getTotal() / GlobalConstants.BATCH_SIZE) + 1;
            } else if (result.getTotal() > GlobalConstants.BATCH_SIZE && result.getTotal() % GlobalConstants.BATCH_SIZE <= 0) {
                totalSize = (result.getTotal() / GlobalConstants.BATCH_SIZE);
            }
            for (int i = 0; i < totalSize; i++) {
                pageNo++;
                matchingOrderReq.setPageNo(pageNo);
                matchingOrderReq.setPageSize(GlobalConstants.BATCH_SIZE);
                RestResult<List<MatchingOrderExportDTO>> resultList = matchingOrderClient.listpageExport(matchingOrderReq);
                exportTotalSize = exportTotalSize + resultList.getData().size();
                if(exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE){
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet("sheet1")
                        .build();
                excelWriter.write(resultList.getData(), testSheet1);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            bos.flush();
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }


    @PostMapping("/update")
    @SysLog(title = "撮合列表信息控制器", content = "更新")
    @ApiOperation(value = "更新")
    public RestResult<MatchingOrderDTO> update(@Validated @RequestBody MatchingOrderReq matchingOrderReq) {

        RestResult<MatchingOrderDTO> result = matchingOrderClient.update(matchingOrderReq);
        return result;
    }

    @PostMapping("/appealDetail")
    @ApiOperation(value = "申诉信息")
    public RestResult<MatchingOrderVoucherDTO> appealDetail(@Validated @RequestBody @ApiParam MatchingOrderIdReq req) {
        RestResult<MatchingOrderVoucherDTO> result = matchingOrderClient.appealDetail(req);
        return result;
    }


    @PostMapping("/getInfo")
    @ApiOperation(value = "查看")
    public RestResult<MatchingOrderInfoDTO> getInfo(@Validated @RequestBody @ApiParam MatchingOrderIdReq req) {
        RestResult<MatchingOrderInfoDTO> result = matchingOrderClient.getInfo(req);
        return result;
    }

    @PostMapping("/getMatchingOrderTotal")
    @ApiOperation(value = "总计")
    public RestResult<MatchingOrderDTO> getMatchingOrderTotal(@Validated @ApiParam @RequestBody MatchingOrderReq req) {
        RestResult<MatchingOrderDTO> result = matchingOrderClient.getMatchingOrderTotal(req);
        return result;
    }


    @PostMapping("/appealSuccess")
    @SysLog(title = "撮合列表信息控制器", content = "申诉成功")
    @ApiOperation(value = "申诉成功")
    public RestResult<MatchingOrderDTO> appealSuccess(@Validated @RequestBody MatchingOrderIdReq req) {
        RestResult<MatchingOrderDTO> result = matchingOrderClient.appealSuccess(req);
        return result;
    }

    @PostMapping("/appealFailure")
    @SysLog(title = "撮合列表信息控制器", content = "申诉失败")
    @ApiOperation(value = "申诉失败")
    public RestResult<MatchingOrderDTO> appealFailure(@Validated @RequestBody MatchingOrderIdReq req) {
        RestResult<MatchingOrderDTO> result = matchingOrderClient.appealFailure(req);
        return result;
    }

    @PostMapping("/pay")
    @SysLog(title = "撮合列表信息控制器", content = "已支付")
    @ApiOperation(value = "已支付")
    public RestResult<MatchingOrderDTO> pay(@Validated @RequestBody MatchingOrderAppealReq req) {
        RestResult<MatchingOrderDTO> result = matchingOrderClient.pay(req);

        return result;
    }

    @PostMapping("/nopay")
    @SysLog(title = "撮合列表信息控制器", content = "未支付")
    @ApiOperation(value = "未支付")
    public RestResult<MatchingOrderDTO> nopay(@Validated @RequestBody MatchingOrderAppealReq req) {

        RestResult<MatchingOrderDTO> result = matchingOrderClient.nopay(req);
        return result;
    }

    @PostMapping("/incorrectTransfer")
    @SysLog(title = "撮合列表信息控制器", content = "错误转帐")
    @ApiOperation(value = "错误转帐")
    public RestResult<MatchingOrderDTO> incorrectTransfer(@Validated @RequestBody MatchingOrderAppealReq req) {
        String updateBy = UserContext.getCurrentUserName();
        req.setUpdateBy(updateBy);
        RestResult<MatchingOrderDTO> result = matchingOrderClient.incorrectTransfer(req);

        return result;
    }


    @PostMapping("/incorrectVoucher")
    @ApiOperation(value = "错误凭证")
    public RestResult<MatchingOrderVoucherDTO> incorrectVoucher(@Validated @RequestBody MatchingOrderIdReq req) {
        RestResult<MatchingOrderVoucherDTO> result = matchingOrderClient.incorrectVoucher(req);

        return result;
    }


    @PostMapping("/pscheck")
    @ApiOperation(value = "ps检测")
    public RestResult<MatchingOrderVoucherUrlDTO> pscheck(@Validated @RequestBody MatchingOrderIdReq req) {
        RestResult<MatchingOrderVoucherUrlDTO> result = matchingOrderClient.pscheck(req);
        psCheckUtils.check(result.getData().getVoucher());
        return result;
    }

    @PostMapping("/manualReview")
    @ApiOperation(value = "人工审核")
    public RestResult manualReview(@Validated @RequestBody MatchingOrderManualReq req) {
        return matchingOrderClient.manualReview(req);
    }


}
