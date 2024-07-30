package org.uu.wallet.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.h2.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.SecurityConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.AssertUtil;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.exception.BizException;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.*;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.*;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.util.DurationCalculatorUtil;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.vo.BuyListVo;
import org.uu.wallet.vo.MerchantNameListVo;
import org.uu.wallet.vo.OrderInfoVo;
import org.uu.wallet.webSocket.MemberSendAmountList;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * @author Admin
 */
@Service
@RequiredArgsConstructor
public class MerchantInfoServiceImpl extends ServiceImpl<MerchantInfoMapper, MerchantInfo> implements IMerchantInfoService {

    @Resource
    private WalletMapStruct walletMapStruct;
    @Resource
    private PaymentOrderMapper paymentOrderMapper;
    @Resource
    private CollectionOrderMapper collectionOrderMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ITradeConfigService iTradeConfigService;
    @Resource
    private MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    @Resource
    private MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;
    @Resource
    private MatchingOrderMapper matchingOrderMapper;
    @Resource
    private MemberInfoMapper memberInfoMapper;
    @Resource
    private ITradeConfigService tradeConfigService;
    @Resource
    private UsdtBuyOrderMapper usdtBuyOrderMapper;
    @Resource
    AsyncNotifyService asyncNotifyService;
    @Resource
    private IMerchantRatesConfigService merchantRatesConfigService;
    @Resource
    IRechargeTronDetailService iRechargeTronDetailService;
    @Resource
    RabbitMQService rabbitMQService;
    @Resource
    @Lazy
    AmountChangeUtil amountChangeUtil;
    @Autowired
    private TronAddressMapper tronAddressMapper;
    @Resource
    @Lazy
    MerchantInfoMapper merchantInfoMapper;
    @Resource
    @Lazy
    UsdtPaymentOrderService usdtPaymentOrderService;
    @Resource
    @Lazy
    TrxPaymentOrderService trxPaymentOrderService;

    @Resource
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;
    @Resource
    private IMerchantCollectOrdersService merchantCollectOrdersService;
    @Resource
    private IApplyDistributedService applyDistributedService;
    @Resource
    @Lazy
    MemberSendAmountList memberSendAmountList;
    @Resource
    @Lazy
    RedisUtil redisUtil;

    @Override
    public PageReturn<MerchantInfoListPageDTO> listPage(MerchantInfoListPageReq req) throws ExecutionException, InterruptedException {
        Page<MerchantInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MerchantInfo> lambdaQuery = lambdaQuery();
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<MerchantInfo> queryWrapper = new QueryWrapper<MerchantInfo>()
                .select("IFNULL(sum(balance), 0) as balanceTotal," +
                        "IFNULL(sum(usdt_balance), 0) as usdtBalanceTotal," +
                        "IFNULL(sum(trx_balance), 0) as trxBalanceTotal"

                ).lambda();
        lambdaQuery.orderByDesc(MerchantInfo::getCreateTime);

        if (CollectionUtils.isNotEmpty(req.getMerchantCodes())) {
            lambdaQuery.in(MerchantInfo::getCode, req.getMerchantCodes());
            queryWrapper.in(MerchantInfo::getCode, req.getMerchantCodes());
        }
        if (!StringUtils.isNullOrEmpty(req.getCurrency())) {
            lambdaQuery.eq(MerchantInfo::getCurrency, req.getCurrency());
            queryWrapper.eq(MerchantInfo::getCurrency, req.getCurrency());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getCode())) {
            lambdaQuery.eq(MerchantInfo::getCode, req.getCode());
            queryWrapper.eq(MerchantInfo::getCode, req.getCode());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getUsername())) {
            lambdaQuery.eq(MerchantInfo::getUsername, req.getUsername());
            queryWrapper.eq(MerchantInfo::getUsername, req.getUsername());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getMerchantType())) {
            lambdaQuery.eq(MerchantInfo::getMerchantType, req.getMerchantType());
            queryWrapper.eq(MerchantInfo::getMerchantType, req.getMerchantType());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(req.getStatus()) && (req.getStatus().equals("0"))) {
            lambdaQuery.eq(MerchantInfo::getRechargeStatus, 0);
            queryWrapper.eq(MerchantInfo::getRechargeStatus, 0);
        } else if (org.apache.commons.lang3.StringUtils.isNotBlank(req.getStatus()) && (req.getStatus().equals("1"))) {
            lambdaQuery.eq(MerchantInfo::getRechargeStatus, 1);
            queryWrapper.eq(MerchantInfo::getRechargeStatus, 1);
        } else if (org.apache.commons.lang3.StringUtils.isNotBlank(req.getStatus()) && (req.getStatus().equals("2"))) {
            lambdaQuery.eq(MerchantInfo::getWithdrawalStatus, 0);
            queryWrapper.eq(MerchantInfo::getWithdrawalStatus, 0);
        } else if (org.apache.commons.lang3.StringUtils.isNotBlank(req.getStatus()) && (req.getStatus().equals("3"))) {
            lambdaQuery.eq(MerchantInfo::getWithdrawalStatus, 1);
            queryWrapper.eq(MerchantInfo::getWithdrawalStatus, 1);
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getRechargeStatus())) {
            lambdaQuery.eq(MerchantInfo::getRechargeStatus, req.getRechargeStatus());
            queryWrapper.eq(MerchantInfo::getRechargeStatus, req.getRechargeStatus());
        }
        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getWithdrawalStatus())) {
            lambdaQuery.eq(MerchantInfo::getWithdrawalStatus, req.getWithdrawalStatus());
            queryWrapper.eq(MerchantInfo::getWithdrawalStatus, req.getWithdrawalStatus());
        }
        // 获取阈值
        TradeConfig tradeConfig = tradeConfigService.getById(1);
        BigDecimal warningBalance = tradeConfig.getWarningBalance();

        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getRiskTag())) {
            if (Objects.equals(req.getRiskTag(), RiskTagEnum.INSUFFICIENT_BALANCE.getCode())) {
                lambdaQuery.le(MerchantInfo::getBalance, warningBalance);
                queryWrapper.le(MerchantInfo::getBalance, warningBalance);
            } else if (Objects.equals(req.getRiskTag(), RiskTagEnum.Normal.getCode())) {
                lambdaQuery.ge(MerchantInfo::getBalance, warningBalance);
                queryWrapper.ge(MerchantInfo::getBalance, warningBalance);
            } else {
                lambdaQuery.eq(MerchantInfo::getId, -1);
                queryWrapper.eq(MerchantInfo::getId, -1);
            }
        }
        CompletableFuture<Page<MerchantInfo>> merchantListFuture = CompletableFuture.supplyAsync(() -> {
            return baseMapper.selectPage(page, lambdaQuery.getWrapper());
        });

        CompletableFuture<MerchantInfo> merchantTotalFuture = CompletableFuture.supplyAsync(() -> {
            return baseMapper.selectOne(queryWrapper);
        });

        CompletableFuture<List<MerchantActivationInfoDTO>> merchantActivationInfo = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMerchantInfoList();
        });

        CompletableFuture<BigDecimal> memberBalanceTotal = CompletableFuture.supplyAsync(() -> {
            return memberInfoMapper.selectMemberTotalBalance();
        });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(merchantListFuture, merchantActivationInfo, merchantTotalFuture, memberBalanceTotal);
        allFutures.get();
        MerchantInfo merchantTotalInfo = merchantTotalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("balanceTotal", merchantTotalInfo.getBalanceTotal().toPlainString());
        extent.put("usdtBalanceTotal", merchantTotalInfo.getUsdtBalanceTotal().toPlainString());
        extent.put("trxBalanceTotal", merchantTotalInfo.getTrxBalanceTotal().toPlainString());
        List<MerchantInfo> records = merchantListFuture.get().getRecords();
        List<MerchantInfoListPageDTO> list = walletMapStruct.merchantInfoListTransform(records);

        // 从 list 中提取所有的 merchantId
        List<String> merchantCodes = list.stream()
                .map(MerchantInfoListPageDTO::getCode)
                .collect(Collectors.toList());
        List<Integer> paymentTypes = Arrays.asList(1, 2);

        if (CollectionUtils.isNotEmpty(merchantCodes)) {
            Map<String, Map<Integer, List<MerchantPayMentDTO>>> paymentMap = merchantRatesConfigService.getMerchantPayMentDTOs(merchantCodes, paymentTypes);

            // 将查询结果设置到每个 MerchantInfoListPageDTO 中
            list.forEach(dto -> {
                dto.setCollectList(paymentMap.getOrDefault(dto.getCode(), new HashMap<>()).getOrDefault(1, null));
                dto.setPaymentList(paymentMap.getOrDefault(dto.getCode(), new HashMap<>()).getOrDefault(2, null));
            });
        }


        BigDecimal balancePageTotal = BigDecimal.ZERO;
        BigDecimal memberBalancePageTotal = BigDecimal.ZERO;
        BigDecimal usdtBalancePageTotal = BigDecimal.ZERO;
        BigDecimal trxBalancePageTotal = BigDecimal.ZERO;
        for (MerchantInfoListPageDTO item : list) {
            item.setRiskTag(RiskTagEnum.Normal.getCode());
            if (item.getBalance().compareTo(warningBalance) <= 0) {
                item.setRiskTag(RiskTagEnum.INSUFFICIENT_BALANCE.getCode());
            }
            for (MerchantActivationInfoDTO innerItem : merchantActivationInfo.get()) {
                if (item.getCode().equals(innerItem.getMerchantCode())) {
                    item.setMemberTotalBalance(innerItem.getBalance());
                    item.setMemberTotalNum(innerItem.getActivationTotalNum());
                }
            }
            memberBalancePageTotal = memberBalancePageTotal.add(item.getMemberTotalBalance());
            balancePageTotal = balancePageTotal.add(item.getBalance());
            BigDecimal usdtBalance = item.getUsdtBalance();
            if (ObjectUtils.isEmpty(usdtBalance)) {
                usdtBalance = BigDecimal.ZERO;
            }
            BigDecimal trxBalance = item.getTrxBalance();
            if (ObjectUtils.isEmpty(trxBalance)) {
                trxBalance = BigDecimal.ZERO;
            }
            usdtBalancePageTotal = usdtBalancePageTotal.add(usdtBalance);
            trxBalancePageTotal = trxBalancePageTotal.add(trxBalance);
        }
        extent.put("balancePageTotal", balancePageTotal.toPlainString());
        extent.put("memberBalancePageTotal", memberBalancePageTotal.toPlainString());
        extent.put("trxBalancePageTotal", trxBalancePageTotal.toPlainString());
        extent.put("usdtBalancePageTotal", usdtBalancePageTotal.toPlainString());
        return PageUtils.flush(page, list, extent);
    }

    @Override
    public List<MerchantInfo> getAllMerchantByStatus() {
        LambdaQueryChainWrapper<MerchantInfo> lambdaQuery = lambdaQuery();
        List<MerchantInfo> list = lambdaQuery().eq(MerchantInfo::getStatus, "1").list();
        return list;
    }


    @Override
    public String getMd5KeyByCode(String merchantCode) {
        QueryWrapper<MerchantInfo> MerchantInfoQueryWrapper = new QueryWrapper<>();
        MerchantInfoQueryWrapper.select("md5_key").eq("code", merchantCode);
        return getOne(MerchantInfoQueryWrapper).getMd5Key();
    }


    @Override
    public boolean getIp(String code, String addr) {
        MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getCode, code).one();
        if (merchantInfo != null && !StringUtils.isNullOrEmpty(merchantInfo.getWhiteList())) {
            String whiteStr = merchantInfo.getWhiteList();
            List<String> list = Arrays.asList(",");
            if (list.contains(addr)) return true;
        }
        return false;
    }

    @Override
    public MerchantInfo getMerchantInfoByCode(String code) {
        MerchantInfo merchantInfo = lambdaQuery()
                .eq(MerchantInfo::getCode, code)
                .eq(MerchantInfo::getDeleted, 0)
                .one();
        return merchantInfo;
    }

    @Override
    public MerchantInfo getMerchantInfoByCodeForUpdate(String code) {
        MerchantInfo merchantInfo = lambdaQuery()
                .eq(MerchantInfo::getCode, code)
                .eq(MerchantInfo::getDeleted, 0)
                .last(" for update")
                .one();
        return merchantInfo;
    }

    @Override
    public MerchantInfo getMerchantInfoByCode(String code, String name) {
        MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getCode, code).or().eq(MerchantInfo::getUsername, name).one();
        return merchantInfo;
    }


    @Override
    public List<MerchantNameListVo> getMerchantNameList() {
        //获取当前商户id
        Long currentUserId = UserContext.getCurrentUserId();
        //查询当前商户名称和商户号
        QueryWrapper<MerchantInfo> merchantInfoQueryWrapper = new QueryWrapper<>();
        merchantInfoQueryWrapper.select("code", "username").eq("id", currentUserId);
        List<Map<String, Object>> maps = listMaps(merchantInfoQueryWrapper);
        ArrayList<MerchantNameListVo> merchantNameListVos = new ArrayList<>();

        for (Map<String, Object> map : maps) {
            MerchantNameListVo merchantNameListVo = new MerchantNameListVo();
            merchantNameListVo.setValue(String.valueOf(map.get("code")));
            merchantNameListVo.setLabel(String.valueOf(map.get("username")));
            merchantNameListVos.add(merchantNameListVo);
        }
        return merchantNameListVos;
    }


    @Override
    public MerchantInfoDTO currentMerchantInfo(Long userId) {

        TradeConfigListPageReq tradeConfigReq = new TradeConfigListPageReq();
        tradeConfigReq.setPageNo(1L);
        tradeConfigReq.setPageSize(10L);
        PageReturn<TradeConfigDTO> payConfigPage = iTradeConfigService.listPage(tradeConfigReq);
        List<TradeConfigDTO> list = payConfigPage.getList();
        TradeConfigDTO tradeConfigDTO = list.get(0);
        MerchantInfo merchantInfo = userDetail(userId);
        AssertUtil.notEmpty(merchantInfo, ResultCode.USERNAME_OR_PASSWORD_ERROR);
        // 查询绑定的菜单

        MerchantInfoDTO merchantInfoDTO = new MerchantInfoDTO();
        BeanUtils.copyProperties(merchantInfo, merchantInfoDTO);
        merchantInfoDTO.setUsdtRate(tradeConfigDTO.getUsdtCurrency());

        return merchantInfoDTO;
    }


    @Override
    public MerchantInfo userDetail(Long userId) {
        MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getId, userId).one();
        AssertUtil.notEmpty(merchantInfo, ResultCode.USERNAME_OR_PASSWORD_ERROR);

        // 查询绑定的角色IDs


        return merchantInfo;
    }


    @Override
    public UserAuthDTO getByUsername(String username) {
        String lastLoginIp = (String) redisUtils.hget(SecurityConstants.LOGIN_USER_NAME + username, SecurityConstants.LOGIN_LAST_LOGIN_IP);
        String lastLoginTime = (String) redisUtils.hget(SecurityConstants.LOGIN_USER_NAME + username, SecurityConstants.LOGIN_LAST_LOGIN_TIME);
        Integer loginCount = (Integer) redisUtils.hget(SecurityConstants.LOGIN_USER_NAME + username, SecurityConstants.LOGIN_COUNT);
        UserAuthDTO userAuthDTO = this.baseMapper.getByMerchantCode(username);
        MerchantInfo merchantInfo = new MerchantInfo();
        if (!ObjectUtils.isEmpty(userAuthDTO)) {
            merchantInfo.setId(userAuthDTO.getUserId());
            merchantInfo.setLastLoginTime(DateUtil.parseLocalDateTime(lastLoginTime, GlobalConstants.DATE_FORMAT));
            merchantInfo.setLoginIp(lastLoginIp);
            merchantInfo.setLogins(loginCount);
            this.baseMapper.updateById(merchantInfo);
        }
        return userAuthDTO;
    }

    /*
     * 根据商户号查询支付费率和代付费率
     * */
    @Override
    public Map<String, Object> getRateByCode(String merchantCode) {
        QueryWrapper<MerchantInfo> merchantInfoQueryWrapper = new QueryWrapper<>();
        merchantInfoQueryWrapper.select("pay_rate", "transfer_rate").eq("code", merchantCode);
        return getMap(merchantInfoQueryWrapper);
    }

    @Override
    public Boolean updateMerchantPwd(Long userId, String password, String passwordTips) {
        return this.baseMapper.updateMerchantPwd(userId, password, passwordTips) > 0;
    }

    @Override
    public Boolean updateUsdtAddress(Long userId, String usdtAddress) {
        return this.baseMapper.updateUsdtAddress(userId, usdtAddress) > 0;
    }

    @Override
    public MerchantFrontPageDTO fetchHomePageInfo(Long merchantId, String name) throws Exception {

        MerchantFrontPageDTO merchantFrontPageVo = new MerchantFrontPageDTO();
        String todayDateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), "yyyy-MM-dd");
        // 获取商户信息
        CompletableFuture<MerchantInfo> merchantFuture = CompletableFuture.supplyAsync(() -> {
            return userDetail(merchantId);
        });

        // 查询代付总订单数量
        CompletableFuture<Long> withdrawTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.queryWithdrawTotalNumByName(name);
        });

        // 查询今日付收笔数
        CompletableFuture<BigDecimal> todayWithdrawAmountFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.calcTodayWithdrawAmount(name, todayDateStr);
        });

        // 查询今日付收费用
        CompletableFuture<BigDecimal> todayWithdrawCommissionFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.calcTodayWithdrawCommission(name, todayDateStr);
        });

        // 今日付收笔数
        CompletableFuture<Long> todayWithdrawFinishNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.calcTodayWithdrawFinishNum(name, todayDateStr);
        });

        // 查询支付当日金额
        CompletableFuture<BigDecimal> todayPayAmountFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.calcTodayPayAmount(name, todayDateStr);
        });

        // 查询今日支付手续费
        CompletableFuture<BigDecimal> todayPayCommissionFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.calcTodayPayCommission(name, todayDateStr);
        });

        // 查询今日代收笔数
        CompletableFuture<Long> todayTodayPayFinishNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.calcTodayPayFinishNum(name, todayDateStr);
        });

        // 查询代收总订单数量
        CompletableFuture<Long> payTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.queryPayTotalNumByName(name);
        });


        // 代收未回调订单数量
        CompletableFuture<Long> payNotCallNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.queryPayNotCallNumByName(name);
        });

        // 代收回调失败订单数量
        CompletableFuture<Long> payCallFailedNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.queryPayCallFailedNumByName(name);
        });

        // 代付未回调订单数量
        CompletableFuture<Long> withdrawNotCallNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.queryWithdrawNotCallNumByName(name);
        });

        // 代付回调失败订单数量
        CompletableFuture<Long> withdrawCallFailedNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.queryWithdrawCallFailedNumByName(name);
        });


        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                merchantFuture, withdrawTotalNumFuture, payTotalNumFuture,
                payNotCallNumFuture,
                withdrawCallFailedNumFuture, withdrawNotCallNumFuture,
                payCallFailedNumFuture, todayPayAmountFuture, todayPayCommissionFuture,
                todayTodayPayFinishNumFuture, todayWithdrawAmountFuture, todayWithdrawCommissionFuture, todayWithdrawFinishNumFuture);

        allFutures.get();
        MerchantInfo merchantInfo = merchantFuture.get();
        // 剩余额度
        merchantFrontPageVo.setRemainingBalance(merchantFuture.get().getBalance());
        merchantFrontPageVo.setPayTotalNum(payTotalNumFuture.get());
        merchantFrontPageVo.setWithdrawTotalNum(withdrawTotalNumFuture.get());
        merchantFrontPageVo.setPayFinishTotalNum(merchantInfo.getTotalPayCount());
        merchantFrontPageVo.setWithdrawFinishTotalNum(merchantInfo.getTotalWithdrawCount());
        merchantFrontPageVo.setPayNotNotifyTotalNum(payNotCallNumFuture.get());
        merchantFrontPageVo.setPayNotifyFailedTotalNum(payCallFailedNumFuture.get());
        merchantFrontPageVo.setWithdrawNotNotifyTotalNum(withdrawNotCallNumFuture.get());
        merchantFrontPageVo.setWithdrawNotifyFailedTotalNum(withdrawCallFailedNumFuture.get());
        merchantFrontPageVo.setPayAndWithdrawSuccessTotalNum(merchantFrontPageVo.getPayFinishTotalNum() + merchantFrontPageVo.getWithdrawFinishTotalNum());
        merchantFrontPageVo.setTransferDownAmount(merchantInfo.getTransferDownAmount());
        merchantFrontPageVo.setTransferDownCount(merchantInfo.getTransferDownCount());
        merchantFrontPageVo.setTransferUpAmount(merchantInfo.getTransferUpAmount());
        merchantFrontPageVo.setTransferUpCount(merchantInfo.getTransferUpCount());
        merchantFrontPageVo.setWithdrawFinishTotalAmount(merchantInfo.getTotalWithdrawAmount());
        merchantFrontPageVo.setWithdrawTotalCommission(merchantInfo.getTotalWithdrawFee());
        merchantFrontPageVo.setPayFinishTotalAmount(merchantInfo.getTotalPayAmount());
        merchantFrontPageVo.setPayTotalCommission(merchantInfo.getTotalPayFee());
        merchantFrontPageVo.setLastLoginTime(merchantInfo.getLastLoginTime());
        merchantFrontPageVo.setLoginIp(merchantInfo.getLoginIp());


        // 今日代收额
        merchantFrontPageVo.setTodayPayAmount(todayPayAmountFuture.get());

        // 今日手续费
        merchantFrontPageVo.setTodayPayCommission(todayPayCommissionFuture.get());

        // 今日代收笔数
        merchantFrontPageVo.setTodayPayFinishNum(todayTodayPayFinishNumFuture.get());

        // 今日付收额
        merchantFrontPageVo.setTodayWithdrawAmount(todayWithdrawAmountFuture.get());

        // 今日代付手续费
        merchantFrontPageVo.setTodayWithdrawCommission(todayWithdrawCommissionFuture.get());

        // 今日付收笔数
        merchantFrontPageVo.setTodayWithdrawFinishNum(todayWithdrawFinishNumFuture.get());


        return merchantFrontPageVo;
    }

    /**
     * 总管理后台
     *
     * @return
     * @throws Exception
     */
    @Override
    public MerchantFrontPageDTO fetchHomePageInfo() throws Exception {
        MerchantFrontPageDTO merchantFrontPageVo = new MerchantFrontPageDTO();

        String todayDateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), "yyyy-MM-dd");


        // 统计商户今日代收交易额
        CompletableFuture<BigDecimal> todayMerchantPayAmountFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.todayMerchantPayAmount(todayDateStr);
        });

        // 统计商户今日代收交易笔数
        CompletableFuture<Long> todayMerchantPayTransNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.todayMerchantPayTransNum(todayDateStr);
        });

        // 统计商户代收交易总金额
        CompletableFuture<BigDecimal> merchantPayTotalAmountFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.merchantPayTotalAmount();
        });

        // 统计商户代收交易总笔数
        CompletableFuture<Long> merchantPayTransTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.merchantPayTransTotalNum();
        });

        // 统计商户代收今日费率
        CompletableFuture<BigDecimal> todayMerchantPayCommissionFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.todayMerchantPayCommission(todayDateStr);
        });

        // 统计商户代收总费率
        CompletableFuture<BigDecimal> merchantPayTotalCommissionFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.merchantPayTotalCommission();
        });

        // 查询代付总订单数量
        CompletableFuture<Long> withdrawTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.queryWithdrawTotalNum();
        });


        // 统计商户今日代付交易额
        CompletableFuture<BigDecimal> todayMerchantWithdrawAmountFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.todayMerchantWithdrawAmount(todayDateStr);
        });

        // 统计商户今日代付交易笔数
        CompletableFuture<Long> todayMerchantWithdrawTransNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.todayMerchantWithdrawTransNum(todayDateStr);
        });

        // 统计商户代付交易总金额
        CompletableFuture<BigDecimal> merchantWithdrawTotalAmountFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.merchantWithdrawTotalAmount();
        });

        // 统计商户代付交易总笔数
        CompletableFuture<Long> merchantWithdrawTransTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.merchantWithdrawTransTotalNum();
        });
        // 统计商户代付今日费率
        CompletableFuture<BigDecimal> todayMerchantWithdrawCommissionFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.todayMerchantWithdrawCommission(todayDateStr);
        });

        // 统计商户代付总费率
        CompletableFuture<BigDecimal> merchantWithdrawTotalCommissionFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.merchantWithdrawTotalCommission();
        });


        // 查询代收总订单数量
        CompletableFuture<Long> payTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.queryPayTotalNum();
        });

        // 获取金额错误订单
        CompletableFuture<Long> amountErrorNumFuture = CompletableFuture.supplyAsync(() -> {
            return matchingOrderMapper.fethchAmountErrorNum();
        });

        // 获取匹配成功订单数量
        CompletableFuture<Long> matchSuccessNumFuture = CompletableFuture.supplyAsync(() -> {
            return matchingOrderMapper.matchSuccessNum();
        });


        // 统计今日买入订单
        CompletableFuture<OrderInfoVo> todayBuyInfoFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.fetchTodayBuyInfoFuture(todayDateStr);
        });

        // 统计今日买入
        CompletableFuture<Long> todayBuyInfoTotalFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.todayBuyInfoFuture(todayDateStr);
        });

        // 统计今日卖出订单
        CompletableFuture<OrderInfoVo> todaySellInfoFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.fetchTodaySellInfoFuture(todayDateStr);
        });

        // 统计今日卖出总笔数
        CompletableFuture<Long> todaySellInfoTotalFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.todaySellInfoFuture(todayDateStr);
        });

        // 统计今日买入总订单信息
        CompletableFuture<OrderInfoVo> buyTotalInfoFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.fetchBuyTotalInfoFuture();
        });

        // 统计今日卖出总订单信息
        CompletableFuture<OrderInfoVo> sellTotalInfoFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.fetchSellTotalInfoFuture();
        });

        // 统计今日usdt信息
        CompletableFuture<OrderInfoVo> todayUsdtInfoFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.fetchTodayUsdtInfoFuture(todayDateStr);
        });

        // 统计usdt总信息
        CompletableFuture<OrderInfoVo> usdtTotalInfoFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.fetchUsdtTotalInfoFuture();
        });


        // 充值订单取消支付订单数量
        CompletableFuture<Long> payCancelNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.queryPayCancelNum();
        });

//        // 充值订单取消订单数量
//        CompletableFuture<Long> payCancelOrderNumFuture = CompletableFuture.supplyAsync(() -> {
////            return collectionOrderMapper.queryPayCancelOrderNum();
////        });
//
////        // 充值订单取消订单数量
////        CompletableFuture<Long> payAppealNumFuture = CompletableFuture.supplyAsync(() -> {
////            return collectionOrderMapper.queryPayAppealNum();
////        });

        // 充值订单取消订单数量
        CompletableFuture<Long> payAppealTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return collectionOrderMapper.queryPayAppealTotalNum();
        });


//        // 代付取消匹配订单数量
//        CompletableFuture<Long> withdrawAppealNumFuture = CompletableFuture.supplyAsync(() -> {
//            return paymentOrderMapper.withdrawAppealNum();
//        });

        // 代付取消匹配订单数量
        CompletableFuture<Long> withdrawAppealTotalNumFuture = CompletableFuture.supplyAsync(() -> {
            return paymentOrderMapper.withdrawAppealTotalNum();
        });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                payCancelNumFuture,
                todayBuyInfoFuture, todaySellInfoFuture, buyTotalInfoFuture,
                sellTotalInfoFuture, matchSuccessNumFuture,
                todayMerchantPayAmountFuture, todayMerchantPayTransNumFuture,
                merchantPayTotalAmountFuture, merchantPayTransTotalNumFuture,
                todayMerchantPayCommissionFuture, merchantPayTotalCommissionFuture,
                todayMerchantWithdrawAmountFuture, todayMerchantWithdrawTransNumFuture,
                merchantWithdrawTotalAmountFuture, merchantWithdrawTransTotalNumFuture,
                todayMerchantWithdrawCommissionFuture, merchantWithdrawTotalCommissionFuture,
                payAppealTotalNumFuture, withdrawAppealTotalNumFuture,
                todaySellInfoTotalFuture, todayBuyInfoTotalFuture
        );

        allFutures.get();
        // 剩余额度

        merchantFrontPageVo.setPayCancelNum(payCancelNumFuture.get());
        merchantFrontPageVo.setWithdrawTotalNum(withdrawTotalNumFuture.get());
        merchantFrontPageVo.setPayTotalNum(payTotalNumFuture.get());
        merchantFrontPageVo.setAmountErrorNum(amountErrorNumFuture.get());
        // 今日买入
        merchantFrontPageVo.setTodayPayAmount(todayBuyInfoFuture.get().getActualAmount());
        merchantFrontPageVo.setTodayPayCommission(todayBuyInfoFuture.get().getTotalCost());
        merchantFrontPageVo.setTodayPayFinishNum(todayBuyInfoFuture.get().getTotalNum());
        // 总
        merchantFrontPageVo.setPayFinishTotalNum(buyTotalInfoFuture.get().getTotalNum());
        merchantFrontPageVo.setPayFinishTotalAmount(buyTotalInfoFuture.get().getActualAmount());
        merchantFrontPageVo.setPayTotalCommission(buyTotalInfoFuture.get().getTotalCost());

        // 今日卖出
        merchantFrontPageVo.setTodayWithdrawAmount(todaySellInfoFuture.get().getActualAmount());
        merchantFrontPageVo.setTodayWithdrawCommission(todaySellInfoFuture.get().getTotalCost());
        merchantFrontPageVo.setTodayWithdrawFinishNum(todaySellInfoFuture.get().getTotalNum());
        // 总
        merchantFrontPageVo.setWithdrawFinishTotalNum(sellTotalInfoFuture.get().getTotalNum());
        merchantFrontPageVo.setWithdrawTotalCommission(sellTotalInfoFuture.get().getTotalCost());
        merchantFrontPageVo.setWithdrawFinishTotalAmount(sellTotalInfoFuture.get().getActualAmount());

        merchantFrontPageVo.setUsdtTotalAmount(usdtTotalInfoFuture.get().getActualAmount());
        merchantFrontPageVo.setTodayUsdtAmount(todayUsdtInfoFuture.get().getActualAmount());
        merchantFrontPageVo.setUsdtTotalNum(usdtTotalInfoFuture.get().getTotalNum());
        merchantFrontPageVo.setPayAndWithdrawSuccessTotalNum(merchantFrontPageVo.getPayFinishTotalNum() + merchantFrontPageVo.getWithdrawFinishTotalNum());
        merchantFrontPageVo.setMatchSuccessNum(matchSuccessNumFuture.get());
        merchantFrontPageVo.setTodayMerchantPayAmount(todayMerchantPayAmountFuture.get());
        merchantFrontPageVo.setTodayMerchantPayCommission(todayMerchantPayCommissionFuture.get());
        merchantFrontPageVo.setTodayMerchantPayTransNum(todayMerchantPayTransNumFuture.get());
        merchantFrontPageVo.setMerchantPayTotalAmount(merchantPayTotalAmountFuture.get());
        merchantFrontPageVo.setMerchantPayTransTotalNum(merchantPayTransTotalNumFuture.get());
        merchantFrontPageVo.setMerchantPayTotalCommission(merchantPayTotalCommissionFuture.get());
        merchantFrontPageVo.setTodayMerchantWithdrawAmount(todayMerchantWithdrawAmountFuture.get());
        merchantFrontPageVo.setTodayMerchantWithdrawTransNum(todayMerchantWithdrawTransNumFuture.get());
        merchantFrontPageVo.setMerchantWithdrawTotalAmount(merchantWithdrawTotalAmountFuture.get());
        merchantFrontPageVo.setMerchantWithdrawTransTotalNum(merchantWithdrawTransTotalNumFuture.get());
        merchantFrontPageVo.setTodayMerchantWithdrawCommission(todayMerchantWithdrawCommissionFuture.get());
        merchantFrontPageVo.setMerchantWithdrawTotalCommission(merchantWithdrawTotalCommissionFuture.get());
        merchantFrontPageVo.setPayAppealTotalNum(payAppealTotalNumFuture.get());
        merchantFrontPageVo.setWithdrawAppealTotalNum(withdrawAppealTotalNumFuture.get());

        merchantFrontPageVo.setTodayPayTotalNum(todayBuyInfoTotalFuture.get());
        merchantFrontPageVo.setTodayWithdrawTotalNum(todaySellInfoTotalFuture.get());
        return merchantFrontPageVo;
    }

    @Override
    @SneakyThrows
    public PageReturn<WithdrawOrderDTO> fetchWithdrawOrderInfo(WithdrawOrderReq withdrawOrderReq) {

        Page<MerchantPaymentOrders> page = new Page<>();
        page.setCurrent(withdrawOrderReq.getPageNo());
        page.setSize(withdrawOrderReq.getPageSize());
        LambdaQueryWrapper<MerchantPaymentOrders> paymentOrder = new LambdaQueryWrapper<>();

        // 新增统计金额字段总计字段
        LambdaQueryWrapper<MerchantPaymentOrders> queryWrapper = new QueryWrapper<MerchantPaymentOrders>()
                .select("IFNULL(sum(amount), 0) as amountTotal," +
                        "IFNULL(sum(cost + fixed_fee), 0) as costTotal," +
                        "IFNULL(sum(order_amount), 0) as orderAmountTotal"
                ).lambda();

        if (org.apache.commons.lang3.StringUtils.isNotBlank(withdrawOrderReq.getPayType())) {
            paymentOrder.eq(MerchantPaymentOrders::getPayType, withdrawOrderReq.getPayType());
            queryWrapper.eq(MerchantPaymentOrders::getPayType, withdrawOrderReq.getPayType());
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(withdrawOrderReq.getMerchantOrder())) {
            paymentOrder.eq(MerchantPaymentOrders::getMerchantOrder, withdrawOrderReq.getMerchantOrder());
            queryWrapper.eq(MerchantPaymentOrders::getMerchantOrder, withdrawOrderReq.getMerchantOrder());
        }
        if (CollectionUtils.isNotEmpty(withdrawOrderReq.getMerchantCodes())) {
            paymentOrder.in(MerchantPaymentOrders::getMerchantCode, withdrawOrderReq.getMerchantCodes());
            queryWrapper.in(MerchantPaymentOrders::getMerchantCode, withdrawOrderReq.getMerchantCodes());
        }
        if (!StringUtils.isNullOrEmpty(withdrawOrderReq.getCurrency())) {
            paymentOrder.eq(MerchantPaymentOrders::getCurrency, withdrawOrderReq.getCurrency());
            queryWrapper.eq(MerchantPaymentOrders::getCurrency, withdrawOrderReq.getCurrency());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(withdrawOrderReq.getPlatformOrder())) {
            paymentOrder.and(e -> e.or().eq(MerchantPaymentOrders::getPlatformOrder, withdrawOrderReq.getPlatformOrder()).or().eq(MerchantPaymentOrders::getMerchantOrder, withdrawOrderReq.getPlatformOrder()));
            queryWrapper.and(e -> e.or().eq(MerchantPaymentOrders::getPlatformOrder, withdrawOrderReq.getPlatformOrder()).or().eq(MerchantPaymentOrders::getMerchantOrder, withdrawOrderReq.getPlatformOrder()));
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(withdrawOrderReq.getMerchantName())) {
            paymentOrder.eq(MerchantPaymentOrders::getMerchantName, withdrawOrderReq.getMerchantName());
            queryWrapper.eq(MerchantPaymentOrders::getMerchantName, withdrawOrderReq.getMerchantName());
        }
        if (!ObjectUtils.isEmpty(withdrawOrderReq.getOrderStatus())) {
            paymentOrder.eq(MerchantPaymentOrders::getOrderStatus, withdrawOrderReq.getOrderStatus());
            queryWrapper.eq(MerchantPaymentOrders::getOrderStatus, withdrawOrderReq.getOrderStatus());
        }
        if (!ObjectUtils.isEmpty(withdrawOrderReq.getCallbackStatus())) {
            paymentOrder.eq(MerchantPaymentOrders::getTradeCallbackStatus, withdrawOrderReq.getCallbackStatus());
            queryWrapper.eq(MerchantPaymentOrders::getTradeCallbackStatus, withdrawOrderReq.getCallbackStatus());
        }
        if (!org.uu.common.core.utils.StringUtils.isEmpty(withdrawOrderReq.getMemberId())) {
            paymentOrder.eq(MerchantPaymentOrders::getMemberId, withdrawOrderReq.getMemberId());
            queryWrapper.eq(MerchantPaymentOrders::getMemberId, withdrawOrderReq.getMemberId());
        }
        if (!org.uu.common.core.utils.StringUtils.isEmpty(withdrawOrderReq.getExternalMemberId())) {
            paymentOrder.like(MerchantPaymentOrders::getExternalMemberId, withdrawOrderReq.getExternalMemberId());
            queryWrapper.like(MerchantPaymentOrders::getExternalMemberId, withdrawOrderReq.getExternalMemberId());
        }
        // 下单时间
        if (!ObjectUtils.isEmpty(withdrawOrderReq.getTimeType()) && withdrawOrderReq.getTimeType().equals(1)) {
            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getStartTime())) {
                paymentOrder.ge(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getStartTime());
                queryWrapper.ge(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getStartTime());
            }

            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getEndTime())) {
                paymentOrder.le(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getEndTime());
                queryWrapper.le(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getEndTime());
            }
        } else if (!ObjectUtils.isEmpty(withdrawOrderReq.getTimeType()) && withdrawOrderReq.getTimeType().equals(2)) {
            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getStartTime())) {
                paymentOrder.ge(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getStartTime());
                queryWrapper.ge(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getStartTime());
            }

            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getEndTime())) {
                paymentOrder.le(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getEndTime());
                queryWrapper.le(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getEndTime());
            }
        } else {

            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getStartTime())) {
                paymentOrder.ge(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getStartTime());
                queryWrapper.ge(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getStartTime());
            }

            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getEndTime())) {
                paymentOrder.le(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getEndTime());
                queryWrapper.le(MerchantPaymentOrders::getCreateTime, withdrawOrderReq.getEndTime());
            }

            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getCompleteStartTime())) {
                paymentOrder.ge(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getStartTime());
                queryWrapper.ge(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getStartTime());
            }

            if (ObjectUtils.isNotEmpty(withdrawOrderReq.getCompleteEndTime())) {
                paymentOrder.le(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getEndTime());
                queryWrapper.le(MerchantPaymentOrders::getUpdateTime, withdrawOrderReq.getEndTime());
            }
        }
        paymentOrder.orderByDesc(MerchantPaymentOrders::getId);


        Page<MerchantPaymentOrders> finalPage = page;
        CompletableFuture<MerchantPaymentOrders> totalFuture = CompletableFuture.supplyAsync(() -> merchantPaymentOrdersMapper.selectOne(queryWrapper));
        CompletableFuture<Page<MerchantPaymentOrders>> resultFuture = CompletableFuture.supplyAsync(() -> merchantPaymentOrdersMapper.selectPage(finalPage, paymentOrder));
        CompletableFuture.allOf(totalFuture, resultFuture);

        page = resultFuture.get();
        MerchantPaymentOrders totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("amountTotal", totalInfo.getAmountTotal());
        extent.put("costTotal", totalInfo.getCostTotal());
        extent.put("orderAmountTotal", totalInfo.getOrderAmountTotal());
        BigDecimal amountPageTotal = BigDecimal.ZERO;
        BigDecimal costPageTotal = BigDecimal.ZERO;
        BigDecimal orderAmountPageTotal = BigDecimal.ZERO;
        List<MerchantPaymentOrders> records = page.getRecords();
        List<WithdrawOrderDTO> withdrawOrderDTOList = walletMapStruct.withdrawOrderTransform(records);
        for (WithdrawOrderDTO item : withdrawOrderDTOList) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(item.getExternalMemberId())) {
                String externalMemberId = item.getExternalMemberId().substring(item.getMerchantCode().length());
                item.setExternalMemberId(externalMemberId);
            }
            amountPageTotal = amountPageTotal.add(item.getAmount());
            BigDecimal cost = BigDecimal.ZERO;
            BigDecimal fixedFee = BigDecimal.ZERO;
            if(ObjectUtils.isNotEmpty(item.getCost())) {
                cost = item.getCost();
            }

            if(ObjectUtils.isNotEmpty(item.getFixedFee())) {
                fixedFee = item.getFixedFee();
            }
            costPageTotal = costPageTotal.add(cost).add(fixedFee);
            BigDecimal orderAmount = item.getOrderAmount();
            if (ObjectUtils.isEmpty(orderAmount)) {
                orderAmount = BigDecimal.ZERO;
            }
            orderAmountPageTotal = orderAmountPageTotal.add(orderAmount);
        }
        extent.put("amountPageTotal", amountPageTotal);
        extent.put("costPageTotal", costPageTotal);
        extent.put("orderAmountPageTotal", orderAmountPageTotal);
        return PageUtils.flush(page, withdrawOrderDTOList, extent);
    }

    @Override
    @Transactional
    public RestResult closePaymentOrder(PaidParamReq req) {
        try{
            String id = req.getId();
            MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectById(id);
            if(ObjectUtils.isEmpty(merchantPaymentOrders)) {
                return RestResult.failed(ResultCode.ORDER_NOT_EXIST);
            }
            if (!merchantPaymentOrders.getOrderStatus().equals(PaymentOrderStatusEnum.BE_MATCHED.getCode())) {
                return RestResult.failed(ResultCode.ORDER_STATUS_VERIFICATION_FAILED);
            }
            merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.FAILED.getCode());
            int i = merchantPaymentOrdersMapper.updateById(merchantPaymentOrders);
            if (i != 1) {
                return RestResult.failed("update merchant payment orders failed");
            }
            String payType = merchantPaymentOrders.getPayType();
            // 释放代付订单锁定金额
            MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(merchantPaymentOrders.getMerchantCode());
            BigDecimal balance;
            int i1;
            LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode());
            BigDecimal allAmount = merchantPaymentOrders.getAmount().add(merchantPaymentOrders.getCost()).add(merchantPaymentOrders.getFixedFee());
            // usdt归还trc20交易余额
            if (payType.equals(PayTypeEnum.INDIAN_USDT.getCode())) {
                balance = merchantInfo.getUsdtBalance();
                if (merchantInfo.getPendingUsdtBalance().compareTo(allAmount) < 0) {
                    throw new Exception("usdt transaction balance not enough");
                }
                lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getUsdtBalance, balance.add(allAmount))
                        .set(MerchantInfo::getPendingUsdtBalance, merchantInfo.getPendingUsdtBalance().subtract(allAmount));
            }
            // trx归还trx余额
            if (payType.equals(PayTypeEnum.INDIAN_TRX.getCode())) {
                balance = merchantInfo.getTrxBalance();
                if (merchantInfo.getPendingTrxBalance().compareTo(allAmount) < 0) {
                    throw new Exception("TRX transaction balance not enough");
                }
                lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getTrxBalance, balance.add(allAmount))
                        .set(MerchantInfo::getPendingTrxBalance, merchantInfo.getPendingTrxBalance().subtract(allAmount));
            }
            // 银行卡类型归还法币
            if (payType.equals(PayTypeEnum.INDIAN_CARD.getCode())) {
                balance = merchantInfo.getBalance();
                if (merchantInfo.getPendingBalance().compareTo(allAmount) < 0) {
                    throw new Exception("transaction balance not enough");
                }
                lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getBalance, balance.add(allAmount))
                        .set(MerchantInfo::getPendingBalance, merchantInfo.getPendingBalance().subtract(allAmount));
            }
            i1 = merchantInfoMapper.update(null, lambdaUpdateWrapperMerchantInfo);
            if (i1 != 1) {
                throw new Exception("merchant payment order balance update failed");
            }
            // 注册事务同步回调 事务提交成功后才执行以下操作
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    //发送提现成功 异步延时回调通知 后期转正常
                    long millis = 3000L;
                    //发送提现延时回调的MQ消息
                    TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                    rabbitMQService.sendTimeoutTask(taskInfo, millis);
                    // 删除订单状态
                    redisUtil.deleteOrder(merchantPaymentOrders.getPlatformOrder());
                    memberSendAmountList.send();
                }
            });
            return RestResult.ok(true);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("已支付操作失败： e {}", e);
        }
        return RestResult.failed("close payment order failed");
    }

    @Override
    public PageReturn<WithdrawOrderExportDTO> fetchWithdrawOrderInfoExport(WithdrawOrderReq req) {
        PageReturn<WithdrawOrderDTO> withdrawOrderReturn = fetchWithdrawOrderInfo(req);

        List<WithdrawOrderExportDTO> resultList = new ArrayList<>();
        for (WithdrawOrderDTO withdrawOrderDTO : withdrawOrderReturn.getList()) {
            WithdrawOrderExportDTO withdrawOrderExportDTO = new WithdrawOrderExportDTO();
            BeanUtils.copyProperties(withdrawOrderDTO, withdrawOrderExportDTO);
            /*String nameByCode = PaymentOrderStatusEnum.getNameByCode(withdrawOrderDTO.getOrderStatus(), req.getLang());
            withdrawOrderExportDTO.setOrderStatus(nameByCode);*/
            withdrawOrderExportDTO.setTradeCallbackStatus(NotifyStatusEnum.getNameByCode(withdrawOrderDTO.getTradeCallbackStatus(), req.getLang()));
            if (withdrawOrderDTO.getAmount() != null) {
                withdrawOrderExportDTO.setAmount(withdrawOrderDTO.getAmount().toString());
            }
            if (withdrawOrderDTO.getCost() != null) {
                withdrawOrderExportDTO.setCost(withdrawOrderDTO.getCost().toString());
            }
            if (!StringUtils.isNullOrEmpty(withdrawOrderDTO.getPayType())) {
                withdrawOrderExportDTO.setPayType(PayTypeEnum.getNameByCode(withdrawOrderDTO.getPayType()));
                if (!Objects.equals(req.getLang(), "zh") && (Objects.equals(withdrawOrderDTO.getPayType(), "1"))) {
                    withdrawOrderExportDTO.setPayType("bank card");
                }
            }

            resultList.add(withdrawOrderExportDTO);
        }
        Page<WithdrawOrderExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(withdrawOrderReturn.getTotal());
        return PageUtils.flush(page, resultList);
    }

    /**
     * 代付手动回调成功
     *
     * @param id
     * @return
     */
    @Override
    public Boolean confirmSuccess(Long id) {
        boolean result = false;
        try {
            MerchantPaymentOrders paymentOrder = merchantPaymentOrdersMapper.selectById(id);

            if (ObjectUtils.isEmpty(paymentOrder)) {
                throw new BizException(ResultCode.ORDER_NOT_EXIST);
            }
            // 判断交易回调状态
            if (paymentOrder.getTradeCallbackStatus().equals(NotifyStatusEnum.SUCCESS.getCode()) || paymentOrder.getTradeCallbackStatus().equals(NotifyStatusEnum.MANUAL_SUCCESS.getCode())) {
                throw new BizException(ResultCode.ORDER_ALREADY_CALLBACK);
            } else {
                result = asyncNotifyService.sendWithDrawSuccessCallbackWithRecordRequest(paymentOrder.getMerchantOrder(), "2");
            }
        } catch (Exception ex) {
            log.error("confirmSuccess->" + ex.getMessage());
        }

        return result;

    }


    @Override
    public MerchantInfo userDetailByCode(String code) {
        MerchantInfo merchantInfo = lambdaQuery().eq(MerchantInfo::getCode, code).one();
        AssertUtil.notEmpty(merchantInfo, ResultCode.USERNAME_OR_PASSWORD_ERROR);

        // 查询绑定的角色IDs


        return merchantInfo;
    }

    @Override
    public Map<Integer, String> fetchOrderStatus() {

        Map<Integer, String> map = new HashMap<>(15);
        map.put(Integer.parseInt(CollectionOrderStatusEnum.PAID.getCode()), CollectionOrderStatusEnum.PAID.getName());
        map.put(Integer.parseInt(CollectionOrderStatusEnum.BE_PAID.getCode()), CollectionOrderStatusEnum.BE_PAID.getName());
        map.put(Integer.parseInt(CollectionOrderStatusEnum.WAS_CANCELED.getCode()), CollectionOrderStatusEnum.WAS_CANCELED.getName());

        return map;
    }

    @Override
    public Map<Integer, String> orderCallbackStatus() {

        Map<Integer, String> map = new HashMap<>(15);
        map.put(Integer.parseInt(NotifyStatusEnum.NOTCALLBACK.getCode()), NotifyStatusEnum.NOTCALLBACK.getName());
        map.put(Integer.parseInt(NotifyStatusEnum.SUCCESS.getCode()), NotifyStatusEnum.SUCCESS.getName());
        map.put(Integer.parseInt(NotifyStatusEnum.FAILED.getCode()), NotifyStatusEnum.FAILED.getName());
        map.put(Integer.parseInt(NotifyStatusEnum.MANUAL_SUCCESS.getCode()), NotifyStatusEnum.MANUAL_SUCCESS.getName());
        map.put(Integer.parseInt(NotifyStatusEnum.MANUAL_FAILED.getCode()), NotifyStatusEnum.MANUAL_FAILED.getName());

        return map;
    }


    @Override
    @SneakyThrows
    public PageReturn<RechargeOrderDTO> fetchRechargeOrderInfo(RechargeOrderReq rechargeOrderReq) {
        Page<MerchantCollectOrders> page = new Page<>();
        page.setCurrent(rechargeOrderReq.getPageNo());
        page.setSize(rechargeOrderReq.getPageSize());
        LambdaQueryWrapper<MerchantCollectOrders> collectionOrder = new LambdaQueryWrapper<>();

        // 新增统计金额字段总计字段
        LambdaQueryWrapper<MerchantCollectOrders> queryWrapper = new QueryWrapper<MerchantCollectOrders>()
                .select("IFNULL(sum(amount),0) as amountTotal," +
                        "IFNULL(sum(cost + fixed_fee), 0) as costTotal," +
                        "IFNULL(sum(order_amount), 0) as orderAmountTotal"
                ).lambda();

        if (!StringUtils.isNullOrEmpty(rechargeOrderReq.getCurrency())) {
            collectionOrder.eq(MerchantCollectOrders::getCurrency, rechargeOrderReq.getCurrency());
            queryWrapper.eq(MerchantCollectOrders::getCurrency, rechargeOrderReq.getCurrency());
        }

        if (org.apache.commons.lang3.StringUtils.isNotBlank(rechargeOrderReq.getPayType())) {
            collectionOrder.eq(MerchantCollectOrders::getPayType, rechargeOrderReq.getPayType());
            queryWrapper.eq(MerchantCollectOrders::getPayType, rechargeOrderReq.getPayType());
        }

        if (!StringUtils.isNullOrEmpty(rechargeOrderReq.getMerchantOrder())) {
            collectionOrder.eq(MerchantCollectOrders::getMerchantOrder, rechargeOrderReq.getMerchantOrder());
            queryWrapper.eq(MerchantCollectOrders::getMerchantOrder, rechargeOrderReq.getMerchantOrder());
        }
        if (!StringUtils.isNullOrEmpty(rechargeOrderReq.getPlatformOrder())) {
            collectionOrder.and(e -> e.or().eq(MerchantCollectOrders::getPlatformOrder, rechargeOrderReq.getPlatformOrder()).or().eq(MerchantCollectOrders::getMerchantOrder, rechargeOrderReq.getPlatformOrder()));
            queryWrapper.and(e -> e.or().eq(MerchantCollectOrders::getPlatformOrder, rechargeOrderReq.getPlatformOrder()).or().eq(MerchantCollectOrders::getMerchantOrder, rechargeOrderReq.getPlatformOrder()));
        }
        if (!ObjectUtils.isEmpty(rechargeOrderReq.getOrderStatus())) {
            collectionOrder.eq(MerchantCollectOrders::getOrderStatus, rechargeOrderReq.getOrderStatus());
            queryWrapper.eq(MerchantCollectOrders::getOrderStatus, rechargeOrderReq.getOrderStatus());
        }
        if (!ObjectUtils.isEmpty(rechargeOrderReq.getCallbackStatus())) {
            collectionOrder.eq(MerchantCollectOrders::getTradeCallbackStatus, rechargeOrderReq.getCallbackStatus());
            queryWrapper.eq(MerchantCollectOrders::getTradeCallbackStatus, rechargeOrderReq.getCallbackStatus());
        }
        if (CollectionUtils.isNotEmpty(rechargeOrderReq.getMerchantCodes())) {
            collectionOrder.in(MerchantCollectOrders::getMerchantCode, rechargeOrderReq.getMerchantCodes());
            queryWrapper.in(MerchantCollectOrders::getMerchantCode, rechargeOrderReq.getMerchantCodes());
        }

        if (!org.uu.common.core.utils.StringUtils.isEmpty(rechargeOrderReq.getMerchantName())) {
            collectionOrder.eq(MerchantCollectOrders::getMerchantName, rechargeOrderReq.getMerchantName());
            queryWrapper.eq(MerchantCollectOrders::getMerchantName, rechargeOrderReq.getMerchantName());
        }
        if (!org.uu.common.core.utils.StringUtils.isEmpty(rechargeOrderReq.getMemberId())) {
            collectionOrder.eq(MerchantCollectOrders::getMemberId, rechargeOrderReq.getMemberId());
            queryWrapper.eq(MerchantCollectOrders::getMemberId, rechargeOrderReq.getMemberId());
        }
        if (!org.uu.common.core.utils.StringUtils.isEmpty(rechargeOrderReq.getExternalMemberId())) {
            collectionOrder.like(MerchantCollectOrders::getExternalMemberId, rechargeOrderReq.getExternalMemberId());
            queryWrapper.like(MerchantCollectOrders::getExternalMemberId, rechargeOrderReq.getExternalMemberId());
        }
        // 下单时间
        if (!ObjectUtils.isEmpty(rechargeOrderReq.getTimeType()) && rechargeOrderReq.getTimeType().equals(1)) {
            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getStartTime())) {
                collectionOrder.ge(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getStartTime());
                queryWrapper.ge(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getStartTime());
            }

            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getEndTime())) {
                collectionOrder.le(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getEndTime());
                queryWrapper.le(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getEndTime());
            }
        } else if (!ObjectUtils.isEmpty(rechargeOrderReq.getTimeType()) && rechargeOrderReq.getTimeType().equals(2)) {
            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getStartTime())) {
                collectionOrder.ge(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getStartTime());
                queryWrapper.ge(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getStartTime());
            }

            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getEndTime())) {
                collectionOrder.le(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getEndTime());
                queryWrapper.le(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getEndTime());
            }
        } else {
            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getStartTime())) {
                collectionOrder.ge(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getStartTime());
                queryWrapper.ge(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getStartTime());
            }

            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getEndTime())) {
                collectionOrder.le(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getEndTime());
                queryWrapper.le(MerchantCollectOrders::getCreateTime, rechargeOrderReq.getEndTime());
            }
            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getCompleteStartTime())) {
                collectionOrder.ge(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getCompleteStartTime());
                queryWrapper.ge(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getCompleteStartTime());
            }

            if (ObjectUtils.isNotEmpty(rechargeOrderReq.getCompleteEndTime())) {
                collectionOrder.le(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getCompleteEndTime());
                queryWrapper.le(MerchantCollectOrders::getUpdateTime, rechargeOrderReq.getCompleteEndTime());
            }
        }
        collectionOrder.orderByDesc(MerchantCollectOrders::getCreateTime);

        Page<MerchantCollectOrders> finalPage = page;
        CompletableFuture<MerchantCollectOrders> totalFuture = CompletableFuture.supplyAsync(() -> merchantCollectOrdersMapper.selectOne(queryWrapper));
        CompletableFuture<Page<MerchantCollectOrders>> resultFuture = CompletableFuture.supplyAsync(() -> merchantCollectOrdersMapper.selectPage(finalPage, collectionOrder));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        MerchantCollectOrders totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();
        extent.put("amountTotal", totalInfo.getAmountTotal());
        extent.put("costTotal", totalInfo.getCostTotal());
        extent.put("orderAmountTotal", totalInfo.getOrderAmountTotal());
        List<MerchantCollectOrders> records = page.getRecords();
        BigDecimal amountPageTotal = BigDecimal.ZERO;
        BigDecimal costPageTotal = BigDecimal.ZERO;
        BigDecimal orderAmountPageTotal = BigDecimal.ZERO;
        List<RechargeOrderDTO> rechargeOrderDTOList = walletMapStruct.rechargeOrderTransform(records);
        for (RechargeOrderDTO item : rechargeOrderDTOList) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(item.getExternalMemberId())) {
                String externalMemberId = item.getExternalMemberId().substring(item.getMerchantCode().length());
                item.setExternalMemberId(externalMemberId);
            }
            amountPageTotal = amountPageTotal.add(item.getAmount());
            BigDecimal cost = BigDecimal.ZERO;
            BigDecimal fixedFee = BigDecimal.ZERO;
            if(ObjectUtils.isNotEmpty(item.getCost())) {
                cost = item.getCost();
            }

            if(ObjectUtils.isNotEmpty(item.getFixedFee())) {
                fixedFee = item.getFixedFee();
            }
            costPageTotal = costPageTotal.add(cost).add(fixedFee);
            BigDecimal orderAmount = item.getOrderAmount();
            if (ObjectUtils.isEmpty(orderAmount)) {
                orderAmount = BigDecimal.ZERO;
            }
            orderAmountPageTotal = orderAmountPageTotal.add(orderAmount);
        }
        extent.put("amountPageTotal", amountPageTotal);
        extent.put("costPageTotal", costPageTotal);
        extent.put("orderAmountPageTotal", orderAmountPageTotal);
        return PageUtils.flush(page, rechargeOrderDTOList, extent);
    }

    @Override
    public PageReturn<RechargeOrderExportDTO> fetchRechargeOrderInfoExport(RechargeOrderReq req) {
        PageReturn<RechargeOrderDTO> rechargeOrderReturn = fetchRechargeOrderInfo(req);

        List<RechargeOrderExportDTO> resultList = new ArrayList<>();

        for (RechargeOrderDTO rechargeOrderDTO : rechargeOrderReturn.getList()) {
            RechargeOrderExportDTO rechargeOrderExportDTO = new RechargeOrderExportDTO();
            BeanUtils.copyProperties(rechargeOrderDTO, rechargeOrderExportDTO);

            //String nameByCode = PaymentOrderStatusEnum.getNameByCode(rechargeOrderDTO.getOrderStatus(), req.getLang());
            String notifyStatus = NotifyStatusEnum.getNameByCode(rechargeOrderDTO.getTradeCallbackStatus(), req.getLang());
            //rechargeOrderExportDTO.setOrderStatus(nameByCode);
            rechargeOrderExportDTO.setTradeCallbackStatus(notifyStatus);
            if (rechargeOrderDTO.getAmount() != null) {
                rechargeOrderExportDTO.setAmount(rechargeOrderDTO.getAmount().toString());
            }
            if (rechargeOrderDTO.getCost() != null) {
                rechargeOrderExportDTO.setCost(rechargeOrderDTO.getCost().toString());
            }
            if (!StringUtils.isNullOrEmpty(rechargeOrderDTO.getPayType())) {
                rechargeOrderExportDTO.setPayType(PayTypeEnum.getNameByCode(rechargeOrderDTO.getPayType()));
                if (!Objects.equals(req.getLang(), "zh") && (Objects.equals(rechargeOrderDTO.getPayType(), "1"))) {
                    rechargeOrderExportDTO.setPayType("bank card");
                }
            }

            resultList.add(rechargeOrderExportDTO);
        }
        Page<RechargeOrderExportDTO> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        page.setTotal(rechargeOrderReturn.getTotal());
        return PageUtils.flush(page, resultList);

    }

    @Override
    public Boolean rechargeConfirmSuccess(Long id) {

        Boolean result = false;
        try {
            MerchantCollectOrders collectionOrder = merchantCollectOrdersMapper.selectById(id);
            if (ObjectUtils.isEmpty(collectionOrder)) {
                throw new BizException(ResultCode.ORDER_NOT_EXIST);
            }
            // 判断交易回调状态
            if (collectionOrder.getTradeCallbackStatus().equals(NotifyStatusEnum.SUCCESS.getCode())) {
                throw new BizException(ResultCode.ORDER_ALREADY_CALLBACK);
            } else {
                result = asyncNotifyService.sendRechargeSuccessCallbackWithRecordRequest(collectionOrder.getPlatformOrder(), "2");
            }
        } catch (Exception ex) {
            log.error("rechargeConfirmSuccess->" + ex.getMessage());
            result = false;
        }

        return result;
    }

    @Override
    public Map<Long, String> getMerchantName() {
        List<MerchantInfo> result = this.baseMapper.selectList(null);
        Map<Long, String> merchantMap = new HashMap<>();
        for (MerchantInfo merchant : result) {
            merchantMap.put(merchant.getId(), merchant.getCode());
        }
        return merchantMap;
    }

    @Override
    public Map<String, String> getCurrency() {

        Map<String, String> map = new HashMap<>(15);
        map.put(CurrenceEnum.INDIA.getCode(), CurrenceEnum.INDIA.getName());
        map.put(CurrenceEnum.CHINA.getCode(), CurrenceEnum.CHINA.getName());
        return map;
    }

    @Override
    @SneakyThrows
    public OrderOverviewDTO getOrderNumOverview() {
        // 获取金额错误订单
        // CompletableFuture<Long> amountErrorNumFuture = CompletableFuture.supplyAsync(matchingOrderMapper::fethchAmountErrorNum);
        // 获取代付
        CompletableFuture<Long> merchantPaymentOrder = CompletableFuture.supplyAsync(collectionOrderMapper::merchantPaymentOrderNum);
        // 获取代收
        CompletableFuture<Long> merchantCollectionOrder = CompletableFuture.supplyAsync(paymentOrderMapper::merchantCollectionOrderNum);

        long merchantPaymentOrderNum = merchantPaymentOrder.get();
        long merchantCollectionOrderNum = merchantCollectionOrder.get();


        OrderOverviewDTO result = new OrderOverviewDTO();
        result.setMerchantCollectionOrderNum(merchantCollectionOrderNum);
        result.setMerchantPaymentOrderNum(merchantPaymentOrderNum);

        return result;
    }

    @Override
    @SneakyThrows
    public MemberOverviewDTO getMemberOverview() {

        // 获取在线会员信息
        Long onlineCount = CommonUtils.getOnlineCount(redisUtils);
        // 获取委托会员信息
        CompletableFuture<MemberOverviewDTO> delegatedMember = CompletableFuture.supplyAsync(memberInfoMapper::selectDelegatedMemberCount);
        MemberOverviewDTO memberOverviewDTO = delegatedMember.get();
        memberOverviewDTO.setOnlineMemberCount(onlineCount);
        return memberOverviewDTO;
    }

    @Override
    @SneakyThrows
    public TodayOrderOverviewDTO todayOrderOverview() {
        TodayOrderOverviewDTO todayOrderOverview = new TodayOrderOverviewDTO();
        String todayDateStr = DateUtil.format(LocalDateTime.now(ZoneId.systemDefault()), "yyyy-MM-dd");
        // 统计商户今日代收交易额
        CompletableFuture<BigDecimal> todayMerchantPayAmountFuture = CompletableFuture.supplyAsync(() -> collectionOrderMapper.todayMerchantPayAmount(todayDateStr));

        // 统计商户今日代收交易笔数
        CompletableFuture<Long> todayMerchantPayTransNumFuture = CompletableFuture.supplyAsync(() -> collectionOrderMapper.todayMerchantPayTransNum(todayDateStr));

        // 统计商户代收今日费率
        CompletableFuture<BigDecimal> todayMerchantPayCommissionFuture = CompletableFuture.supplyAsync(() -> collectionOrderMapper.todayMerchantPayCommission(todayDateStr));

        // 统计商户今日代付交易额
        CompletableFuture<BigDecimal> todayMerchantWithdrawAmountFuture = CompletableFuture.supplyAsync(() -> paymentOrderMapper.todayMerchantWithdrawAmount(todayDateStr));

        // 统计商户今日代付交易笔数
        CompletableFuture<Long> todayMerchantWithdrawTransNumFuture = CompletableFuture.supplyAsync(() -> paymentOrderMapper.todayMerchantWithdrawTransNum(todayDateStr));

        // 统计商户代付今日费率
        CompletableFuture<BigDecimal> todayMerchantWithdrawCommissionFuture = CompletableFuture.supplyAsync(() -> paymentOrderMapper.todayMerchantWithdrawCommission(todayDateStr));

        // 今日买入订单
        CompletableFuture<OrderInfoVo> todayBuyInfoFuture = CompletableFuture.supplyAsync(() -> collectionOrderMapper.fetchTodayBuyInfoFuture(todayDateStr));

        // 今日买入订单数量
        CompletableFuture<Long> todayBuyInfoTotalFuture = CompletableFuture.supplyAsync(() -> collectionOrderMapper.todayBuyInfoFuture(todayDateStr));

        // 今日卖出订单
        CompletableFuture<OrderInfoVo> todaySellInfoFuture = CompletableFuture.supplyAsync(() -> paymentOrderMapper.fetchTodaySellInfoFuture(todayDateStr));

        // 今日卖出订单数量
        CompletableFuture<Long> todaySellInfoTotalFuture = CompletableFuture.supplyAsync(() -> paymentOrderMapper.todaySellInfoFuture(todayDateStr));

        // 统计今日usdt信息
        CompletableFuture<OrderInfoVo> todayUsdtInfoFuture = CompletableFuture.supplyAsync(() -> paymentOrderMapper.fetchTodayUsdtInfoFuture(todayDateStr));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                todayBuyInfoFuture, todaySellInfoFuture,
                todayMerchantPayAmountFuture, todayMerchantPayTransNumFuture,
                todayMerchantPayCommissionFuture,
                todayMerchantWithdrawAmountFuture, todayMerchantWithdrawTransNumFuture,
                todayMerchantWithdrawCommissionFuture,
                todaySellInfoTotalFuture, todayBuyInfoTotalFuture
        );

        allFutures.get();

        todayOrderOverview.setTodayMerchantPayAmount(todayMerchantPayAmountFuture.get());
        todayOrderOverview.setTodayMerchantPayTransNum(todayMerchantPayTransNumFuture.get());
        todayOrderOverview.setTodayMerchantWithdrawAmount(todayMerchantWithdrawAmountFuture.get());
        todayOrderOverview.setTodayMerchantWithdrawTransNum(todayMerchantWithdrawTransNumFuture.get());
        todayOrderOverview.setTodayPayAmount(todayBuyInfoFuture.get().getActualAmount());
        todayOrderOverview.setTodayPayCommission(todayBuyInfoFuture.get().getTotalCost());
        todayOrderOverview.setTodayPayFinishNum(todayBuyInfoFuture.get().getTotalNum());
        todayOrderOverview.setTodayUsdtAmount(todayUsdtInfoFuture.get().getActualAmount());
        todayOrderOverview.setTodayPayTotalNum(todayBuyInfoTotalFuture.get());
        todayOrderOverview.setTodayWithdrawTotalNum(todaySellInfoTotalFuture.get());
        todayOrderOverview.setTodayWithdrawAmount(todaySellInfoFuture.get().getActualAmount());
        todayOrderOverview.setTodayWithdrawCommission(todaySellInfoFuture.get().getTotalCost());
        todayOrderOverview.setTodayWithdrawFinishNum(todaySellInfoFuture.get().getTotalNum());
        // 计算成功率
        BigDecimal paySuccessRate = BigDecimal.ZERO;
        BigDecimal withdrawSuccessRate = BigDecimal.ZERO;
        if (Objects.nonNull(todayOrderOverview.getTodayPayTotalNum()) && todayOrderOverview.getTodayPayTotalNum() > 0
                && Objects.nonNull(todayOrderOverview.getTodayPayFinishNum()) && todayOrderOverview.getTodayPayFinishNum() > 0
        ) {
            paySuccessRate = BigDecimal.valueOf(todayOrderOverview.getTodayPayTotalNum()).divide(new BigDecimal(todayOrderOverview.getTodayPayFinishNum())).setScale(4, BigDecimal.ROUND_HALF_UP);
        }

        if (Objects.nonNull(todayOrderOverview.getTodayWithdrawTotalNum()) && todayOrderOverview.getTodayWithdrawTotalNum() > 0
                && Objects.nonNull(todayOrderOverview.getTodayWithdrawFinishNum()) && todayOrderOverview.getTodayWithdrawFinishNum() > 0
        ) {
            withdrawSuccessRate = BigDecimal.valueOf(todayOrderOverview.getTodayWithdrawTotalNum()).divide(new BigDecimal(todayOrderOverview.getTodayWithdrawFinishNum())).setScale(4, BigDecimal.ROUND_HALF_UP);
        }
        todayOrderOverview.setTodayPaySuccessRate(paySuccessRate);
        todayOrderOverview.setTodayWithdrawSuccessRate(withdrawSuccessRate);
        return todayOrderOverview;
    }

    @Override
    @SneakyThrows
    public TodayUsdtOrderOverviewDTO todayUsdtOrderOverview() {
        String todayStr = DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY);
        CompletableFuture<TodayUsdtOrderOverviewDTO> usdtToday = CompletableFuture.supplyAsync(() -> usdtBuyOrderMapper.getUsdtBuyOrderToday(todayStr));
        CompletableFuture<TodayUsdtOrderOverviewDTO> usdtTotal = CompletableFuture.supplyAsync(usdtBuyOrderMapper::getUsdtBuyOrderTotal);

        TodayUsdtOrderOverviewDTO todayData = usdtToday.get();
        TodayUsdtOrderOverviewDTO totalData = usdtTotal.get();
        totalData.setTodayUsdtAmount(todayData.getTodayUsdtAmount());
        totalData.setTodayUsdtOrderCount(todayData.getTodayUsdtOrderCount());
        totalData.setTodayITokenAmount(todayData.getTodayITokenAmount());
        return totalData;
    }

    @Override
    public List<MerchantLastOrderWarnDTO> getLatestOrderTime() {
        List<MerchantLastOrderWarnDTO> resultList = new ArrayList<>();
        // 获取阈值
        TradeConfig tradeConfig = tradeConfigService.getById(1);
        Integer limitHours = tradeConfig.getMerchantOrderUncreatedTime();
        List<LastOrderWarnDTO> collectLastOrderCreditTime = merchantCollectOrdersMapper.getCollectLastOrderCreditTime();
        HashSet<String> collectOvertime = new HashSet<>();
        for (LastOrderWarnDTO lastOrderWarnDTO : collectLastOrderCreditTime) {
            check(lastOrderWarnDTO, limitHours, collectOvertime);
        }
        // 获取代付最后一笔订单时间，根据商户分组
        List<LastOrderWarnDTO> paymentLastOrderCreditTime = merchantPaymentOrdersMapper.getPaymentLastOrderCreditTime();
        HashSet<String> paymentOvertime = new HashSet<>();
        for (LastOrderWarnDTO lastOrderWarnDTO : paymentLastOrderCreditTime) {
            check(lastOrderWarnDTO, limitHours, paymentOvertime);
        }
        HashSet<String> result = new HashSet<>(collectOvertime);
        result.retainAll(paymentOvertime);
        for (String merchantName : result) {
            MerchantLastOrderWarnDTO dto = new MerchantLastOrderWarnDTO();
            // 获取代收最后一笔订单时间，根据商户分组
            dto.setThreshold(limitHours);
            dto.setMerchantName(merchantName);
            dto.setWarn(true);
            resultList.add(dto);
        }
        return resultList;
    }

    @Override
    @Transactional
    public KycRestResult usdtPaid(PaidParamReq req) {
        try {
            String id = req.getId();
            if ("5".equals(req.getType())) {
                // 代收订单
                MerchantCollectOrders merchantCollectOrders = merchantCollectOrdersMapper.selectById(id);
                if (ObjectUtils.isEmpty(merchantCollectOrders)) {
                    return KycRestResult.failed(ResultCode.UNFINISHED_ORDER_EXISTS.getCode());
                }
                if (!Objects.equals(merchantCollectOrders.getOrderStatus(), CollectionOrderStatusEnum.BE_PAID.getCode())) {
                    return KycRestResult.failed("merchant collection order status error");
                }
                // 检查是否到账
                Boolean checkFill = iRechargeTronDetailService.queryAndFillOrderId(merchantCollectOrders.getPlatformOrder(), merchantCollectOrders.getUsdtAddr(), merchantCollectOrders.getAmount());
                if (checkFill) {
                    // 账变
                    Boolean usdtAccountChangeUpdate = amountChangeUtil.insertOrUpdateAccountChange(merchantCollectOrders.getMerchantCode(), merchantCollectOrders.getAmount(), ChangeModeEnum.ADD, "USDT", merchantCollectOrders.getPlatformOrder(), AccountChangeEnum.COLLECTION, "USDT代收-手动完成", merchantCollectOrders.getMerchantOrder(), ChannelEnum.USDT.getName(), "", req.getBalanceType());
                    if (!usdtAccountChangeUpdate) {
                        log.error("usdt account change update failed");
                        throw new Exception("usdt account change update failed");
                    }
                    //订单总费用 = 订单费用 + 固定手续费
                    MerchantRatesConfig merchantRatesConfig = merchantRatesConfigService.getMerchantRatesConfigByCode("1", merchantCollectOrders.getPayType(), merchantCollectOrders.getMerchantCode());
                    BigDecimal cost = BigDecimal.ZERO;
                    //代收费率大于0才计算费用
                    if (merchantRatesConfig.getRates() != null && merchantRatesConfig.getRates().compareTo(BigDecimal.ZERO) > 0) {
                        //使用实际收到的钱计算订单费用
                        cost = merchantCollectOrders.getAmount().multiply((merchantRatesConfig.getRates().divide(BigDecimal.valueOf(100))));
                    }
                    BigDecimal fee = cost.add(merchantRatesConfig.getFixedFee());
                    Boolean usdtFeeAccountChangeUpdate = amountChangeUtil.insertOrUpdateAccountChange(merchantCollectOrders.getMerchantCode(), fee, ChangeModeEnum.SUB, "USDT", merchantCollectOrders.getPlatformOrder(), AccountChangeEnum.COLLECTION_FEE, "USDT代收手续费-手动完成", merchantCollectOrders.getMerchantOrder(), ChannelEnum.USDT.getName(), "", req.getBalanceType());
                    if (!usdtFeeAccountChangeUpdate) {
                        log.error("usdt fee account change update failed");
                        throw new Exception("usdt fee account change update failed");
                    }
                    // 修改订单状态
                    merchantCollectOrders.setOrderStatus(CollectionOrderStatusEnum.PAID.getCode());
                    merchantCollectOrders.setUpdateBy(req.getUpdateBy());
                    int i = merchantCollectOrdersMapper.updateById(merchantCollectOrders);
                    if (i != 1) {
                        log.error("merchant order status update failed");
                        throw new Exception("merchant order status update failed");
                    }
                    LambdaUpdateWrapper<TronAddress> lambdaUpdateWrapperTronAddress = new LambdaUpdateWrapper<>();
                    // 指定更新条件，地址
                    lambdaUpdateWrapperTronAddress.eq(TronAddress::getAddress, merchantCollectOrders.getUsdtAddr());
                    TronAddress tronAddress = tronAddressMapper.selectTronAddressByAddress(merchantCollectOrders.getUsdtAddr());
                    // 订单成功数+1
                    lambdaUpdateWrapperTronAddress.set(TronAddress::getOrderSuccessNum, tronAddress.getOrderSuccessNum() + 1);
                    tronAddressMapper.update(null, lambdaUpdateWrapperTronAddress);
                    // 回调
                    // 注册事务同步回调 事务提交成功后才执行以下操作
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //异步通知
                            TaskInfo taskInfo = new TaskInfo(merchantCollectOrders.getPlatformOrder(), TaskTypeEnum.DEPOSIT_NOTIFICATION.getCode(), System.currentTimeMillis());
                            rabbitMQService.sendRechargeSuccessCallbackNotification(taskInfo);
                        }
                    });
                } else {
                    return KycRestResult.failed(ResultCode.USDT_ORDER_NOT_RECEIPT);
                }
            } else if ("6".equals(req.getType())) {
                // 代付订单
                MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectById(id);
                if (ObjectUtils.isEmpty(merchantPaymentOrders)) {
                    return KycRestResult.failed(ResultCode.UNFINISHED_ORDER_EXISTS.getCode());
                }
                if (!Objects.equals(merchantPaymentOrders.getOrderStatus(), PaymentOrderStatusEnum.HANDLING.getCode())) {
                    return KycRestResult.failed("merchant payment order status error");
                }
                // 代付费用扣除
                Boolean usdtAmountChangeUpdate = amountChangeUtil.insertOrUpdateAccountChange(merchantPaymentOrders.getMerchantCode(), merchantPaymentOrders.getAmount(), ChangeModeEnum.SUB, "USDT", merchantPaymentOrders.getPlatformOrder(), AccountChangeEnum.PAYMENT, "USDT代付-手动完成", merchantPaymentOrders.getMerchantOrder(), ChannelEnum.USDT.getName(), "", req.getBalanceType());
                if (!usdtAmountChangeUpdate) {
                    log.error("usdt account change update failed");
                    throw new Exception("usdt account change update failed");
                }
                //订单总费用 = 订单费用 + 固定手续费
                BigDecimal cost = BigDecimal.ZERO;
                //代收费率大于0才计算费用
                if (merchantPaymentOrders.getCost() != null && merchantPaymentOrders.getCost().compareTo(BigDecimal.ZERO) > 0) {
                    //使用实际收到的钱计算订单费用
                    cost = merchantPaymentOrders.getCost();
                }
                BigDecimal fee = cost.add(merchantPaymentOrders.getFixedFee());
                Boolean usdtFeeAccountChangeUpdate = amountChangeUtil.insertOrUpdateAccountChange(merchantPaymentOrders.getMerchantCode(), fee, ChangeModeEnum.SUB, "USDT", merchantPaymentOrders.getPlatformOrder(), AccountChangeEnum.PAYMENT_FEE, "USDT代付手续费-手动完成", merchantPaymentOrders.getMerchantOrder(), ChannelEnum.USDT.getName(), "", req.getBalanceType());
                if (!usdtFeeAccountChangeUpdate) {
                    log.error("usdt fee account change update failed");
                    throw new Exception("usdt fee account change update failed");
                }
                // 修改订单状态
                merchantPaymentOrders.setOrderStatus(CollectionOrderStatusEnum.PAID.getCode());
                merchantPaymentOrders.setUpdateBy(req.getUpdateBy());
                int i = merchantPaymentOrdersMapper.updateById(merchantPaymentOrders);
                if (i != 1) {
                    throw new Exception("merchant order status update failed");
                }

//                LambdaUpdateWrapper<TronAddress> lambdaUpdateWrapperTronAddress = new LambdaUpdateWrapper<>();
//                // 指定更新条件，地址
//                lambdaUpdateWrapperTronAddress.eq(TronAddress::getAddress, merchantPaymentOrders.getUsdtAddr());
//                TronAddress tronAddress = tronAddressMapper.selectTronAddressByAddress(merchantPaymentOrders.getUsdtAddr());
//                // 订单成功数+1
//                lambdaUpdateWrapperTronAddress.set(TronAddress::getOrderSuccessNum, tronAddress.getOrderSuccessNum() + 1);
//                int update = tronAddressMapper.update(null, lambdaUpdateWrapperTronAddress);
//                if (update != 1) {
//                    throw new Exception("tron address count update failed");
//                }
                // 回调
                // 注册事务同步回调 事务提交成功后才执行以下操作
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //发送提现成功 异步延时回调通知 后期转正常
                        long millis = 3000L;
                        //发送提现延时回调的MQ消息
                        TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                        rabbitMQService.sendTimeoutTask(taskInfo, millis);
                    }
                });
            }
            return KycRestResult.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("已支付操作失败： e {}", e);
        }
        return KycRestResult.failed("paid failed");
    }


    @Override
    @Transactional
    public KycRestResult usdtUnPaid(PaidParamReq req) {
        try {
            String id = req.getId();
            if ("5".equals(req.getType())) {
                MerchantCollectOrders merchantCollectOrders = merchantCollectOrdersMapper.selectById(id);
                if (ObjectUtils.isEmpty(merchantCollectOrders)) {
                    return KycRestResult.failed(ResultCode.UNFINISHED_ORDER_EXISTS.getCode());
                }
                if (!Objects.equals(merchantCollectOrders.getOrderStatus(), CollectionOrderStatusEnum.BE_PAID.getCode())) {
                    return KycRestResult.failed("merchant collection order status error");
                }
                // 回调
                // 注册事务同步回调 事务提交成功后才执行以下操作
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //异步通知
                        TaskInfo taskInfo = new TaskInfo(merchantCollectOrders.getPlatformOrder(), TaskTypeEnum.DEPOSIT_NOTIFICATION.getCode(), System.currentTimeMillis());
                        rabbitMQService.sendRechargeSuccessCallbackNotification(taskInfo);
                    }
                });
                // 修改订单状态
                merchantCollectOrders.setOrderStatus(CollectionOrderStatusEnum.WAS_CANCELED.getCode());
                merchantCollectOrders.setUpdateBy(req.getUpdateBy());
                int i = merchantCollectOrdersMapper.updateById(merchantCollectOrders);
                if (i != 1) {
                    throw new Exception("merchant collection order update failed");
                }
            } else if ("6".equals(req.getType())) {
                MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectById(id);
                if (ObjectUtils.isEmpty(merchantPaymentOrders)) {
                    return KycRestResult.failed(ResultCode.UNFINISHED_ORDER_EXISTS.getCode());
                }
                if (!Objects.equals(merchantPaymentOrders.getOrderStatus(), PaymentOrderStatusEnum.HANDLING.getCode())) {
                    return KycRestResult.failed("merchant payment order status error");
                }
                //获取商户信息 加上排他行锁
                MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(merchantPaymentOrders.getMerchantCode());
                BigDecimal balance;
                int i1 = 0;
                LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode());
                BigDecimal allAmount = merchantPaymentOrders.getAmount().add(merchantPaymentOrders.getCost()).add(merchantPaymentOrders.getFixedFee());
                if (Objects.equals(req.getBalanceType(), BalanceTypeEnum.TRC20.getName())) {
                    balance = merchantInfo.getUsdtBalance();
                    if (merchantInfo.getPendingUsdtBalance().compareTo(allAmount) < 0) {
                        return KycRestResult.failed("usdt transaction balance not enough");
                    }
                    lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getUsdtBalance, balance.add(allAmount))
                            .set(MerchantInfo::getPendingUsdtBalance, merchantInfo.getPendingUsdtBalance().subtract(allAmount));
                    i1 = merchantInfoMapper.update(null, lambdaUpdateWrapperMerchantInfo);
                }
                if (Objects.equals(req.getBalanceType(), BalanceTypeEnum.TRX.getName())) {
                    balance = merchantInfo.getTrxBalance();
                    if (merchantInfo.getPendingTrxBalance().compareTo(allAmount) < 0) {
                        throw new Exception("usdt transaction balance not enough");
                    }
                    lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getTrxBalance, balance.add(allAmount))
                            .set(MerchantInfo::getPendingTrxBalance, merchantInfo.getPendingTrxBalance().subtract(allAmount));
                    i1 = merchantInfoMapper.update(null, lambdaUpdateWrapperMerchantInfo);
                }
                if (i1 != 1) {
                    throw new Exception("merchant payment order balance update failed");
                }
                // 回调
                // 注册事务同步回调 事务提交成功后才执行以下操作
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //发送提现成功 异步延时回调通知 后期转正常
                        long millis = 3000L;
                        //发送提现延时回调的MQ消息
                        TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                        rabbitMQService.sendTimeoutTask(taskInfo, millis);
                    }
                });
                merchantPaymentOrders.setOrderStatus(CollectionOrderStatusEnum.WAS_CANCELED.getCode());
                merchantPaymentOrders.setUpdateBy(req.getUpdateBy());
                int i = merchantPaymentOrdersMapper.updateById(merchantPaymentOrders);
                if (i != 1) {
                    throw new Exception("merchant payment order update failed");
                }
            }
            return KycRestResult.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("未支付操作失败： e {}", e);
        }
        return KycRestResult.failed("unPaid failed");
    }

    @Override
    @Transactional
    public KycRestResult manualConfirmation(PaidParamReq req) {
        String id = req.getId();
        try {
            MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectById(id);
            if (ObjectUtils.isEmpty(merchantPaymentOrders)) {
                return KycRestResult.failed(ResultCode.UNFINISHED_ORDER_EXISTS.getCode());
            }
            // 确认订单状态
            if (!merchantPaymentOrders.getOrderStatus().equals(PaymentOrderStatusEnum.TO_BE_REVIEWED.getCode())) {
                return KycRestResult.failed("merchant payment order status error");
            }
            // 修改订单状态
            LambdaUpdateWrapper<MerchantPaymentOrders> lambdaUpdateWrapperMerchantPaymentOrder = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapperMerchantPaymentOrder.eq(MerchantPaymentOrders::getId, id);
            // 银行卡/法币 需要改为待匹配
            if (BalanceTypeEnum.FCB.getName().equals(req.getBalanceType())) {
                lambdaUpdateWrapperMerchantPaymentOrder.set(MerchantPaymentOrders::getOrderStatus, PaymentOrderStatusEnum.BE_MATCHED.getCode());
            } else {
                lambdaUpdateWrapperMerchantPaymentOrder.set(MerchantPaymentOrders::getOrderStatus, PaymentOrderStatusEnum.HANDLING.getCode());
            }
            int update = merchantPaymentOrdersMapper.update(null, lambdaUpdateWrapperMerchantPaymentOrder);
            if (update != 1) {
                throw new Exception("merchant payment order update failed");
            }
            // 调用方法
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    if (req.getBalanceType().equals(BalanceTypeEnum.TRC20.getName())) {
                        usdtPaymentOrderService.usdtPaymentOrder(merchantPaymentOrders.getPlatformOrder());
                    }
                    if (req.getBalanceType().equals(BalanceTypeEnum.TRX.getName())) {
                        trxPaymentOrderService.trxPaymentOrder(merchantPaymentOrders.getPlatformOrder());
                    }
                    if (req.getBalanceType().equals(BalanceTypeEnum.FCB.getName())) {
                        //将代付订单存入到redis 供买入列表进行买入
                        // 事务提交后执行的Redis操作
                        BuyListVo buyListVo = new BuyListVo();
                        //订单号
                        buyListVo.setPlatformOrder(merchantPaymentOrders.getPlatformOrder());
                        //订单金额
                        buyListVo.setAmount(merchantPaymentOrders.getAmount());
                        //支付方式
                        buyListVo.setPayType(merchantPaymentOrders.getPayType());
                        //存入redis买入金额列表
                        redisUtil.addOrderIdToList(buyListVo, "1");
                        //推送最新的 金额列表给前端
                        memberSendAmountList.send();
                    }
                }
            });
            return KycRestResult.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("人工确认操作失败： e {}", e);
        }
        return KycRestResult.failed("manual confirmation failed");
    }

    @Override
    @Transactional
    public KycRestResult manualCancel(PaidParamReq req) {
        String id = req.getId();
        try {
            MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectById(id);
            if (ObjectUtils.isEmpty(merchantPaymentOrders)) {
                return KycRestResult.failed(ResultCode.UNFINISHED_ORDER_EXISTS.getCode());
            }
            // 确认订单状态
            if (!merchantPaymentOrders.getOrderStatus().equals(PaymentOrderStatusEnum.TO_BE_REVIEWED.getCode())) {
                return KycRestResult.failed("merchant payment order status error");
            }
            // 修改订单状态
            LambdaUpdateWrapper<MerchantPaymentOrders> lambdaUpdateWrapperMerchantPaymentOrder = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapperMerchantPaymentOrder.eq(MerchantPaymentOrders::getId, id)
                    .set(MerchantPaymentOrders::getOrderStatus, PaymentOrderStatusEnum.FAILED.getCode());
            int update = merchantPaymentOrdersMapper.update(null, lambdaUpdateWrapperMerchantPaymentOrder);
            if (update != 0) {
                throw new Exception("merchant payment order update failed");
            }
            // 释放代付订单锁定金额
            MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(merchantPaymentOrders.getMerchantCode());
            BigDecimal balance;
            int i1;
            LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode());
            BigDecimal allAmount = merchantPaymentOrders.getAmount().add(merchantPaymentOrders.getCost()).add(merchantPaymentOrders.getFixedFee());
            if (Objects.equals(req.getBalanceType(), BalanceTypeEnum.TRC20.getName())) {
                balance = merchantInfo.getUsdtBalance();
                if (merchantInfo.getPendingUsdtBalance().compareTo(allAmount) < 0) {
                    return KycRestResult.failed("usdt transaction balance not enough");
                }
                lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getUsdtBalance, balance.add(allAmount))
                        .set(MerchantInfo::getPendingUsdtBalance, merchantInfo.getPendingUsdtBalance().subtract(allAmount));
            }
            if (Objects.equals(req.getBalanceType(), BalanceTypeEnum.TRX.getName())) {
                balance = merchantInfo.getTrxBalance();
                if (merchantInfo.getPendingTrxBalance().compareTo(allAmount) < 0) {
                    throw new Exception("TRX transaction balance not enough");
                }
                lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getTrxBalance, balance.add(allAmount))
                        .set(MerchantInfo::getPendingTrxBalance, merchantInfo.getPendingTrxBalance().subtract(allAmount));
            }
            if (Objects.equals(req.getBalanceType(), BalanceTypeEnum.FCB.getName())) {
                balance = merchantInfo.getBalance();
                if (merchantInfo.getPendingBalance().compareTo(allAmount) < 0) {
                    throw new Exception("transaction balance not enough");
                }
                lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getBalance, balance.add(allAmount))
                        .set(MerchantInfo::getPendingBalance, merchantInfo.getPendingBalance().subtract(allAmount));
            }
            i1 = merchantInfoMapper.update(null, lambdaUpdateWrapperMerchantInfo);
            if (i1 != 1) {
                throw new Exception("merchant payment order balance update failed");
            }
            // 回调
            // 注册事务同步回调 事务提交成功后才执行以下操作
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    //发送提现成功 异步延时回调通知 后期转正常
                    long millis = 3000L;
                    //发送提现延时回调的MQ消息
                    TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                    rabbitMQService.sendTimeoutTask(taskInfo, millis);
                }
            });
            return KycRestResult.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("人工取消操作失败： e {}", e);
            return KycRestResult.failed(e.getMessage());
        }
    }


    private void check(LastOrderWarnDTO lastOrderWarnDTO, int limit, Set<String> set) {
        String diff = DurationCalculatorUtil.secondsBetween(lastOrderWarnDTO.getLastOrderCreateTime(), LocalDateTime.now(ZoneId.systemDefault()));
        int diffHour = (Integer.parseInt(diff) / 60 / 60);
        if (ObjectUtils.isNotEmpty(lastOrderWarnDTO)
                && ObjectUtils.isNotEmpty(lastOrderWarnDTO.getLastOrderCreateTime())
                && diffHour >= limit
        ) {
            set.add(lastOrderWarnDTO.getMerchantName());
        }
    }


    @Override
    public RestResult<MerchantInfoDTO> merchantDetail(Long id) {
        // 获取商户基本信息
        MerchantInfo merchantInfo = getById(id);
        if (Objects.isNull(merchantInfo)) {
            RestResult.failed("merchant does not exist");
        }
        MerchantInfoDTO merchantInfoDTO = new MerchantInfoDTO();
        BeanUtils.copyProperties(merchantInfo, merchantInfoDTO);

        // Step 1: 使用并行流分组收集订单和下发数据
        Map<String, List<MerchantCollectOrders>> collectionMap = merchantCollectOrdersService.collectionMap(merchantInfo.getCode());
        Map<String, List<MerchantPaymentOrders>> paymentMap = merchantPaymentOrdersService.paymentMap(merchantInfo.getCode());
        Map<String, List<ApplyDistributed>> applyDistributedMap = applyDistributedService.applyDistributedMap(merchantInfo.getCode());

        // Step 2: 获取商户费率信息
        // 1:银行卡,2:USDT,3:upi,6:TRX
        List<String> payTypes = Arrays.asList("1", "2", "3", "6");
        Map<String, String> collectionRates = merchantRatesConfigService.getMerchantRates(1, payTypes, merchantInfo.getCode());
        Map<String, String> paymentRates = merchantRatesConfigService.getMerchantRates(2, payTypes, merchantInfo.getCode());

        // Step 3: 计算统计信息
        //TRC20("1", "TRC20"), 3:法币余额,TRX("2", "TRX");
        List<String> balanceTypes = Arrays.asList("3", "1", "2");
        List<MerchantInfoDTO.MerchantCollectStatistics> collectStatisticsList = balanceTypes.parallelStream()
                .map(balanceType -> {
                    String collectionType = getCollectionType(balanceType);
                    String paymentType = getPaymentType(balanceType);

                    List<MerchantCollectOrders> collectOrders = collectionMap.getOrDefault(collectionType, Collections.emptyList());
                    List<MerchantPaymentOrders> paymentOrders = paymentMap.getOrDefault(paymentType, Collections.emptyList());
                    List<ApplyDistributed> applyDistributedList = applyDistributedMap.getOrDefault(balanceType, Collections.emptyList());
                    String collectionRate = collectionRates.getOrDefault(collectionType, "0");
                    String paymentRate = paymentRates.getOrDefault(paymentType, "0");

                    return calculateStatistics(collectOrders, paymentOrders, applyDistributedList, collectionRate, paymentRate, balanceType);
                })
                .collect(Collectors.toList());

        // Step 4: 创建并返回 DTO
        merchantInfoDTO.setMerchantCollectStatisticsList(collectStatisticsList);
        return RestResult.ok(merchantInfoDTO);
    }

    private MerchantInfoDTO.MerchantCollectStatistics calculateStatistics(List<MerchantCollectOrders> collectOrders,
                                                                          List<MerchantPaymentOrders> paymentOrders,
                                                                          List<ApplyDistributed> applyDistributedList,
                                                                          String collectionRate, String paymentRate,
                                                                          String balanceType) {
        // 计算代收订单的统计信息
        Long collectionOrdersNumber = (long) collectOrders.size();
        BigDecimal collectionAmountTotal = collectOrders.parallelStream()
                .map(MerchantCollectOrders::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal collectionCostTotal = collectOrders.parallelStream()
                .map(MerchantCollectOrders::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal collectionFeeTotal = collectOrders.parallelStream()
                .map(order -> Optional.ofNullable(order.getFixedFee())) // 将可能为null的值包装为Optional
                .filter(Optional::isPresent) // 过滤掉为null的Optional
                .map(Optional::get) // 获取包装的非null值
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加操作

        // 计算代付订单的统计信息
        Long paymentOrdersNumber = (long) paymentOrders.size();
        BigDecimal paymentAmountTotal = paymentOrders.parallelStream()
                .map(MerchantPaymentOrders::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paymentCostTotal = paymentOrders.parallelStream()
                .map(MerchantPaymentOrders::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paymentFeeTotal = paymentOrders.parallelStream()
                .map(order -> Optional.ofNullable(order.getFixedFee())) // 将可能为null的值包装为Optional
                .filter(Optional::isPresent) // 过滤掉为null的Optional
                .map(Optional::get) // 获取包装的非null值
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 累加操作


        // 计算下发数据的统计信息
        Long issuedNumber = (long) applyDistributedList.size();
        BigDecimal issuedTotal = applyDistributedList.parallelStream()
                .map(ApplyDistributed::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 创建并设置统计信息对象
        MerchantInfoDTO.MerchantCollectStatistics statistics = new MerchantInfoDTO.MerchantCollectStatistics();
        statistics.setCurrency(balanceType);
        statistics.setCollectionOrdersNumber(collectionOrdersNumber);
        statistics.setCollectionAmountTotal(collectionAmountTotal);
        statistics.setCollectionRates(collectionRate);
        statistics.setCollectionCostTotal(collectionCostTotal.add(collectionFeeTotal));

        statistics.setPaymentOrdersNumber(paymentOrdersNumber);
        statistics.setPaymentAmountTotal(paymentAmountTotal);
        statistics.setPaymentCostTotal(paymentCostTotal.add(paymentFeeTotal));
        statistics.setPaymentRates(paymentRate);

        statistics.setIssuedNumber(issuedNumber);
        statistics.setIssuedTotal(issuedTotal);

        return statistics;
    }

    /**
     * 代收：UPI,TRC-20,TRX
     */
    private String getCollectionType(String balanceType) {
        switch (balanceType) {
            case "1":
                return "2";
            case "2":
                return "6";
            case "3":
                return "3";
            default:
                return "";
        }
    }

    /**
     * 代付：银行卡，TRC-20,TRX
     */
    private String getPaymentType(String balanceType) {
        switch (balanceType) {
            case "1":
                return "2";
            case "2":
                return "6";
            case "3":
                return "1";
            default:
                return "";
        }
    }

}
