package org.uu.manager.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.core.utils.ExcelUtil;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.MerchantInfoClient;
import org.uu.manager.config.AdminMapStruct;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.entity.BiWithdrawOrderDaily;
import org.uu.manager.mapper.BiPaymentOrderMapper;
import org.uu.manager.mapper.BiWithdrawOrderDailyMapper;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.service.IMerchantInfoService;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/merchantInfoAdmin", "/merchantInfoAdmin"})
@Api(description = "商户控制器")
public class MerchantInfoController {


    private final MerchantInfoClient merchantInfoClient;
    private final RedisUtils redisUtils;
    private final BiPaymentOrderMapper biPaymentOrderMapper;
    private final BiWithdrawOrderDailyMapper biWithdrawOrderDailyMapper;
    private final IMerchantInfoService iMerchantInfoService;
    private final AdminMapStruct adminMapStruct;


    @PostMapping("/createMerchantInfo")
    @ApiOperation(value = "创建商户")
    @SysLog(title = "商户控制器", content = "创建商户")
    public RestResult<MerchantInfoAddDTO> save(@RequestBody @ApiParam MerchantInfoAddReq req) {

        return merchantInfoClient.createMerchantInfo(req);
    }


    @PostMapping("/update")
    @ApiOperation(value = "更新商户信息")
    @SysLog(title = "商户控制器", content = "更新商户信息")
    public RestResult<MerchantInfoAddDTO> update(@RequestBody @ApiParam MerchantInfoUpdateReq merchantInfoReq) {
        return merchantInfoClient.updateForAdmin(merchantInfoReq);

    }


    @PostMapping("/updatePwd")
    @ApiOperation(value = "修改商户登录密码")
    @SysLog(title = "商户控制器", content = "修改商户登录密码")
    public RestResult updatePwd(@RequestBody @ApiParam MerchantInfoPwdReq merchantInfoPwdReq) {
        return merchantInfoClient.updateMerchantPwd(merchantInfoPwdReq);
    }

    @PostMapping("/updateUsdtAddress")
    @ApiOperation(value = "修改商户提现usdt地址")
    @SysLog(title = "商户控制器", content = "修改商户提现usdt地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "usdtAddress", value = "商户提现usdt地址", required = true, dataType = "String"),
            @ApiImplicitParam(name = "id", value = "商户id", required = true, dataType = "Long"),
    })
    public RestResult updateUsdtAddress(@RequestParam(value = "usdtAddress") String usdtAddress, @RequestParam(value = "id") Long id) {


        return merchantInfoClient.updateUsdtAddress(id, usdtAddress);
    }


    @PostMapping("/updateMerchantPublicKey")
    @ApiOperation(value = "修改商户公钥")
    @SysLog(title = "商户控制器", content = "修改商户公钥")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantPublicKey", value = "商户公钥", required = true, dataType = "String"),
            @ApiImplicitParam(name = "id", value = "商户id", required = true, dataType = "Long"),
    })
    public RestResult updateMerchantPublicKey(@RequestParam(value = "merchantPublicKey") String merchantPublicKey, @RequestParam(value = "id") Long id) {
        return merchantInfoClient.updateMerchantPublicKey(id, merchantPublicKey);
    }

    @PostMapping("/listpage")
    @ApiOperation(value = "获取商户列表")
    public RestResult<List<MerchantInfoListPageDTO>> list(@RequestBody @ApiParam MerchantInfoListPageReq req) {
        return merchantInfoClient.listPage(req);
    }

    @PostMapping("/current")
    @ApiOperation(value = "获取当前商户信息")
    public RestResult<MerchantInfoDTO> currentMerchantInfo() {
        Long id = UserContext.getCurrentUserId();
        return merchantInfoClient.fetchMerchantInfo(id);
    }


    /**
     * 商户后台手动下分
     *
     * @param req
     * @return
     */
    @PostMapping("/merchantWithdraw")
    @ApiOperation(value = "商户后台手动下分")
    @SysLog(title = "商户控制器", content = "商户后台手动下分")
    public RestResult merchantWithdraw(@Validated @RequestBody MerchantWithdrawReq req) {
        return merchantInfoClient.merchantWithdraw(req.getMerchantCode(), req.getAmount(), req.getCurrency(), req.getRemark(), req.getPayType());
    }


    @PostMapping("/delete")
    @ApiOperation(value = "删除")
    @SysLog(title = "商户控制器", content = "删除")
    public RestResult delete(@RequestBody @ApiParam MerchantInfoDeleteReq req) {
        return merchantInfoClient.delete(req);

    }

    @PostMapping("/getInfo")
    @ApiOperation(value = "商户详情")
    @SysLog(title = "商户控制器", content = "商户详情")
    public RestResult<MerchantInfoDTO> getInfo(@RequestBody @ApiParam MerchantInfoGetInfoReq req) {
        return merchantInfoClient.getInfo(req);

    }


    /**
     * @param
     * @param
     * @return
     */
    @PostMapping("/applyRecharge")
    @ApiOperation(value = "手动上分")
    @SysLog(title = "商户控制器", content = "手动上分")
    public RestResult<ApplyDistributedDTO> applyRecharge(@RequestBody ApplyDistributedReq req) {
        return merchantInfoClient.applyRecharge(req);
    }


    /**
     * @return
     */
    @PostMapping("/applyWithdraw")
    @ApiOperation(value = "手动下发")
    @SysLog(title = "商户控制器", content = "手动下发")
    public RestResult<ApplyDistributedDTO> applyWithdraw(@RequestBody ApplyDistributedReq req) {
        return merchantInfoClient.applyWithdraw(req);
    }


    /**
     * 获取商户首页信息
     *
     * @return
     */
    @PostMapping("/homePage")
    @ApiOperation(value = "获取商户首页信息")
    public RestResult<MerchantFrontPageDTO> fetchHomePageInfo() throws Exception {
        Long merchantId = UserContext.getCurrentUserId();
        String name = UserContext.getCurrentUserName();

        return merchantInfoClient.fetchHomePageInfo(merchantId, name);

    }


    /**
     * 获取商户首页信息
     *
     * @return
     */
    @PostMapping("/overview")
    @ApiOperation(value = "总后台数据概览")
    public RestResult<MerchantFrontPageDTO> fetchOverviewInfo() throws Exception {

        String endDate = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), GlobalConstants.DATE_FORMAT_DAY);
        String startDate = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()).minusDays(30), GlobalConstants.DATE_FORMAT_DAY);
        LambdaQueryWrapper<BiPaymentOrder> buyQuery = new LambdaQueryWrapper<>();
        buyQuery.ge(BiPaymentOrder::getDateTime, startDate);
        buyQuery.le(BiPaymentOrder::getDateTime, endDate);

        LambdaQueryWrapper<BiWithdrawOrderDaily> sellQuery = new LambdaQueryWrapper<>();
        sellQuery.ge(BiWithdrawOrderDaily::getDateTime, startDate);
        sellQuery.le(BiWithdrawOrderDaily::getDateTime, endDate);
        // 获取金额错误订单
        CompletableFuture<List<BiPaymentOrder>> buyFuture = CompletableFuture.supplyAsync(() -> {
            return biPaymentOrderMapper.selectList(buyQuery);
        });

        // 获取金额错误订单
        CompletableFuture<List<BiWithdrawOrderDaily>> sellFuture = CompletableFuture.supplyAsync(() -> {
            return biWithdrawOrderDailyMapper.selectList(sellQuery);
        });

        // 获取金额错误订单
        CompletableFuture<RestResult<MerchantFrontPageDTO>> resultFuture = CompletableFuture.supplyAsync(() -> {
            return merchantInfoClient.fetchOverviewInfo();
        });


        CompletableFuture<Void> allFutures = CompletableFuture.allOf(buyFuture, sellFuture, resultFuture);
        allFutures.get();

        List<BiPaymentOrder> biPaymentOrders = buyFuture.get();
        List<BiPaymentOrderDTO> biPaymentOrdersDTO = new ArrayList<>();
        List<BiWithdrawOrderDaily> biWithdrawOrderDailies = sellFuture.get();
        List<BiWithdrawOrderDailyDTO> biWithdrawOrderDailiesDTO = new ArrayList<>();

        for (BiPaymentOrder item : biPaymentOrders) {
            BiPaymentOrderDTO biPaymentOrderDTO = new BiPaymentOrderDTO();
            BeanUtils.copyProperties(item, biPaymentOrderDTO);
            if (item.getOrderNum() <= 0L) {
                biPaymentOrderDTO.setSuccessRate(0d);
            } else {
                Double result = new BigDecimal(item.getSuccessOrderNum().toString())
                        .divide(new BigDecimal(item.getOrderNum().toString()), 2, RoundingMode.DOWN).doubleValue();
                biPaymentOrderDTO.setSuccessRate(result);
            }

            biPaymentOrdersDTO.add(biPaymentOrderDTO);
        }

        for (BiWithdrawOrderDaily item : biWithdrawOrderDailies) {
            BiWithdrawOrderDailyDTO biWithdrawOrderDailyDTO = new BiWithdrawOrderDailyDTO();
            BeanUtils.copyProperties(item, biWithdrawOrderDailyDTO);
            if (item.getOrderNum() <= 0L) {
                biWithdrawOrderDailyDTO.setSuccessRate(0d);
            } else {
                double result = new BigDecimal(item.getSuccessOrderNum().toString())
                        .divide(new BigDecimal(item.getOrderNum().toString()), 2, RoundingMode.DOWN).doubleValue();
                biWithdrawOrderDailyDTO.setSuccessRate(result);
            }
            biWithdrawOrderDailiesDTO.add(biWithdrawOrderDailyDTO);
        }

        RestResult<MerchantFrontPageDTO> result = resultFuture.get();
        result.getData().setBuyList(biPaymentOrdersDTO);
        result.getData().setSellList(biWithdrawOrderDailiesDTO);
        return result;

    }

    @PostMapping("/fetchWithdrawOrderInfo")
    @ApiOperation(value = "获取代付订单列表")
    public RestResult<List<WithdrawOrderDTO>> fetchWithdrawOrderInfo(@Validated @RequestBody WithdrawOrderReq withdrawOrderReq) {
        if (StringUtils.isEmpty(withdrawOrderReq.getMerchantCode())) {
            Long merchantId = UserContext.getCurrentUserId();
            String merchantStr = (String) redisUtils.hget(RedisConstants.MERCHANT_INFO, merchantId.toString());
            String merchantCode = CommonUtils.getMerchantCode(merchantStr);
            withdrawOrderReq.setMerchantCode(merchantCode);
        }
        RestResult<List<WithdrawOrderDTO>> result = merchantInfoClient.fetchWithdrawOrderInfo(withdrawOrderReq);
        return result;
    }

    @PostMapping("/fetchWithdrawOrder")
    @ApiOperation(value = "获取总后台代付订单列表")
    public RestResult<List<WithdrawOrderDTO>> fetchWithdrawOrder(@Validated @RequestBody WithdrawOrderReq withdrawOrderReq) {
        return merchantInfoClient.fetchWithdrawOrderInfo(withdrawOrderReq);
    }

    @PostMapping("/closePaymentOrder")
    @ApiOperation(value = "关闭代付订单")
    public RestResult<Boolean> closePaymentOrder(@Validated @RequestBody PaidParamReq req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        return merchantInfoClient.closePaymentOrder(req);
    }

    @PostMapping("/fetchWithdrawOrderExport")
    @ApiOperation(value = "代付订单列表导出")
    public void fetchWithdrawOrderExport(HttpServletResponse response, @RequestBody @ApiParam WithdrawOrderReq matchingOrderReq) throws IOException {
        matchingOrderReq.setPageSize(GlobalConstants.BATCH_SIZE);
        RestResult<List<WithdrawOrderExportDTO>> result = merchantInfoClient.fetchWithdrawOrderInfoExport(matchingOrderReq);
        List<WithdrawOrderExportDTO> data = result.getData();
        // 获取class
        Class<?> clazz = getExportModeClass(matchingOrderReq.getLang(), matchingOrderReq.getSource(), WithdrawOrderExportDTO.class, WithdrawOrderExportEnDTO.class,
                WithdrawOrderExportForMerchantDTO.class, WithdrawOrderExportForMerchantEnDTO.class);
        // 根据source转化对象
        List<?> exportData = getExportWithdrawModeData(matchingOrderReq.getSource(), data);

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
            ExcelUtil.setResponseHeader(response, "MerchantPayoutOrders");
            excelWriter = EasyExcel.write(bos, clazz).build();
            head = ExcelUtil.parseHead(clazz);

            WriteSheet testSheet = EasyExcel.writerSheet("sheet1")
                    .head(head)
                    .build();
            excelWriter.write(exportData, testSheet);
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
                RestResult<List<WithdrawOrderExportDTO>> resultList = merchantInfoClient.fetchWithdrawOrderInfoExport(matchingOrderReq);
                List<WithdrawOrderExportDTO> exportResultList = resultList.getData();
                List<?> exportListData = getExportWithdrawModeData(matchingOrderReq.getSource(), exportResultList);
                exportTotalSize = exportTotalSize + resultList.getData().size();
                if (exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE) {
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet("sheet1")
                        .build();
                excelWriter.write(exportListData, testSheet1);
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


    @PostMapping("/confirmSuccess")
    @ApiOperation(value = "手动回调成功")
    @SysLog(title = "商户控制器", content = "代付手动回调成功")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "记录id", required = true, dataType = "Long")
    })
    public RestResult<String> confirmSuccess(Long id) {

        return merchantInfoClient.confirmSuccess(id);
    }

    @PostMapping("/orderStatus")
    @ApiOperation(value = "获取订单状态")
    public RestResult fetchOrderStatus() {

        RestResult<Map<Integer, String>> map = merchantInfoClient.fetchOrderStatus();
        return map;

    }

    @PostMapping("/orderCallbackStatus")
    @ApiOperation(value = "获取订单回调状态")
    public RestResult orderCallbackStatus() {

        RestResult<Map<Integer, String>> map = merchantInfoClient.orderCallbackStatus();
        return map;

    }

    @PostMapping("/rechargeConfirmSuccess")
    @ApiOperation(value = "代收手动回调成功")
    @SysLog(title = "商户控制器", content = "代收手动回调成功")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "记录id", required = true, dataType = "Long")
    })
    public RestResult<Boolean> rechargeConfirmSuccess(Long id) {

        RestResult<Boolean> result = merchantInfoClient.rechargeConfirmSuccess(id);
        return result;
    }


    @PostMapping("/resetKey")
    @ApiOperation(value = "重置商户密钥")
    public RestResult resetKey(@RequestParam("code") String code) {
        return merchantInfoClient.resetKey(code);
    }

    @PostMapping("/resetMerchantGoogle")
    @ApiOperation(value = "重置商户谷歌密钥")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "merchantCode", value = "商户code", required = true, dataType = "String")
    })
    public RestResult resetMerchantGoogle(@RequestParam("merchantCode") String merchantCode) {
        return merchantInfoClient.resetMerchantGoogle(merchantCode);
    }


    /**
     * @param
     * @param
     * @return
     */
    @PostMapping("/resetPassword")
    @SysLog(title = "商户控制器", content = "重置密码")
    @ApiOperation(value = "重置密码")
    public RestResult resetPassword(@RequestParam("code") String code) {
        return merchantInfoClient.resetPassword(code);
    }

    @PostMapping("/fetchRechargeOrderInfo")
    @ApiOperation(value = "获取代收订单列表")
    public RestResult<List<RechargeOrderDTO>> fetchRechargeOrderInfo(@Validated @RequestBody RechargeOrderReq rechargeOrderReq) {
        if (StringUtils.isEmpty(rechargeOrderReq.getMerchantCode())) {
            Long merchantId = UserContext.getCurrentUserId();
            String merchantStr = (String) redisUtils.hget(RedisConstants.MERCHANT_INFO, merchantId.toString());
            String merchantCode = CommonUtils.getMerchantCode(merchantStr);
            rechargeOrderReq.setMerchantCode(merchantCode);
        }
        return merchantInfoClient.fetchRechargeOrderInfo(rechargeOrderReq);
    }

    @PostMapping("/fetchCollectionOrderInfo")
    @ApiOperation(value = "总后台获取代收订单列表")
    public RestResult<List<RechargeOrderDTO>> fetchCollectionOrderInfo(@Validated @RequestBody RechargeOrderReq rechargeOrderReq) {
        return merchantInfoClient.fetchRechargeOrderInfo(rechargeOrderReq);
    }

    @PostMapping("/export")
    @ApiOperation(value = "代收订单列表导出")
    public void export(HttpServletResponse response, @RequestBody @ApiParam RechargeOrderReq matchingOrderReq) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        matchingOrderReq.setPageSize(GlobalConstants.BATCH_SIZE);
        RestResult<List<RechargeOrderExportDTO>> result = merchantInfoClient.fetchRechargeOrderInfoExport(matchingOrderReq);
        List<RechargeOrderExportDTO> data = result.getData();
        // 获取class
        Class<?> clazz = getExportModeClass(matchingOrderReq.getLang(), matchingOrderReq.getSource(), RechargeOrderExportDTO.class, RechargeOrderExportEnDTO.class,
                RechargeOrderExportForMerchantDTO.class, RechargeOrderExportForMerchantEnDTO.class);
        // 根据source转化对象
        List<?> exportData = getExportRechargeModeData(matchingOrderReq.getSource(), data);
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
            ExcelUtil.setResponseHeader(response, "MerchantCollectionOrders");
            excelWriter = EasyExcel.write(bos, clazz).build();
            head = ExcelUtil.parseHead(clazz);

            WriteSheet testSheet = EasyExcel.writerSheet("sheet1")
                    .head(head)
                    .build();
            excelWriter.write(exportData, testSheet);
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
                RestResult<List<RechargeOrderExportDTO>> resultList = merchantInfoClient.fetchRechargeOrderInfoExport(matchingOrderReq);
                List<RechargeOrderExportDTO> resultListData = resultList.getData();
                List<?> exportDataList = getExportRechargeModeData(matchingOrderReq.getSource(), resultListData);
                exportTotalSize = exportTotalSize + resultList.getData().size();
                if (exportTotalSize > GlobalConstants.EXPORT_TOTAL_SIZE) {
                    return;
                }
                WriteSheet testSheet1 = EasyExcel.writerSheet("sheet1")
                        .build();
                excelWriter.write(exportDataList, testSheet1);
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

    private Class<?> getExportModeClass(String lang, String source, Class<?> zhBase, Class<?> enBase, Class<?> zhOther, Class<?> enOther) {
        Class<?> clazz = Objects.equals(lang, "zh") ? zhBase : enBase;
        if ("2".equals(source)) {
            clazz = Objects.equals(lang, "zh") ? zhOther : enOther;
        }
        return clazz;
    }

    private List<?> getExportRechargeModeData(String source, List<RechargeOrderExportDTO> data) {
        if ("1".equals(source)) {
            return data;
        }
        return adminMapStruct.toRechargeOrderExportForMerchantDTO(data);
    }

    private List<?> getExportWithdrawModeData(String source, List<WithdrawOrderExportDTO> data) {
        if ("1".equals(source)) {
            return data;
        }
        return adminMapStruct.toWithdrawOrderExportForMerchantDTO(data);
    }

    @GetMapping("/getMerchantName")
    @ApiOperation(value = "获取商户名称")
    public RestResult getMerchantName() {

        RestResult<Map<Integer, String>> map = merchantInfoClient.getMerchantName();
        return map;

    }


    @GetMapping("/getCurrency")
    @ApiOperation(value = "获取币种列表")
    public RestResult getCurrency() {

        RestResult<Map<String, String>> map = merchantInfoClient.getCurrency();
        return map;

    }


    @PostMapping("/getOrderNumOverview")
    @ApiOperation(value = "获取订单数量的概览")
    public RestResult<OrderOverviewDTO> getOrderNumOverview() {
        return merchantInfoClient.getOrderNumOverview();
    }

    @PostMapping("/getMemberOverview")
    @ApiOperation(value = "获取在线会员和委托相关概览")
    public RestResult<MemberOverviewDTO> getMemberOverview() {
        return merchantInfoClient.getMemberOverview();
    }


    @PostMapping("/getMerchantOrderOverview")
    @ApiOperation(value = "获取代收代付订单统计")
    public RestResult<BiOverViewStatisticsDailyDTO> getMerchantOrderOverview(@RequestBody @ApiParam MerchantDailyReportReq req) {
        return iMerchantInfoService.getMerchantOrderOverview(req);
    }

    @PostMapping("/todayOrderOverview")
    @ApiOperation(value = "获取今日订单统计")
    public RestResult<TodayOrderOverviewDTO> todayOrderOverview() {
        return merchantInfoClient.todayOrderOverview();
    }

    @PostMapping("/todayUsdtOrderOverview")
    @ApiOperation(value = "获取今日订单统计")
    public RestResult<TodayUsdtOrderOverviewDTO> todayUsdtOrderOverview() {
        return merchantInfoClient.todayUsdtOrderOverview();
    }

    @PostMapping("/paid")
    @ApiOperation(value = "已支付")
    public KycRestResult paid(@RequestBody @ApiParam PaidParamReq req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        return merchantInfoClient.paid(req);
    }

    @PostMapping("/unPaid")
    @ApiOperation(value = "未支付")
    public KycRestResult unPaid(@RequestBody @ApiParam PaidParamReq req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        return merchantInfoClient.unPaid(req);
    }

    @PostMapping("/usdtPaid")
    @ApiOperation(value = "usdt已支付")
    public KycRestResult usdtPaid(@RequestBody @ApiParam PaidParamReq req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        return merchantInfoClient.usdtPaid(req);
    }

    @PostMapping("/usdtUnPaid")
    @ApiOperation(value = "usdt未支付")
    public KycRestResult usdtUnPaid(@RequestBody @ApiParam PaidParamReq req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        return merchantInfoClient.usdtUnPaid(req);
    }

    @PostMapping("/manualConfirmation")
    @ApiOperation(value = "人工审核确认")
    public KycRestResult manualConfirmation(@RequestBody @ApiParam PaidParamReq req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        return merchantInfoClient.manualConfirmation(req);
    }

    @PostMapping("/manualCancel")
    @ApiOperation(value = "人工审核取消")
    public KycRestResult manualCancel(@RequestBody @ApiParam PaidParamReq req) {
        req.setUpdateBy(UserContext.getCurrentUserName());
        return merchantInfoClient.manualCancel(req);
    }

}
