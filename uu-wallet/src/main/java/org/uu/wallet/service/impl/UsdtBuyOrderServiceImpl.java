package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.UsdtBuyOrderDTO;
import org.uu.common.pay.dto.UsdtBuyOrderExportDTO;
import org.uu.common.pay.dto.UsdtBuySuccessOrderDTO;
import org.uu.common.pay.req.UsdtBuyOrderIdReq;
import org.uu.common.pay.req.UsdtBuyOrderReq;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.ChangeModeEnum;
import org.uu.wallet.Enum.CurrenceEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.UsdtPayTypeEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.UsdtBuyOrder;
import org.uu.wallet.mapper.UsdtBuyOrderMapper;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.service.*;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.vo.UsdtBuyOrderVo;
import org.uu.wallet.vo.UsdtBuyPageDataVo;
import org.uu.wallet.vo.UsdtPurchaseOrderDetailsVo;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author
 */
@Service
@Slf4j
public class UsdtBuyOrderServiceImpl extends ServiceImpl<UsdtBuyOrderMapper, UsdtBuyOrder> implements IUsdtBuyOrderService {
    @Resource
    WalletMapStruct walletMapStruct;
    @Resource
    IUsdtConfigService usdtConfigService;
    @Resource
    ITradeConfigService tradeConfigService;
    @Resource
    @Lazy
    private IMemberInfoService memberInfoService;

    @Resource
    @Lazy
    AmountChangeUtil amountChangeUtil;
    @Resource
    @Lazy
    IRechargeTronDetailService iRechargeTronDetailService;

    /**
     * 根据会员id 查询usdt买入记录
     *
     * @param memberId
     * @return {@link List}<{@link UsdtBuyOrder}>
     */
    @Override
    public List<UsdtBuyOrderVo> findPagedUsdtPurchaseRecords(String memberId) {


        LambdaQueryChainWrapper<UsdtBuyOrder> lambdaQuery = lambdaQuery();

        //会员id
        lambdaQuery.eq(UsdtBuyOrder::getMemberId, memberId);

        // 倒序排序
        lambdaQuery.orderByDesc(UsdtBuyOrder::getId);

        //默认查询10条记录
        lambdaQuery.last("LIMIT 10");

        List<UsdtBuyOrder> usdtBuyOrderList = lambdaQuery.list();

        //IPage＜实体＞转 IPage＜Vo＞
        List<UsdtBuyOrderVo> usdtBuyOrderVoList = new ArrayList<>();
        for (UsdtBuyOrder usdtBuyOrder : usdtBuyOrderList) {
            UsdtBuyOrderVo usdtBuyOrderVo = new UsdtBuyOrderVo();
            BeanUtil.copyProperties(usdtBuyOrder, usdtBuyOrderVo);
            usdtBuyOrderVoList.add(usdtBuyOrderVo);
        }

        return usdtBuyOrderVoList;
    }

    /**
     * 查询全部USDT买入记录
     *
     * @param req
     * @return {@link RestResult}<{@link PageReturn}<{@link UsdtBuyOrderVo}>>
     */
    @Override
    public RestResult<PageReturn<UsdtBuyOrderVo>> findAllUsdtPurchaseRecords(PageRequestHome req) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取USDT买入页面数据失败: 获取会员信息失败, req: {}", req);
            return RestResult.failure(ResultCode.RELOGIN);
        }

        if (req == null) {
            req = new PageRequestHome();
        }

        Page<UsdtBuyOrder> pageUsdtBuyOrder = new Page<>();
        pageUsdtBuyOrder.setCurrent(req.getPageNo());
        pageUsdtBuyOrder.setSize(req.getPageSize());

        LambdaQueryChainWrapper<UsdtBuyOrder> lambdaQuery = lambdaQuery();

        //查询当前会员的USDT买入订单
        lambdaQuery.eq(UsdtBuyOrder::getMemberId, memberInfo.getId());

        // 倒序排序
        lambdaQuery.orderByDesc(UsdtBuyOrder::getId);

        baseMapper.selectPage(pageUsdtBuyOrder, lambdaQuery.getWrapper());

        List<UsdtBuyOrder> records = pageUsdtBuyOrder.getRecords();


        ArrayList<UsdtBuyOrderVo> usdtBuyOrderVoList = new ArrayList<>();

        //IPage＜实体＞转 IPage＜Vo＞
        for (UsdtBuyOrder usdtBuyOrder : records) {
            UsdtBuyOrderVo usdtBuyOrderVo = new UsdtBuyOrderVo();
            BeanUtil.copyProperties(usdtBuyOrder, usdtBuyOrderVo);
            usdtBuyOrderVoList.add(usdtBuyOrderVo);
        }

        PageReturn<UsdtBuyOrderVo> flush = PageUtils.flush(pageUsdtBuyOrder, usdtBuyOrderVoList);

        log.info("分页查询全部USDT买入记录: 会员账号: {}, req: {}, 返回数据: {}", memberInfo.getMemberAccount(), req, flush);

        return RestResult.ok(flush);
    }


    @Override
    @SneakyThrows
    public PageReturn<UsdtBuyOrderDTO> listPage(UsdtBuyOrderReq req) {
        Page<UsdtBuyOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<UsdtBuyOrder> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(UsdtBuyOrder::getCreateTime);

        LambdaQueryWrapper<UsdtBuyOrder> queryWrapper = new QueryWrapper<UsdtBuyOrder>()
                .select("IFNULL(sum(usdt_num),0) as usdtNumTotal, IFNULL(sum(arb_num),0) as arbNumTotal, IFNULL(sum(usdt_actual_num), 0) as usdtActualNumTotal").lambda();

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getMemberId())) {
            lambdaQuery.and(e -> e.or().eq(UsdtBuyOrder::getMemberId, req.getMemberId()).or().eq(UsdtBuyOrder::getMemberAccount, req.getMemberId()));
            queryWrapper.and(e -> e.or().eq(UsdtBuyOrder::getMemberId, req.getMemberId()).or().eq(UsdtBuyOrder::getMemberAccount, req.getMemberId()));
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getPlatformOrder())) {
            lambdaQuery.eq(UsdtBuyOrder::getPlatformOrder, req.getPlatformOrder());
            queryWrapper.eq(UsdtBuyOrder::getPlatformOrder, req.getPlatformOrder());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getStatus())) {
            lambdaQuery.eq(UsdtBuyOrder::getStatus, req.getStatus());
            queryWrapper.eq(UsdtBuyOrder::getStatus, req.getStatus());
        }
        if (!ObjectUtils.isEmpty(req.getCreateTimeStart())) {
            lambdaQuery.ge(UsdtBuyOrder::getCreateTime, req.getCreateTimeStart());
            queryWrapper.ge(UsdtBuyOrder::getCreateTime, req.getCreateTimeStart());
        }
        if (!ObjectUtils.isEmpty(req.getCreateTimeEnd())) {
            lambdaQuery.le(UsdtBuyOrder::getCreateTime, req.getCreateTimeEnd());
            queryWrapper.le(UsdtBuyOrder::getCreateTime, req.getCreateTimeEnd());
        }

        CompletableFuture<UsdtBuyOrder> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        Page<UsdtBuyOrder> finalPage = page;
        CompletableFuture<Page<UsdtBuyOrder>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(totalFuture, resultFuture);
        allFutures.get();
        page = resultFuture.get();
        UsdtBuyOrder totalResult = totalFuture.get();
        JSONObject extend = new JSONObject();
        extend.put("usdtNumTotal", totalResult.getUsdtNumTotal().toPlainString());
        extend.put("arbNumTotal", totalResult.getArbNumTotal().toPlainString());
        extend.put("usdtActualNumTotal", totalResult.getUsdtNumTotal().toPlainString());
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<UsdtBuyOrder> records = page.getRecords();
        List<UsdtBuyOrderDTO> list = walletMapStruct.usdtBuyOrderTransform(records);
        BigDecimal usdtNumPageTotal = BigDecimal.ZERO;
        BigDecimal arbNumPageTotal = BigDecimal.ZERO;
        BigDecimal usdtActualNumPageTotal = BigDecimal.ZERO;
        for (UsdtBuyOrderDTO usdtBuyOrderDTO : list) {
            BigDecimal usdtNum = usdtBuyOrderDTO.getUsdtNum() == null ? new BigDecimal(0) : usdtBuyOrderDTO.getUsdtNum();
            BigDecimal arbNum = usdtBuyOrderDTO.getArbNum() == null ? new BigDecimal(0) : usdtBuyOrderDTO.getArbNum();
            BigDecimal usdtActualNum = usdtBuyOrderDTO.getUsdtActualNum();
            usdtNumPageTotal = usdtNumPageTotal.add(usdtNum);
            arbNumPageTotal = arbNumPageTotal.add(arbNum);
            if (ObjectUtils.isEmpty(usdtActualNum)) {
                usdtBuyOrderDTO.setUsdtActualNum(usdtBuyOrderDTO.getUsdtNum());
            }
            usdtActualNumPageTotal = usdtActualNumPageTotal.add(usdtActualNum);

        }
        extend.put("usdtNumPageTotal", usdtNumPageTotal.toPlainString());
        extend.put("arbNumPageTotal", arbNumPageTotal.toPlainString());
        extend.put("usdtActualNumPageTotal", usdtActualNumPageTotal.toPlainString());
        return PageUtils.flush(page, list, extend);
    }

    @Override
    public PageReturn<UsdtBuyOrderExportDTO> listpageForExport(UsdtBuyOrderReq req) {
        PageReturn<UsdtBuyOrderDTO> usdtList = listPage(req);
        List<UsdtBuyOrderExportDTO> result = new ArrayList<>();
        for (UsdtBuyOrderDTO usdtBuyOrderDTO : usdtList.getList()) {
            UsdtBuyOrderExportDTO dto = new UsdtBuyOrderExportDTO();
            BeanUtils.copyProperties(usdtBuyOrderDTO, dto);
            String nameByCode = OrderStatusEnum.getNameByCode(usdtBuyOrderDTO.getStatus());
            dto.setStatus(nameByCode);
            String payType = usdtBuyOrderDTO.getPayType();
            String usdtPayType = UsdtPayTypeEnum.getNameByCode(payType);
            dto.setPayType(usdtPayType);
            result.add(dto);
        }
        Page<UsdtBuyOrderDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(usdtList.getTotal());
        return PageUtils.flush(page, result);
    }

    /**
     * 根据订单号获取买入订单
     *
     * @param platformOrder
     * @return {@link UsdtBuyOrder}
     */
    @Override
    public UsdtBuyOrder getUsdtBuyOrderByPlatformOrder(String platformOrder) {
        return lambdaQuery().eq(UsdtBuyOrder::getPlatformOrder, platformOrder).one();
    }

    /**
     * 获取USDT买入页面数据
     *
     * @return {@link RestResult}<{@link UsdtBuyPageDataVo}>
     */
    @Override
    public RestResult<UsdtBuyPageDataVo> getUsdtBuyPageData() {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取USDT买入页面数据失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        UsdtBuyPageDataVo usdtBuyPageDataVo = new UsdtBuyPageDataVo();
        //主网络列表
        usdtBuyPageDataVo.setNetworkProtocolList(usdtConfigService.getNetworkProtocol());
        //usdt汇率
        usdtBuyPageDataVo.setUsdtCurrency(tradeConfigService.getById(1).getUsdtCurrency());
        //分页查询 USDT 买入记录
        usdtBuyPageDataVo.setUsdtBuyOrder(findPagedUsdtPurchaseRecords(String.valueOf(memberInfo.getId())));

        log.info("获取USDT买入页面数据成功  会员账号: {}, 返回数据: {}", memberInfo.getId(), usdtBuyPageDataVo);

        return RestResult.ok(usdtBuyPageDataVo);

    }

//    /**
//     * USDT完成转账处理
//     *
//     * @param platformOrder
//     * @param voucherImage
//     * @return {@link RestResult}
//     */
//    @Override
//    public RestResult usdtBuyCompleted(String platformOrder, String voucherImage) {
//
//
//        if (!FileUtil.isValidImageExtension(voucherImage)) {
//            // 如果有文件不符合规茨，则返回错误
//            log.error("USDT完成转账处理失败: 会员上传图片文件不符合规范 直接驳回, 订单号: {}, 文件名: {}", platformOrder, voucherImage);
//            return RestResult.failure(ResultCode.FILE_UPLOAD_REQUIRED);
//        }
//
//        //分布式锁key ar-wallet-usdtBuyCompleted+订单号
//        String key = "ar-wallet-usdtBuyCompleted" + platformOrder;
//        RLock lock = redissonUtil.getLock(key);
//
//        boolean req = false;
//
//        try {
//            req = lock.tryLock(10, TimeUnit.SECONDS);
//
//            if (req) {
//
//                //获取当前会员信息
//                MemberInfo memberInfo = memberInfoService.getMemberInfo();
//
//                if (memberInfo == null) {
//                    log.error("USDT完成转账处理失败: 获取会员信息失败, 订单号: {}", platformOrder);
//                    return RestResult.failure(ResultCode.RELOGIN);
//                }
//
//                //获取USDT订单
//                UsdtBuyOrder usdtBuyOrder = getUsdtBuyOrderByPlatformOrder(platformOrder);
//
//                String memberId = String.valueOf(memberInfo.getId());
//
//                //校验该笔订单是否属于该会员
//                if (usdtBuyOrder == null || !usdtBuyOrder.getMemberId().equals(memberId)) {
//                    log.error("USDT完成转账处理失败, 该订单不存在或不属于当前会员, 会员信息: {}, 订单号: {}, USDT订单信息: {}", memberInfo, platformOrder, usdtBuyOrder);
//                    return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
//                }
//
//                //校验订单状态
//                if (!usdtBuyOrder.getStatus().equals(OrderStatusEnum.BE_PAID.getCode())){
//                    log.error("USDT完成转账处理失败, 订单状态校验失败: 当前订单状态为: {}, 会员信息: {}, 订单号: {}, USDT订单信息: {}", usdtBuyOrder.getStatus(), memberInfo, platformOrder, usdtBuyOrder);
//                    return RestResult.failure(ResultCode.ORDER_EXPIRED);
//                }
//
//                //文件校验
////                RestResult validateFile = FileUtil.validateFile(voucherImage, arProperty.getMaxImageFileSize(), "image");
////                if (validateFile != null) {
////                    log.error("USDT完成转账处理: 非法操作: 订单校验失败: {}, platformOrder: {}", validateFile, platformOrder);
////                    return validateFile;
////                }
//
//                //调用阿里云存储服务 将图片上传上去 并获取到文件名
////                String fileName = ossService.uploadFile(voucherImage);
////                if (fileName == null) {
////                    log.error("USDT完成转账处理 阿里云上传文件失败, 会员信息: {}, 订单号: {}", memberInfo, platformOrder);
////                    return RestResult.failure(ResultCode.FILE_UPLOAD_FAILED);
////                }
//
//                //更新USDT订单信息
//                boolean update = lambdaUpdate()
//                        .eq(UsdtBuyOrder::getPlatformOrder, platformOrder)
//                        .set(UsdtBuyOrder::getUsdtProof, baseUrl + voucherImage)//更新usdt支付凭证
//                        .set(UsdtBuyOrder::getStatus, OrderStatusEnum.CONFIRMATION.getCode())//更新USDT订单状态为确认中
//                        .set(UsdtBuyOrder::getPaymentTime, LocalDateTime.now())//更新支付时间
//                        .update();
//
//
//                log.info("USDT完成转账处理成功 会员账号: {}, 订单信息: {}, sql执行结果: {}", memberInfo.getAppealCount(), usdtBuyOrder, update);
//
//                return RestResult.ok();
//            }
//        } catch (Exception e) {
//            log.error("USDT完成转账处理失败: 订单号: {}, e: {}", platformOrder, e);
//        } finally {
//            //释放锁
//            if (req && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
//        }
//        log.error("USDT完成转账处理失败: 订单号: {}", platformOrder);
//        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
//    }

    /**
     * 根据会员id 查看进行中的USDT订单数量
     *
     * @param memberId
     */
    @Override
    public UsdtBuyOrder countActiveUsdtBuyOrders(String memberId) {

        return lambdaQuery()
                .eq(UsdtBuyOrder::getStatus, OrderStatusEnum.BE_PAID.getCode())//待支付
                .eq(UsdtBuyOrder::getMemberId, memberId).last("LIMIT 1").one();
    }

    /**
     * 获取会员待支付的USDT买入订单
     *
     * @param memberId
     * @return {@link UsdtBuyOrder}
     */
    @Override
    public UsdtBuyOrder getPendingUsdtBuyOrder(Long memberId) {
        return lambdaQuery().eq(UsdtBuyOrder::getMemberId, memberId).eq(UsdtBuyOrder::getStatus, OrderStatusEnum.BE_PAID.getCode()).one();
    }


    /**
     * 获取USDT买入订单详情
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link UsdtPurchaseOrderDetailsVo}>
     */
    @Override
    public RestResult<UsdtPurchaseOrderDetailsVo> getUsdtPurchaseOrderDetails(PlatformOrderReq platformOrderReq) {


        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取USDT买入订单详情失败: 获取会员信息失败: {}", memberInfo);
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //查询USDT订单
        UsdtBuyOrder usdtBuyOrderByPlatformOrder = getUsdtBuyOrderByPlatformOrder(platformOrderReq.getPlatformOrder());

        if (usdtBuyOrderByPlatformOrder == null) {
            log.error("获取USDT买入订单详情失败: 获取订单信息失败: req: {}, 会员信息: {}", platformOrderReq, memberInfo);
            return RestResult.failure(ResultCode.ORDER_NOT_EXIST);
        }

        UsdtPurchaseOrderDetailsVo usdtPurchaseOrderDetailsVo = new UsdtPurchaseOrderDetailsVo();

        //赋值给vo
        BeanUtils.copyProperties(usdtBuyOrderByPlatformOrder, usdtPurchaseOrderDetailsVo);

        return RestResult.ok(usdtPurchaseOrderDetailsVo);
    }

    @Override
    public PageReturn<UsdtBuySuccessOrderDTO> successOrderListPage(UsdtBuyOrderReq req) {
        Page<UsdtBuyOrder> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(UsdtBuyOrder::getPaymentTime)
                .eq(UsdtBuyOrder::getStatus, OrderStatusEnum.SUCCESS.getCode())
                .eq(UsdtBuyOrder::getUsdtAddr, req.getUsdtAddr())
                .eq(StringUtils.isNotEmpty(req.getPlatformOrder()), UsdtBuyOrder::getPlatformOrder, req.getPlatformOrder())
                .eq(StringUtils.isNotEmpty(req.getMemberUsdtAddr()), UsdtBuyOrder::getFromAddress, req.getMemberUsdtAddr())
                .page(page);

        List<UsdtBuySuccessOrderDTO> list = page.getRecords().stream()
                .map(this::convertToSuccessOrderDTO)
                .collect(Collectors.toList());

        return PageUtils.flush(page, list);
    }

    @Override
    @Transactional
    public RestResult<UsdtBuyOrderDTO> pay(UsdtBuyOrderIdReq req) {
        try{
            UsdtBuyOrder usdtBuyOrder = getById(req.getId());
            Boolean checkFill = iRechargeTronDetailService.queryAndFillOrderId(usdtBuyOrder.getPlatformOrder(), usdtBuyOrder.getUsdtAddr(), usdtBuyOrder.getUsdtActualNum());
            if(checkFill){
                if (usdtBuyOrder.getStatus().equals(OrderStatusEnum.SUCCESS.getCode())) {
                    return RestResult.failed();
                }
                usdtBuyOrder.setStatus(OrderStatusEnum.SUCCESS.getCode());
                usdtBuyOrder.setUpdateBy(UserContext.getCurrentUserName());
                usdtBuyOrder.setRemark(req.getRemark());
                baseMapper.updateById(usdtBuyOrder);
                amountChangeUtil.insertMemberChangeAmountRecord(usdtBuyOrder.getMemberId(), usdtBuyOrder.getArbActualNum(), ChangeModeEnum.ADD, CurrenceEnum.ARB.getCode(), usdtBuyOrder.getPlatformOrder(), MemberAccountChangeEnum.USDT_RECHARGE, usdtBuyOrder.getUpdateBy(), "3");
                UsdtBuyOrderDTO usdtBuyOrderInfoDTO = new UsdtBuyOrderDTO();
                BeanUtils.copyProperties(usdtBuyOrder, usdtBuyOrderInfoDTO);
                return RestResult.ok(usdtBuyOrderInfoDTO);
            }
            return RestResult.failed("usdt order has no checked");
        }catch (Exception e){
            System.err.println(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return RestResult.failed("pay failed");
        }
    }

    private UsdtBuySuccessOrderDTO convertToSuccessOrderDTO(UsdtBuyOrder usdtBuyOrder) {
        return UsdtBuySuccessOrderDTO.builder()
                .paymentTime(usdtBuyOrder.getPaymentTime())
                .id(usdtBuyOrder.getId())
                .txid(usdtBuyOrder.getTxid())
                .usdtAddr(usdtBuyOrder.getUsdtAddr())
                .memberUsdtAddr(usdtBuyOrder.getFromAddress())
                .platformOrder(usdtBuyOrder.getPlatformOrder())
                .usdtNum(usdtBuyOrder.getUsdtNum())
                .memberId(usdtBuyOrder.getMemberId())
                .build();
    }
}
