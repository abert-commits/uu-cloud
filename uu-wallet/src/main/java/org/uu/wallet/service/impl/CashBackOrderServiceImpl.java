package org.uu.wallet.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CashBackOrderApiDTO;
import org.uu.common.pay.dto.CashBackOrderListPageDTO;
import org.uu.common.pay.req.CashBackOrderListPageReq;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.AccountChangeEnum;
import org.uu.wallet.Enum.CashBackOrderStatusEnum;
import org.uu.wallet.Enum.ChangeModeEnum;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.CashBackOrderMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.req.ApiCashBackReq;
import org.uu.wallet.req.ApiCashBackRequest;
import org.uu.wallet.service.ICashBackOrderService;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.util.RequestUtil;
import org.uu.wallet.util.RsaUtil;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 退回订单表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2024-05-09
 */
@Service
@Slf4j
public class CashBackOrderServiceImpl extends ServiceImpl<CashBackOrderMapper, CashBackOrder> implements ICashBackOrderService {

    @Resource
    RedissonUtil redissonUtil;

    @Resource
    private MerchantInfoMapper merchantInfoMapper;

    @Resource
    private AmountChangeUtil amountChangeUtil;

    @Resource
    private MemberInfoMapper memberInfoMapper;

    @Resource
    private ArProperty arProperty;

    @Resource
    private IMerchantInfoService merchantInfoService;

    @Override
    public CashBackOrder getCashBackOrder(String orderNo) {
        LambdaQueryChainWrapper<CashBackOrder> queryChainWrapper = lambdaQuery().eq(CashBackOrder::getPlatformOrder, orderNo);
        return baseMapper.selectOne(queryChainWrapper.getWrapper());
    }

    @Override
    public CashBackOrder getProcessingCashBackOrderByMemberId(Long memberId) {
        LambdaQueryChainWrapper<CashBackOrder> queryChainWrapper = lambdaQuery().eq(CashBackOrder::getMemberId, memberId).eq(CashBackOrder::getOrderStatus, CashBackOrderStatusEnum.CASH_BACK_PROCESSING);
        return baseMapper.selectOne(queryChainWrapper.getWrapper());
    }

    @Override
    public CashBackOrder getCashBackOrderByMemberIdAndOrderStatus(Long memberId, String orderStatus) {
        LambdaQueryChainWrapper<CashBackOrder> queryChainWrapper = lambdaQuery().eq(CashBackOrder::getMemberId, memberId).eq(CashBackOrder::getOrderStatus, orderStatus);
        return baseMapper.selectOne(queryChainWrapper.getWrapper());
    }

    @Deprecated
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean cashBack(String orderNo, String processUserName) {
        String key = "uu-wallet-cashBack" + orderNo;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;
        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {
                // 检查订单状态
                log.info("余额退回-退回订单 {}", orderNo);
                CashBackOrder cashBackOrder = getCashBackOrder(orderNo);
                log.info("余额退回-退回订单结果 {}", cashBackOrder);
                String currentOrderStatus = cashBackOrder.getOrderStatus();
                if (!currentOrderStatus.equals(CashBackOrderStatusEnum.CASH_BACK_PROCESSING.getCode())) {
                    throw new Exception("余额退回订单处理失败 订单: {"+cashBackOrder+"}, 订单状态异常: {"+currentOrderStatus+"}");
                }
                // 检查订单金额和冻结金额是否一致
                BigDecimal orderAmount = cashBackOrder.getAmount();
                MemberInfo memberInfo = memberInfoMapper.getMemberInfoByIdForUpdate(cashBackOrder.getMemberId());
                BigDecimal cashBackFrozenAmount = ObjectUtils.isEmpty(memberInfo.getCashBackFrozenAmount()) ? BigDecimal.ZERO : memberInfo.getCashBackFrozenAmount();
                // 如果冻结金额和退回金额不一致
                if (orderAmount.compareTo(cashBackFrozenAmount) != 0) {
                    throw new Exception("余额退回订单处理失败 订单: {"+cashBackOrder+"}, 订单金额异常: {"+cashBackFrozenAmount+"}" );
                }
                // 检查商户余额
                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoByCode(cashBackOrder.getMerchantCode());
                log.info("余额退回-商户信息{}", merchantInfo);
                BigDecimal merchantCashBackFrozenAmount = ObjectUtils.isEmpty(merchantInfo.getCashBackFrozenAmount()) ? BigDecimal.ZERO : merchantInfo.getCashBackFrozenAmount();
                if (merchantCashBackFrozenAmount.compareTo(cashBackFrozenAmount) < 0) {
                    throw new Exception("余额退回订单处理失败 订单: {"+cashBackOrder+"}, 商户退回金额异常: {"+merchantCashBackFrozenAmount+"}");
                }
                long requestTimestamp = System.currentTimeMillis();
                CashBackOrderApiDTO merchantOrder = cashBackApi(orderNo, cashBackOrder.getMerchantMemberId(), orderAmount, merchantInfo.getCode(), merchantInfo.getMerchantPublicKey());
                long responseTimestamp = System.currentTimeMillis();
                String time = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), "yyyy-MM-dd");
                // 清除会员冻结金额
                BigDecimal memberBalance = memberInfo.getBalance();
                BigDecimal afterMemberBalance = memberBalance.add(orderAmount);
                memberInfo.setBalance(afterMemberBalance);
                memberInfo.setCashBackFrozenAmount(BigDecimal.ZERO);
                memberInfoMapper.updateById(memberInfo);
                // 清除商户冻结金额
                BigDecimal merchantBalance = merchantInfo.getBalance();
                BigDecimal afterMerchantBalance = merchantBalance.add(orderAmount);
                BigDecimal afterCashBackFrozenAmount = merchantCashBackFrozenAmount.subtract(orderAmount);
                merchantInfo.setBalance(afterMerchantBalance);
                merchantInfo.setCashBackFrozenAmount(afterCashBackFrozenAmount);
                merchantInfoMapper.updateById(merchantInfo);
                String cashBackOrderStatus = CashBackOrderStatusEnum.CASH_BACK_FAILED.getCode();

                if (ObjectUtil.isNotEmpty(merchantOrder.getOrderNo())) {
                    cashBackOrder.setMerchantOrder(merchantOrder.getOrderNo());
                    cashBackOrderStatus = CashBackOrderStatusEnum.CASH_BACK_SUCCESS.getCode();
                    // 成功设置成功时间
                    cashBackOrder.setCompletionTime(LocalDateTime.now());
                    // 扣除会员对应余额 并新增对应余额退回账变记录
//                    amountChangeUtil.insertCashBackMemberChangeAmountRecord(memberInfo.getId().toString(), orderAmount, ChangeModeEnum.SUB, "ARB", orderNo, MemberAccountChangeEnum.CASH_BACK, processUserName, merchantOrder.getOrderNo());
                    // 设置用户弹窗提醒
                    MemberInfo memberUpdate = new MemberInfo();
                    memberUpdate.setCashBackAttention(1);
                    memberUpdate.setId(memberInfo.getId());
                    memberInfoMapper.updateById(memberUpdate);
                } else {
                    cashBackOrder.setFailedTime(LocalDateTime.now());
                    cashBackOrder.setFailedReason(merchantOrder.getRemark());
                }
                // 更新状态
                cashBackOrder.setOrderStatus(cashBackOrderStatus);
                String completeDuration = String.valueOf(responseTimestamp - requestTimestamp);
                cashBackOrder.setCompleteDuration(completeDuration);
                cashBackOrder.setRequestTimestamp(String.valueOf(requestTimestamp));
                cashBackOrder.setResponseTimestamp(String.valueOf(responseTimestamp));
                baseMapper.updateById(cashBackOrder);
                return true;
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("余额退回订单处理失败 订单号: {}, 报错信息: {}", orderNo, e.getMessage());
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean generateOrder(String thOrder, BigDecimal amount, String memberId, String merchantName, String merchantCode, String merchantMemberId) {
        // 生成订单
        CashBackOrder order = new CashBackOrder();
        order.setPlatformOrder(thOrder);
        order.setOrderStatus(CashBackOrderStatusEnum.CASH_BACK_PROCESSING.getCode());
        order.setAmount(amount);
        order.setMemberId(memberId);
        order.setMerchantCode(merchantCode);
        order.setMerchantName(merchantName);
        // 截取商户
        merchantMemberId = merchantMemberId.substring(merchantCode.length());
        order.setMerchantMemberId(merchantMemberId);
        int insert = baseMapper.insert(order);
        return insert > 0;
    }

    @Override
    public CashBackOrderApiDTO cashBackApi(String thOrder, String merchantMemberId, BigDecimal amount, String merchantCode, String merchantPublicKeyStr) {
        CashBackOrderApiDTO dto = new CashBackOrderApiDTO();
        dto.setOrderNo(null);
        dto.setResult(false);
        ApiCashBackReq request = new ApiCashBackReq();
        request.setMerchantOrder(thOrder);
        request.setUserId(merchantMemberId);
        request.setMerchantCode(merchantCode);
        request.setAmount(String.valueOf(amount));


        try {
            //商户公钥
            PublicKey merchantPublicKey = RsaUtil.getPublicKeyFromString(merchantPublicKeyStr);

            //平台私钥
            PrivateKey platformPrivateKey = RsaUtil.getPrivateKeyFromString(arProperty.getPrivateKey());

            EncryptedData encryptedData = RsaUtil.signAndEncryptData(request, platformPrivateKey, merchantPublicKey);

            ApiCashBackRequest apiCashBackRequest = new ApiCashBackRequest();
            BeanUtils.copyProperties(encryptedData, apiCashBackRequest);
            apiCashBackRequest.setMerchantCode(merchantCode);
            apiCashBackRequest.setSignature(RsaUtil.generateSign(request, platformPrivateKey));
            UUID uuid = UUID.randomUUID();
            String random32 = uuid.toString().replace("-", "");
            apiCashBackRequest.setRandom(random32);
            long currentTimeSeconds = System.currentTimeMillis() / 1000;
            apiCashBackRequest.setTimestamp(String.valueOf(currentTimeSeconds));

            log.info("余额退回--请求参数明文：{} ; 请求参数密文：{}", request, JSON.toJSONString(apiCashBackRequest));
            //发送请求
            MerchantInfo merchantInfoByCode = merchantInfoService.getMerchantInfoByCode(merchantCode);
            if (merchantInfoByCode == null || merchantInfoByCode.getApiUrl() == null) {
                log.info("余额退回--请求地址获失败：{} , 商户code：{}, 订单号：{}", merchantInfoByCode, merchantCode, thOrder);
                return dto;
            }
            String url = merchantInfoByCode.getApiUrl();
            String path = arProperty.getCashBackUrl();
            url += path;
            log.info("余额退回--请求地址获：{} , 商户code：{}, 订单号：{}", url, merchantCode, thOrder);
            if(ObjectUtils.isEmpty(url)){
                log.info("余额退回--请求地址获失败：{} , 商户code：{}, 订单号：{}", url, merchantCode, thOrder);
                return dto;
            }
            String res = RequestUtil.HttpRestClientToJson(url, JSON.toJSONString(apiCashBackRequest));
            if (StringUtils.isNotEmpty(res) && JSONUtil.isJson(res)) {
                JSONObject jsonObject = JSON.parseObject(res);
                if (jsonObject.containsKey("code")
                        && jsonObject.containsKey("data")
                        && "0".equals(jsonObject.getString("code"))
                ) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    if (data.containsKey("status")
                            && "1".equals(data.getString("status"))
                            && data.containsKey("merchantOrderNo")
                    ) {
                        dto.setResult(true);
                        dto.setRemark("success");
                        dto.setOrderNo(data.getString("merchantOrderNo"));
                        return dto;
                    }
                }
                String msg = jsonObject.containsKey("msg") ? jsonObject.getString("msg") : "";
                dto.setRemark(msg);
                log.error("余额退回--返回数据: {}", res);
                return dto;
            }
        } catch (Exception e) {
            return dto;
        }
        return dto;
    }

    @Override
    public PageReturn<CashBackOrderListPageDTO> listPage(CashBackOrderListPageReq req) throws ExecutionException, InterruptedException {
        Page<CashBackOrder> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<CashBackOrder> lambdaQuery = lambdaQuery();
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<CashBackOrder> queryWrapper = new QueryWrapper<CashBackOrder>()
                .select("IFNULL(sum(amount), 0) as amountTotal").lambda();

        lambdaQuery.orderByDesc(CashBackOrder::getId);

        if (StringUtils.isNotBlank(req.getMemberId())) {
            lambdaQuery.eq(CashBackOrder::getMemberId, req.getMemberId());
            queryWrapper.eq(CashBackOrder::getMemberId, req.getMemberId());
        }

        if (StringUtils.isNotBlank(req.getMerchantOrder())) {
            lambdaQuery.eq(CashBackOrder::getMerchantOrder, req.getMerchantOrder());
            queryWrapper.eq(CashBackOrder::getMerchantOrder, req.getMerchantOrder());
        }

        if (StringUtils.isNotBlank(req.getPlatformOrder())) {
            lambdaQuery.eq(CashBackOrder::getPlatformOrder, req.getPlatformOrder());
            queryWrapper.eq(CashBackOrder::getPlatformOrder, req.getPlatformOrder());
        }

        if (StringUtils.isNotBlank(req.getOrderStatus())) {
            lambdaQuery.eq(CashBackOrder::getOrderStatus, req.getOrderStatus());
            queryWrapper.eq(CashBackOrder::getOrderStatus, req.getOrderStatus());
        }

        if (ObjectUtils.isNotEmpty(req.getStartTime())) {
            lambdaQuery.ge(CashBackOrder::getCreateTime, req.getStartTime());
            queryWrapper.ge(CashBackOrder::getCreateTime, req.getStartTime());
        }

        if (ObjectUtils.isNotEmpty(req.getEndTime())) {
            lambdaQuery.le(CashBackOrder::getCreateTime, req.getEndTime());
            queryWrapper.le(CashBackOrder::getCreateTime, req.getEndTime());
        }

        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(CashBackOrder::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(CashBackOrder::getMerchantCode, req.getMerchantCode());
        }

        if (StringUtils.isNotBlank(req.getMerchantMemberId())) {
            lambdaQuery.eq(CashBackOrder::getMerchantMemberId, req.getMerchantMemberId());
            queryWrapper.eq(CashBackOrder::getMerchantMemberId, req.getMerchantMemberId());
        }


        Page<CashBackOrder> finalPage = page;
        CompletableFuture<CashBackOrder> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<CashBackOrder>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        CashBackOrder totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();

        extent.put("amountTotal", totalInfo.getAmountTotal().toPlainString());
        BigDecimal amountPageTotal = BigDecimal.ZERO;

        List<CashBackOrder> records = page.getRecords();

        ArrayList<CashBackOrderListPageDTO> list = new ArrayList<>();
        for (CashBackOrder record : records) {
            CashBackOrderListPageDTO cashBackOrderListPageDTO = new CashBackOrderListPageDTO();
            BeanUtils.copyProperties(record, cashBackOrderListPageDTO);

            amountPageTotal = amountPageTotal.add(record.getAmount());
            list.add(cashBackOrderListPageDTO);
        }
        extent.put("amountPageTotal", amountPageTotal.toPlainString());
        return PageUtils.flush(page, list, extent);
    }
}
