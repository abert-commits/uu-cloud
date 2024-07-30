package org.uu.wallet.util;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.result.ResultCode;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.common.web.exception.BizException;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.*;
import org.uu.wallet.service.IBuyService;
import org.uu.wallet.service.IMatchPoolService;
import org.uu.wallet.service.IMemberAccountChangeService;
import org.uu.wallet.webSocket.MemberMessageSender;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Admin
 */
@Slf4j
@Component
public class AmountChangeUtil {

    @Resource
    MerchantInfoMapper merchantInfoMapper;
    @Resource
    AccountChangeMapper accountChangeMapper;
    @Resource
    RedissonUtil redissonUtil;
    @Resource
    MemberAccountChangeMapper memberAccountChangeMapper;
    @Resource
    MemberInfoMapper memberInfoMapper;
    @Autowired
    CollectionOrderMapper collectionOrderMapper;
    @Autowired
    MatchingOrderMapper matchingOrderMapper;
    @Autowired
    PaymentOrderMapper paymentOrderMapper;
    @Autowired
    CollectionInfoMapper collectionInfoMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IMemberAccountChangeService memberAccountChangeService;

    @Autowired
    TransactionDefinition transactionDefinition;

    @Resource
    private MerchantCollectOrdersMapper merchantCollectOrdersMapper;

    @Resource
    private MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;

    @Resource
    private MerchantRatesConfigMapper merchantRatesConfigMapper;
    @Autowired
    private DelegationOrderMapper delegationOrderMapper;
    @Autowired
    private DelegationOrderUtil delegationOrderUtil;

    public AmountChangeUtil() {
    }

    /**
     * 更新商户余额并记录账变
     *
     * @param merchantCode    商户code
     * @param changeAmount    账变金额
     * @param changeModeEnum  账变类型
     * @param currentcy       币种
     * @param orderNo         订单号-平台订单号
     * @param orderCreateTime 订单创建时间       '2023-10-27'
     * @param merchantOrderNo 商户订单号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean insertChangeAmountRecord(
            String merchantCode,
            BigDecimal changeAmount,
            ChangeModeEnum changeModeEnum,
            String currentcy,
            String orderNo,
            AccountChangeEnum accountChangeEnum,
            String orderCreateTime,
            String remark,
            String merchantOrderNo,
            String paymentChannel,
            String usdtAddr,
            String balanceType
    ) {

        log.info("开始记录商户余额账变,商户ID->{},账变金额->{},账变类型->{},订单号->{}", merchantCode, changeAmount,
                changeModeEnum.getName(), orderNo);
        String key = "uu-wallet" + merchantCode;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;
        BigDecimal commission = BigDecimal.ZERO;

        try {


            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {

                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(merchantCode);
                if (ObjectUtils.isEmpty(merchantInfo)) {
                    log.error("商户不存在,商户ID->{}", merchantCode);
                    throw new BizException(ResultCode.MERCHANT_NOT_EXIST);
                }
                if (accountChangeEnum.getCode().equals(AccountChangeEnum.PAYMENT.getCode())) {
                    commission = merchantInfo.getTransferRate().multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.COLLECTION.getCode())) {
                    commission = merchantInfo.getPayRate().multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                }
                AccountChange accountChange = new AccountChange();
                BigDecimal balance = merchantInfo.getBalance();
                accountChange.setBeforeChange(balance);
                accountChange.setAmountChange(changeAmount);
                //账变后法币余额
                BigDecimal afterFcbBalance = BigDecimal.ZERO;
                //账变后usdt余额
                BigDecimal afterUsdtBalance = BigDecimal.ZERO;
                //账变TRX余额
                BigDecimal afterTrxBalance = BigDecimal.ZERO;
                if (changeModeEnum.getCode().equals(ChangeModeEnum.ADD.getCode())) {
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {
                        afterFcbBalance = balance.add(changeAmount);
                    }
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {
                        afterUsdtBalance = merchantInfo.getUsdtBalance().add(changeAmount);
                    }
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {
                        afterTrxBalance = merchantInfo.getTrxBalance().add(changeAmount);
                    }
                } else {
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {
                        if (balance.compareTo(changeAmount) < 0) {
                            log.error("商户余额不足,商户ID->{}", merchantCode);
                            throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
                        }
                        afterFcbBalance = balance.subtract(changeAmount);
                    }

                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {
                        if (merchantInfo.getUsdtBalance().compareTo(changeAmount) < 0) {
                            log.error("商户USDT不足,商户ID->{}", merchantCode);
                            throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
                        }
                        afterUsdtBalance = merchantInfo.getUsdtBalance().subtract(changeAmount);
                    }

                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {
                        if (merchantInfo.getTrxBalance().compareTo(changeAmount) < 0) {
                            log.error("商户TRX不足,商户ID->{}", merchantCode);
                            throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
                        }
                        afterTrxBalance = merchantInfo.getTrxBalance().subtract(changeAmount);
                    }
                    log.info("商户ID->{},账变金额->{},账变类型->{},订单号->{},账变前金额->{},账变后金额->{}", merchantCode, changeAmount,
                            changeModeEnum.getName(), orderNo, accountChange.getBeforeChange(), afterFcbBalance);
                }

                accountChange.setMerchantCode(merchantCode);
                accountChange.setCurrentcy(currentcy);
                accountChange.setChangeMode(changeModeEnum.getCode());
                accountChange.setOrderNo(merchantOrderNo);
                accountChange.setChangeType(Integer.parseInt(accountChangeEnum.getCode()));
                accountChange.setCreateTime(LocalDateTime.now(ZoneId.systemDefault()));
                accountChange.setRemark(remark);
                accountChange.setCommission(commission);
                accountChange.setPaymentChannel(paymentChannel);
                accountChange.setMerchantName(merchantInfo.getUsername());
                accountChange.setMerchantOrder(orderNo);
                if (StringUtils.isNotBlank(balanceType)) {
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {
                        log.info("商户账变USDT>======账变类型 :{}", balanceType.toUpperCase());
                        accountChange.setUsdtBalance(afterUsdtBalance);
                        accountChange.setBeforeChange(merchantInfo.getUsdtBalance());
                        accountChange.setAfterChange(afterUsdtBalance);
                        merchantInfo.setUsdtBalance(afterUsdtBalance.setScale(6, RoundingMode.HALF_UP));
                    }
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {
                        log.info("商户账变FCB>======账变类型 :{}", balanceType.toUpperCase());
                        accountChange.setAfterChange(afterFcbBalance);
                        accountChange.setBeforeChange(merchantInfo.getBalance());
                        accountChange.setAfterChange(afterFcbBalance);
                        merchantInfo.setBalance(afterFcbBalance.setScale(6, RoundingMode.HALF_UP));
                    }
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {
                        log.info("商户账变TRX>======账变类型 :{}", balanceType.toUpperCase());
                        accountChange.setTrxBalance(afterTrxBalance);
                        accountChange.setBeforeChange(merchantInfo.getTrxBalance());
                        accountChange.setAfterChange(afterTrxBalance);
                        merchantInfo.setTrxBalance(afterTrxBalance.setScale(6, RoundingMode.HALF_UP));
                    }
                }
                if (ObjectUtils.isNotEmpty(usdtAddr)) {
                    accountChange.setUsdtAddr(usdtAddr);
                }
                // 代收-玩家支付
                if (accountChangeEnum.getCode().equals(AccountChangeEnum.COLLECTION.getCode())) {

                    // 统计累积业务数据

                    merchantInfo.setTotalPayAmount(merchantInfo.getTotalPayAmount().add(changeAmount));
                    merchantInfo.setTotalPayCount(merchantInfo.getTotalPayCount() + 1);
                    merchantInfo.setTotalPayFee(merchantInfo.getTotalPayFee().add(merchantInfo.getPayRate().multiply(changeAmount)
                            .divide(new BigDecimal("100")).setScale(2)));


                    // 代收-玩家提现
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.PAYMENT.getCode())) {

                    // 统计累积业务数据

                    merchantInfo.setTotalWithdrawAmount(merchantInfo.getTotalWithdrawAmount().add(changeAmount));
                    merchantInfo.setTotalWithdrawCount(merchantInfo.getTotalWithdrawCount() + 1);
                    merchantInfo.setTotalWithdrawFee(merchantInfo.getTotalWithdrawFee().add(merchantInfo.getTransferRate().multiply(changeAmount)
                            .divide(new BigDecimal("100")).setScale(2)));


                    // 下分-商户提现
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.WITHDRAW.getCode())) {
                    merchantInfo.setTransferDownAmount(merchantInfo.getTransferDownAmount().add(changeAmount));
                    merchantInfo.setTransferDownCount(merchantInfo.getTransferDownCount() + 1);
                    // 上分-商户充值
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.RECHARGE.getCode())) {
                    merchantInfo.setTransferUpAmount(merchantInfo.getTransferUpAmount().add(changeAmount));
                    merchantInfo.setTransferUpCount(merchantInfo.getTransferUpCount() + 1);
                }

                int i = merchantInfoMapper.updateById(merchantInfo);
                if (i < 1) throw new Exception("更新商户余额失败");
                int j = accountChangeMapper.insert(accountChange);
                if (j < 1) throw new Exception("新增商户账变失败");
            } else {
                log.info("获取锁失败回滚操作,商户ID->{},账变金额->{},账变类型->{},订单号->{}", merchantCode, changeAmount,
                        changeModeEnum.getName(), orderNo);
                throw new Exception("获取锁失败,回滚操作");
            }
        } catch (Exception e) {
            log.error("商户ID->{},账变金额->{},账变类型->{},订单号->{},异常信息->{}", merchantCode, changeAmount,
                    changeModeEnum.getName(), orderNo, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Boolean.FALSE;
        } finally {
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Boolean.TRUE;
    }

    /**
     * 更新商户余额产生账变化 simon
     *
     * @param merchantCode      商户号
     * @param changeAmount      账变金额
     * @param changeModeEnum    账变类型 add 收入  sub支出
     * @param currentcy         币种
     * @param orderNo           订单号-平台订单号
     * @param accountChangeEnum 账变类型
     * @param remark            备注
     * @param merchantOrderNo   商户订单号
     * @param paymentChannel
     * @param usdtAddr          USDT地址
     * @param balanceType       余额类型
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean insertOrUpdateAccountChange(
            String merchantCode,
            BigDecimal changeAmount,
            ChangeModeEnum changeModeEnum,
            String currentcy,
            String orderNo,
            AccountChangeEnum accountChangeEnum,
            String remark,
            String merchantOrderNo,
            String paymentChannel,
            String usdtAddr,
            String balanceType
    ) {

        //商户账变
        log.info("商户账变, 商户号: {}, 账变金额: {}, 账变类型: {}, 订单号: {}, 账变类型: {} 余额类型: {}",
                merchantCode, changeAmount, changeModeEnum.getName(), orderNo, accountChangeEnum, balanceType);

        //分布式锁
        String key = "uu-wallet" + merchantCode;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {

                //获取商户信息 加上排他行锁
                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(merchantCode);

                if (merchantInfo == null){
                    log.error("商户账变, 失败, 获取商户信息失败, 商户号: {}, 账变金额: {}, 账变类型: {}, 订单号: {}, 账变类型: {} 余额类型: {}",
                            merchantCode, changeAmount, changeModeEnum.getName(), orderNo, accountChangeEnum, balanceType);
                    throw new BizException(ResultCode.MERCHANT_NOT_EXIST);
                }

                //-------------------------
                //法币-商户余额
                BigDecimal balance = merchantInfo.getBalance();
                //法币-商户交易中金额
                BigDecimal pendingBalance = merchantInfo.getPendingBalance();
                //法币-商户账变前余额 余额+交易中金额
                BigDecimal beforeChangeBalance = balance.add(pendingBalance);
                //-------------------------

                //USDT-商户余额
                BigDecimal usdtBalance = merchantInfo.getUsdtBalance();
                //USDT-交易中金额
                BigDecimal pendingUsdtBalance = merchantInfo.getPendingUsdtBalance();
                //USDT-商户账变前余额 余额 + 交易中金额
                BigDecimal beforeChangeUsdtBalance = usdtBalance.add(pendingUsdtBalance);
                //-------------------------

                //TRX-商户余额
                BigDecimal trxBalance = merchantInfo.getTrxBalance();
                //TRX-商户交易中金额
                BigDecimal pendingTrxBalance = merchantInfo.getPendingTrxBalance();
                //TRX-商户账变前余额 余额+交易中金额
                BigDecimal beforeChangeTrxBalance = trxBalance.add(pendingTrxBalance);
                //-------------------------


                //添加商户账变记录表
                AccountChange accountChange = new AccountChange();

                //账变金额
                accountChange.setAmountChange(changeAmount);

                //判断是收入还是支出
                if (changeModeEnum.getCode().equals(ChangeModeEnum.ADD.getCode())) {
                    //收入账变

                    //增加法币余额
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {

                        //账变前余额 商户余额 + 交易中金额
                        accountChange.setBeforeChange(beforeChangeBalance);

                        //账变后余额 总余额+账变金额
                        accountChange.setAfterChange(beforeChangeBalance.add(changeAmount));

                        //**更新商户余额 可用余额+账变金额
                        merchantInfo.setBalance(balance.add(changeAmount));

                        log.info("商户账变 增加商户法币余额, 订单号: {}", orderNo);
                    }

                    //增加USDT余额
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {

                        //账变前余额 商户余额+交易中金额
                        accountChange.setBeforeChange(beforeChangeUsdtBalance);

                        //账变后余额 总余额+账变金额
                        accountChange.setAfterChange(beforeChangeUsdtBalance.add(changeAmount));

                        //**更新商户余额  可用余额+账变金额
                        merchantInfo.setUsdtBalance(usdtBalance.add(changeAmount));

                        log.info("商户账变 增加商户USDT余额, 订单号: {}", orderNo);
                    }

                    //增加TRX余额
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {

                        //账变前余额 商户余额+交易中金额
                        accountChange.setBeforeChange(beforeChangeTrxBalance);

                        //账变后余额 总余额+账变金额
                        accountChange.setAfterChange(beforeChangeTrxBalance.add(changeAmount));

                        //**更新商户余额  可用余额+账变金额
                        merchantInfo.setTrxBalance(trxBalance.add(changeAmount));

                        log.info("商户账变 增加商户TRX余额, 订单号: {}", orderNo);

                    }
                } else {

                    //判断如果是代收费用 那么是减商户余额
                    if (accountChangeEnum.getCode().equals(AccountChangeEnum.COLLECTION_FEE.getCode())) {

                        //减少法币余额
                        if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {

                            //账变前余额 商户余额 + 交易中金额
                            accountChange.setBeforeChange(beforeChangeBalance);

                            //账变后余额 总余额-账变金额
                            accountChange.setAfterChange(beforeChangeBalance.subtract(changeAmount));

                            //**更新商户余额 可用余额-账变金额
                            merchantInfo.setBalance(balance.subtract(changeAmount));

                            log.info("商户账变 代收费用或下发 减少商户法币余额, 订单号: {}", orderNo);
                        }

                        //减少USDT余额
                        if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {

                            //账变前余额 商户余额+交易中金额
                            accountChange.setBeforeChange(beforeChangeUsdtBalance);

                            //账变后余额 总余额-账变金额
                            accountChange.setAfterChange(beforeChangeUsdtBalance.subtract(changeAmount));

                            //**更新商户余额  可用余额-账变金额
                            merchantInfo.setUsdtBalance(usdtBalance.subtract(changeAmount));

                            log.info("商户账变 代收费用或下发 减少商户USDT余额, 订单号: {}", orderNo);
                        }

                        //减少TRX余额
                        if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {

                            //账变前余额 商户余额+交易中金额
                            accountChange.setBeforeChange(beforeChangeTrxBalance);

                            //账变后余额 总余额-账变金额
                            accountChange.setAfterChange(beforeChangeTrxBalance.subtract(changeAmount));

                            //**更新商户余额  可用余额-账变金额
                            merchantInfo.setTrxBalance(trxBalance.subtract(changeAmount));

                            log.info("商户账变 代收费用或下发 减少商户USDT余额, 订单号: {}", orderNo);
                        }

                    } else {
                        //非代收费用或下发的支出 目前做统一处理, 扣除商户交易中金额
                        //代付或代付费用

                        //减少法币交易中金额
                        if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {

                            //账变前余额 商户余额 + 交易中金额
                            accountChange.setBeforeChange(beforeChangeBalance);

                            //账变后余额 总余额-账变金额
                            accountChange.setAfterChange(beforeChangeBalance.subtract(changeAmount));

                            //**更新商户交易中金额 交易中金额-账变金额
                            merchantInfo.setPendingBalance(pendingBalance.subtract(changeAmount));

                            log.info("商户账变 代付或代付费用 减少商户法币交易中金额, 订单号: {}", orderNo);
                        }

                        //减少USDT交易中金额
                        if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {

                            //账变前余额 商户余额+交易中金额
                            accountChange.setBeforeChange(beforeChangeUsdtBalance);

                            //账变后余额 总余额+账变金额
                            accountChange.setAfterChange(beforeChangeUsdtBalance.subtract(changeAmount));

                            //**更新商户余额  交易中金额-账变金额
                            merchantInfo.setPendingUsdtBalance(pendingUsdtBalance.subtract(changeAmount));

                            log.info("商户账变 代付或代付费用 减少商户USDT交易中金额, 订单号: {}", orderNo);
                        }

                        //减少TRX交易中金额
                        if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {

                            //账变前余额 商户余额+交易中金额
                            accountChange.setBeforeChange(beforeChangeTrxBalance);

                            //账变后余额 总余额-账变金额
                            accountChange.setAfterChange(beforeChangeTrxBalance.subtract(changeAmount));

                            //**更新商户余额  交易中金额-账变金额
                            merchantInfo.setPendingTrxBalance(pendingTrxBalance.subtract(changeAmount));

                            log.info("商户账变 代付或代付费用 减少商户TRX交易中金额, 订单号: {}", orderNo);
                        }
                    }
                }

                //商户号
                accountChange.setMerchantCode(merchantCode);

                //币种
                accountChange.setCurrentcy(currentcy);

                //账变类型：add-增加, sub-支出
                accountChange.setChangeMode(changeModeEnum.getCode());

                //订单号 (这里之前存的是商户号 暂时和之前保持一样 后面考虑要不要换成平台订单号)
                accountChange.setOrderNo(merchantOrderNo);

                //账变类型 代收 代付 代收付费用等
                accountChange.setChangeType(Integer.parseInt(accountChangeEnum.getCode()));

                //创建时间
                accountChange.setCreateTime(LocalDateTime.now(ZoneId.systemDefault()));

                //备注
                accountChange.setRemark(remark);

                //支付通道
                accountChange.setPaymentChannel(paymentChannel);

                //商户名称
                accountChange.setMerchantName(merchantInfo.getUsername());

                //平台订单号 这里存反了
                accountChange.setMerchantOrder(orderNo);


                //如果传了USDT地址 那么赋值
                if (StringUtils.isNotBlank(usdtAddr)) {
                    accountChange.setUsdtAddr(usdtAddr);
                }

                // 代收-玩家支付
                if (accountChangeEnum.getCode().equals(AccountChangeEnum.COLLECTION.getCode())) {

                    // 统计累积业务数据

                    merchantInfo.setTotalPayAmount(merchantInfo.getTotalPayAmount().add(changeAmount));
                    merchantInfo.setTotalPayCount(merchantInfo.getTotalPayCount() + 1);
                    merchantInfo.setTotalPayFee(merchantInfo.getTotalPayFee().add(merchantInfo.getPayRate().multiply(changeAmount)
                            .divide(new BigDecimal("100")).setScale(2)));

                    // 代收-玩家提现
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.PAYMENT.getCode())) {

                    // 统计累积业务数据

                    merchantInfo.setTotalWithdrawAmount(merchantInfo.getTotalWithdrawAmount().add(changeAmount));
                    merchantInfo.setTotalWithdrawCount(merchantInfo.getTotalWithdrawCount() + 1);
                    merchantInfo.setTotalWithdrawFee(merchantInfo.getTotalWithdrawFee().add(merchantInfo.getTransferRate().multiply(changeAmount)
                            .divide(new BigDecimal("100")).setScale(2)));

                    // 下分-商户提现
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.WITHDRAW.getCode())) {
                    merchantInfo.setTransferDownAmount(merchantInfo.getTransferDownAmount().add(changeAmount));
                    merchantInfo.setTransferDownCount(merchantInfo.getTransferDownCount() + 1);
                    // 上分-商户充值
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.RECHARGE.getCode())) {
                    merchantInfo.setTransferUpAmount(merchantInfo.getTransferUpAmount().add(changeAmount));
                    merchantInfo.setTransferUpCount(merchantInfo.getTransferUpCount() + 1);
                }
                int i = merchantInfoMapper.updateById(merchantInfo);
                if (i < 1) throw new Exception("更新商户余额失败");
                int j = accountChangeMapper.insert(accountChange);
                if (j < 1) throw new Exception("新增商户账变失败");
            } else {
                log.error("商户账变, 失败, 获取锁失败, 商户号: {}, 账变金额: {}, 账变类型: {}, 订单号: {}, 账变类型: {} 余额类型: {}",
                        merchantCode, changeAmount, changeModeEnum.getName(), orderNo, accountChangeEnum, balanceType);

                throw new Exception("获取锁失败,回滚操作");
            }
        } catch (Exception e) {
            log.error("商户账变, 失败, 异常失败, 商户号: {}, 账变金额: {}, 账变类型: {}, 订单号: {}, 账变类型: {} 余额类型: {}, 异常信息: {}",
                    merchantCode, changeAmount, changeModeEnum.getName(), orderNo, accountChangeEnum, balanceType, e.getMessage());
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Boolean.FALSE;
        } finally {

            //释放分布式锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Boolean.TRUE;
    }

    /**
     * 更新商户余额产生账变化-仅用于商户的上分下发
     *
     * @param merchantCode    商户code
     * @param changeAmount    账变金额
     * @param changeModeEnum  账变类型
     * @param currentcy       币种
     * @param orderNo         订单号-平台订单号
     * @param merchantOrderNo 商户订单号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean merchantAccountChange(
            String merchantCode,
            BigDecimal changeAmount,
            ChangeModeEnum changeModeEnum,
            String currentcy,
            String orderNo,
            AccountChangeEnum accountChangeEnum,
            String remark,
            String merchantOrderNo,
            String paymentChannel,
            String usdtAddr,
            String balanceType
    ) {

        log.info("开始记录商户余额账变,商户ID->{},账变金额->{},账变类型->{},订单号->{}", merchantCode, changeAmount,
                changeModeEnum.getName(), orderNo);
        String key = "uu-wallet-merchant" + merchantCode;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;
        BigDecimal commission = BigDecimal.ZERO;
        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {

                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(merchantCode);
                if (ObjectUtils.isEmpty(merchantInfo)) {
                    log.error("商户不存在,商户ID->{}", merchantCode);
                    throw new BizException(ResultCode.MERCHANT_NOT_EXIST);
                }
                if (accountChangeEnum.getCode().equals(AccountChangeEnum.PAYMENT.getCode())) {
                    commission = merchantInfo.getTransferRate().multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.COLLECTION.getCode())) {
                    commission = merchantInfo.getPayRate().multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                }
                AccountChange accountChange = new AccountChange();
                BigDecimal balance = merchantInfo.getBalance();
                accountChange.setAmountChange(changeAmount);
                if (changeModeEnum.getCode().equals(ChangeModeEnum.ADD.getCode())) {
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {
                        BigDecimal afterFcbBalance = balance.add(changeAmount);
                        accountChange.setBeforeChange(balance);
                        accountChange.setAfterChange(afterFcbBalance);
                        merchantInfo.setBalance(afterFcbBalance.setScale(6, RoundingMode.HALF_UP));
                        log.info("更新法币余额：{}", merchantInfo.getBalance());
                    }
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {
                        BigDecimal afterUsdtBalance = merchantInfo.getUsdtBalance().add(changeAmount);
                        accountChange.setBeforeChange(merchantInfo.getUsdtBalance());
                        accountChange.setAfterChange(afterUsdtBalance);
                        merchantInfo.setUsdtBalance(afterUsdtBalance.setScale(6, RoundingMode.HALF_UP));
                        log.info("更新USDT余额：{}", merchantInfo.getUsdtBalance());

                    }
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {
                        BigDecimal afterTrxBalance = merchantInfo.getTrxBalance().add(changeAmount);
                        accountChange.setBeforeChange(merchantInfo.getTrxBalance());
                        accountChange.setAfterChange(afterTrxBalance);
                        merchantInfo.setTrxBalance(afterTrxBalance.setScale(6, RoundingMode.HALF_UP));
                        log.info("更新TRX余额：{}", merchantInfo.getTrxBalance());

                    }
                } else {
                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.FCB.getName())) {
                        if (merchantInfo.getBalance().compareTo(changeAmount) < 0) {
                            log.error("商户余额不足,商户ID->{}", merchantCode);
                            throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
                        }
                        BigDecimal afterFcbBalance = merchantInfo.getBalance().subtract(changeAmount);
                        accountChange.setBeforeChange(balance);
                        accountChange.setAfterChange(afterFcbBalance);
                        merchantInfo.setBalance(afterFcbBalance.setScale(6, RoundingMode.HALF_UP));
                        log.info("更新卖出法币交易中余额：{}", merchantInfo.getPendingBalance());
                    }

                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRC20.getName())) {
                        if (merchantInfo.getUsdtBalance().compareTo(changeAmount) < 0) {
                            log.error("商户USDT不足,商户ID->{}", merchantCode);
                            throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
                        }
                        BigDecimal afterUsdtBalance = merchantInfo.getUsdtBalance().subtract(changeAmount);
                        accountChange.setBeforeChange(merchantInfo.getUsdtBalance());
                        accountChange.setAfterChange(afterUsdtBalance);
                        merchantInfo.setUsdtBalance(afterUsdtBalance.setScale(6, RoundingMode.HALF_UP));
                        log.info("更新卖出USDT交易中余额：{}", merchantInfo.getPendingUsdtBalance());
                    }

                    if (balanceType.toUpperCase().equals(BalanceTypeEnum.TRX.getName())) {
                        if (merchantInfo.getTrxBalance().compareTo(changeAmount) < 0) {
                            log.error("商户TRX不足,商户ID->{}", merchantCode);
                            throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
                        }
                        BigDecimal afterTrxBalance = merchantInfo.getTrxBalance().subtract(changeAmount);
                        accountChange.setBeforeChange(merchantInfo.getTrxBalance());
                        accountChange.setAfterChange(afterTrxBalance);
                        merchantInfo.setTrxBalance(afterTrxBalance.setScale(6, RoundingMode.HALF_UP));
                        log.info("更新卖出TRX交易中余额：{}", merchantInfo.getPendingTrxBalance());
                    }

                }

                accountChange.setMerchantCode(merchantCode);
                accountChange.setCurrentcy(currentcy);
                accountChange.setChangeMode(changeModeEnum.getCode());
                accountChange.setOrderNo(merchantOrderNo);
                accountChange.setChangeType(Integer.parseInt(accountChangeEnum.getCode()));
                accountChange.setCreateTime(LocalDateTime.now(ZoneId.systemDefault()));
                accountChange.setRemark(remark);
                accountChange.setCommission(commission);
                accountChange.setPaymentChannel(paymentChannel);
                accountChange.setMerchantName(merchantInfo.getUsername());
                accountChange.setMerchantOrder(orderNo);
                if (ObjectUtils.isNotEmpty(usdtAddr)) {
                    accountChange.setUsdtAddr(usdtAddr);
                }
                // 代收-玩家支付
                if (accountChangeEnum.getCode().equals(AccountChangeEnum.COLLECTION.getCode())) {

                    // 统计累积业务数据

                    merchantInfo.setTotalPayAmount(merchantInfo.getTotalPayAmount().add(changeAmount));
                    merchantInfo.setTotalPayCount(merchantInfo.getTotalPayCount() + 1);
                    merchantInfo.setTotalPayFee(merchantInfo.getTotalPayFee().add(merchantInfo.getPayRate().multiply(changeAmount)
                            .divide(new BigDecimal("100")).setScale(2)));

                    // 代收-玩家提现
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.PAYMENT.getCode())) {

                    // 统计累积业务数据

                    merchantInfo.setTotalWithdrawAmount(merchantInfo.getTotalWithdrawAmount().add(changeAmount));
                    merchantInfo.setTotalWithdrawCount(merchantInfo.getTotalWithdrawCount() + 1);
                    merchantInfo.setTotalWithdrawFee(merchantInfo.getTotalWithdrawFee().add(merchantInfo.getTransferRate().multiply(changeAmount)
                            .divide(new BigDecimal("100")).setScale(2)));

                    // 下分-商户提现
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.WITHDRAW.getCode())) {
                    merchantInfo.setTransferDownAmount(merchantInfo.getTransferDownAmount().add(changeAmount));
                    merchantInfo.setTransferDownCount(merchantInfo.getTransferDownCount() + 1);
                    // 上分-商户充值
                } else if (accountChangeEnum.getCode().equals(AccountChangeEnum.RECHARGE.getCode())) {
                    merchantInfo.setTransferUpAmount(merchantInfo.getTransferUpAmount().add(changeAmount));
                    merchantInfo.setTransferUpCount(merchantInfo.getTransferUpCount() + 1);
                }
                int i = merchantInfoMapper.updateById(merchantInfo);
                if (i < 1) throw new Exception("更新商户余额失败");
                int j = accountChangeMapper.insert(accountChange);
                if (j < 1) throw new Exception("新增商户账变失败");
            } else {
                log.info("获取锁失败回滚操作,商户ID->{},账变金额->{},账变类型->{},订单号->{}", merchantCode, changeAmount,
                        changeModeEnum.getName(), orderNo);
                throw new Exception("获取锁失败,回滚操作");
            }
        } catch (Exception e) {
            log.error("商户ID->{},账变金额->{},账变类型->{},订单号->{},异常信息->{}", merchantCode, changeAmount,
                    changeModeEnum.getName(), orderNo, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Boolean.FALSE;
        } finally {
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Boolean.TRUE;
    }

    /**
     * 商户下发申请，更新余额到交易中，不产生账变
     *
     * @param merchantCode   商户code
     * @param changeAmount   账变金额
     * @param changeModeEnum 账变类型
     * @param orderNo        订单号-平台订单号
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean updateMerchantBalance(
            String merchantCode,
            BigDecimal changeAmount,
            ChangeModeEnum changeModeEnum,
            String orderNo,
            AccountChangeEnum accountChangeEnum,
            String balanceType
    ) {

        log.info("开始记录商户余额账变,商户ID->{},账变金额->{},账变类型->{},订单号->{}", merchantCode, changeAmount,
                changeModeEnum.getName(), orderNo);
        String key = "uu-wallet-apply" + merchantCode;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;
        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {
                MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoById(merchantCode);
                if (ObjectUtils.isEmpty(merchantInfo)) {
                    log.error("商户不存在,商户ID->{}", merchantCode);
                    throw new BizException(ResultCode.MERCHANT_NOT_EXIST);
                }

                switch (balanceType.toUpperCase()) {
                    case "FCB":
                        operateMerchantBalance(merchantInfo.getBalance(), merchantInfo.getPendingBalance(), changeAmount,
                                merchantInfo::setBalance, merchantInfo::setPendingBalance, merchantCode, accountChangeEnum);
                        log.info("更新法币交易中余额：{}", merchantInfo.getPendingBalance());
                        break;
                    case "TRC20":
                        operateMerchantBalance(merchantInfo.getUsdtBalance(), merchantInfo.getPendingUsdtBalance(), changeAmount,
                                merchantInfo::setUsdtBalance, merchantInfo::setPendingUsdtBalance, merchantCode, accountChangeEnum);
                        log.info("更新USDT交易中余额：{}", merchantInfo.getPendingUsdtBalance());
                        break;
                    case "TRX":
                        operateMerchantBalance(merchantInfo.getTrxBalance(), merchantInfo.getPendingTrxBalance(), changeAmount,
                                merchantInfo::setTrxBalance, merchantInfo::setPendingTrxBalance, merchantCode, accountChangeEnum);
                        log.info("更新TRX交易中余额：{}", merchantInfo.getPendingTrxBalance());
                        break;
                    default:
                        break;
                }
                int i = merchantInfoMapper.updateById(merchantInfo);
                if (i < 1) throw new Exception("更新商户余额失败");
            } else {
                log.info("获取锁失败回滚操作,商户ID->{},账变金额->{},账变类型->{},订单号->{}", merchantCode, changeAmount,
                        changeModeEnum.getName(), orderNo);
                throw new Exception("获取锁失败,回滚操作");
            }
        } catch (Exception e) {
            log.error("商户ID->{},账变金额->{},账变类型->{},订单号->{},异常信息->{}", merchantCode, changeAmount,
                    changeModeEnum.getName(), orderNo, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Boolean.FALSE;
        } finally {
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Boolean.TRUE;
    }

   
    private void operateMerchantBalance(BigDecimal balance, BigDecimal pendingBalance, BigDecimal changeAmount,
                                        Consumer<BigDecimal> setBalance, Consumer<BigDecimal> setPendingBalance,
                                        String merchantCode, AccountChangeEnum accountChangeEnum) {
        if (accountChangeEnum.getCode().equals(AccountChangeEnum.WITHDRAW_BACK.getCode())) {
            //商户下发申请拒绝 交易中金额退到余额
            if (pendingBalance.compareTo(changeAmount) < 0) {
                log.error("商户余额不足, 商户ID->{}", merchantCode);
                throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
            }
            BigDecimal afterPendingBalance = pendingBalance.subtract(changeAmount);
            BigDecimal afterBalance = balance.add(changeAmount);
            setBalance.accept(afterBalance.setScale(6, RoundingMode.HALF_UP));
            setPendingBalance.accept(afterPendingBalance.setScale(6, RoundingMode.HALF_UP));
        } else {
            if (balance.compareTo(changeAmount) < 0) {
                log.error("商户余额不足, 商户ID->{}", merchantCode);
                throw new BizException(ResultCode.MERCHANT_OUTSTANDING_BALANCE);
            }
            BigDecimal afterPendingBalance = pendingBalance.add(changeAmount);
            BigDecimal afterBalance = balance.subtract(changeAmount);
            setBalance.accept(afterBalance.setScale(6, RoundingMode.HALF_UP));
            setPendingBalance.accept(afterPendingBalance.setScale(6, RoundingMode.HALF_UP));
        }

    }

    public Boolean insertChangeAmountRecord(
            String merchantCode,
            BigDecimal changeAmount,
            ChangeModeEnum changeModeEnum,
            String currentcy,
            String orderNo,
            AccountChangeEnum accountChangeEnum,
            String orderCreateTime,
            String remark,
            String merchantOrderNo,
            String paymentChannel,
            String balanceType
    ) {
        return insertChangeAmountRecord(merchantCode, changeAmount, changeModeEnum, currentcy, orderNo, accountChangeEnum, orderCreateTime, remark, merchantOrderNo, paymentChannel, null, balanceType);
    }


    public Boolean insertMemberChangeAmountRecord(String mid, BigDecimal changeAmount, ChangeModeEnum changeModeEnum, String currentcy, String orderNo, MemberAccountChangeEnum memberAccountChangeEnum, String createBy, String payType) {
        return insertMemberChangeAmountRecord(mid, changeAmount, changeModeEnum, currentcy, orderNo, memberAccountChangeEnum, createBy, "", payType, null, null);
    }

    public Boolean insertMemberChangeAmountRecord(String mid, BigDecimal changeAmount, ChangeModeEnum changeModeEnum, String currentcy, String orderNo, MemberAccountChangeEnum memberAccountChangeEnum, String createBy, String merchantOrder, String remark, String merchantMemberId) {
        return insertMemberChangeAmountRecord(mid, changeAmount, changeModeEnum, currentcy, orderNo, memberAccountChangeEnum, createBy, remark, PayTypeEnum.INDIAN_UPI.getCode(), merchantOrder, merchantMemberId);
    }


    /**
     * 更新会员余额并记录账变
     *
     * @param mid            商户ID
     * @param changeAmount   账变金额
     * @param changeModeEnum 账变类型
     * @param currentcy      币种
     * @param orderNo        订单号
     * @param orderNo        订单号
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean insertMemberChangeAmountRecord(String mid, BigDecimal changeAmount, ChangeModeEnum changeModeEnum, String currentcy, String orderNo, MemberAccountChangeEnum memberAccountChangeEnum, String createBy, String remark, String payType, String merchantOrder, String merchantMemberId) {
        log.info("开始记录会员余额账变,会员ID->{},账变金额->{},账变类型->{},订单号->{}", mid, changeAmount,
                changeModeEnum.getName(), orderNo);
        String key = "ar-wallet-sell" + mid;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {

                MemberInfo memberInfo = memberInfoMapper.getMemberInfoById(mid);
                if (ObjectUtils.isEmpty(memberInfo)) {
                    log.error("会员不存在,会员ID->{}", mid);
                    throw new BizException(ResultCode.MEMBER_NOT_EXIST);
                }


                MemberAccountChange accountChange = new MemberAccountChange();
                BigDecimal balance = memberInfo.getBalance();
                BigDecimal frozenAmount = memberInfo.getBiFrozenAmount();
                accountChange.setBeforeChange(balance);
                accountChange.setAmountChange(changeAmount);
                BigDecimal afterAmount = BigDecimal.ZERO;
                BigDecimal afterFrozenAmount = BigDecimal.ZERO;
                // 后台冻结金额修改标识
                String frozenAmountFlag = null;
                if (changeModeEnum.getCode().equals(ChangeModeEnum.ADD.getCode())) {
                    // 回退
                    if (memberAccountChangeEnum.getCode().equals(MemberAccountChangeEnum.UNFREEZE.getCode())) {
                        afterFrozenAmount = frozenAmount.subtract(changeAmount);
                        frozenAmountFlag = "1";
                    }
                    afterAmount = balance.add(changeAmount);
                } else {
                    if (balance.compareTo(changeAmount) == -1) {
                        log.error("会员余额不足,会员ID->{}", mid);
                        throw new BizException(ResultCode.MEMBER_OUTSTANDING_BALANCE);
                    }
                    if (memberAccountChangeEnum.getCode().equals(MemberAccountChangeEnum.FREEZE.getCode())) {
                        afterFrozenAmount = frozenAmount.add(changeAmount);
                        frozenAmountFlag = "1";
                    }
                    afterAmount = balance.subtract(changeAmount);
                    log.info("会员ID->{},账变金额->{},账变类型->{},订单号->{},账变前金额->{},账变后金额->{}", mid, changeAmount,
                            changeModeEnum.getName(), orderNo, accountChange.getBeforeChange(), afterAmount);
                }


                accountChange.setMid(mid);
                accountChange.setAfterChange(afterAmount);
                accountChange.setCurrentcy(currentcy);
                accountChange.setChangeMode(changeModeEnum.getCode());
                accountChange.setOrderNo(orderNo);
                accountChange.setChangeType(memberAccountChangeEnum.getCode());
                accountChange.setCreateTime(LocalDateTime.now(ZoneId.systemDefault()));
                accountChange.setCreateBy(createBy);
                accountChange.setUpdateBy(createBy);
                accountChange.setRemark(remark);
                accountChange.setMerchantOrder(merchantOrder);
                accountChange.setPayType(payType);
                if (ObjectUtils.isNotEmpty(merchantMemberId)) {
                    accountChange.setMemberId(merchantMemberId);
                }

                // 设置商户名称
                if (!ObjectUtils.isEmpty(memberInfo.getMerchantName())) {
                    accountChange.setMerchantName(memberInfo.getMerchantName());
                }

                // 设置会员账号
                if (!ObjectUtils.isEmpty(memberInfo.getMemberAccount())) {
                    accountChange.setMemberAccount(memberInfo.getMemberAccount());
                }

                int i = memberInfoMapper.updateBalanceById(accountChange.getAfterChange(), mid, frozenAmountFlag, afterFrozenAmount);
                if (i < 1) throw new Exception("更新会员余额失败");
                int j = memberAccountChangeMapper.insert(accountChange);
                if (j < 1) throw new Exception("新增会员账变");
                // 更新余额后检查是否有委托订单 如果有调用重新委托方法
                DelegationOrder delegationOrder = delegationOrderMapper.selectByMemberIdForUpdate(mid);
                if (ObjectUtils.isNotEmpty(delegationOrder)) {
                    // 调用重新重新委托接口
                    delegationOrderUtil.redelegate(Long.parseLong(mid), afterAmount);
                }
            } else {
                log.info("获取锁失败回滚操作,会员ID->{},账变金额->{},账变类型->{},订单号->{}", mid, changeAmount,
                        changeModeEnum.getName(), orderNo);
                throw new Exception("获取锁失败回滚操作");
            }
        } catch (Exception e) {
            log.error("会员ID->{},账变金额->{},账变类型->{},订单号->{},异常信息->{}", mid, changeAmount,
                    changeModeEnum.getName(), orderNo, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Boolean.FALSE;
        } finally {
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Boolean.TRUE;
    }


    /**
     * 获取当前会员信息
     *
     * @return {@link MemberInfo}
     */
    public MemberInfo getMemberInfo(Long buyMemberId) {

        LambdaQueryWrapper<MemberInfo> sectionQueryWrapper = new LambdaQueryWrapper<MemberInfo>();
        sectionQueryWrapper.eq(MemberInfo::getId, buyMemberId).eq(MemberInfo::getDeleted, 0);

        if (buyMemberId != null) {
            return memberInfoMapper.selectOne(sectionQueryWrapper);
        }

        log.error("获取当前会员信息失败: 会员id为null");
        return null;
    }

    /**
     * 账变处理
     *
     * @param mid                     mid
     * @param changeAmount            changeAmount
     * @param changeModeEnum          changeModeEnum
     * @param memberAccountChangeEnum memberAccountChangeEnum
     * @param currency                currency
     * @param orderNo                 orderNo
     * @param merchantOrderNo         merchantOrderNo
     * @param createBy                createBy
     * @return Boolean
     */
    @Transactional
    public BigDecimal kycAutoCompleteAccountChange(String mid,
                                                   BigDecimal changeAmount,
                                                   ChangeModeEnum changeModeEnum,
                                                   MemberAccountChangeEnum memberAccountChangeEnum,
                                                   String currency,
                                                   String orderNo,
                                                   String merchantOrderNo,
                                                   String createBy,
                                                   String remark) {
        log.info("kyc自动完成账变,会员ID->{},账变金额->{},账变类型->{},订单号->{}", mid, changeAmount,
                changeModeEnum.getName(), orderNo);
        String key = "ar-wallet-kycAccountChange" + mid;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;
        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {

                MemberInfo antiMemberInfo = memberInfoMapper.getMemberInfoById(mid);
                MemberInfo updateMemberInfo = new MemberInfo();
                updateMemberInfo.setId(Long.parseLong(mid));
                BigDecimal divide = null;
                if (ObjectUtils.isEmpty(antiMemberInfo)
                ) {
                    log.error("用户不存在, 会员ID->{}", mid);
                    throw new BizException(ResultCode.MEMBER_NOT_EXIST);
                }

                // 用户充值 蚂蚁卖出 代收
                if (memberAccountChangeEnum.getCode().equals(MemberAccountChangeEnum.WITHDRAW.getCode())) {
                    MerchantCollectOrders merchantCollectOrders = merchantCollectOrdersMapper.selectMerchantCollectOrdersForUpdate(merchantOrderNo);
                    String merchantCode = merchantCollectOrders.getMerchantCode();
                    MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoByCode(merchantCode);
                    BigDecimal antiFrozenAmount = antiMemberInfo.getFrozenAmount();
                    // 蚂蚁交易中金额操作
                    if (antiFrozenAmount.compareTo(BigDecimal.ZERO) == 0
                            || antiFrozenAmount.compareTo(changeAmount) < 0) {
                        log.error("蚂蚁余额不足, 会员ID->{}, 交易中余额:{}", mid, antiFrozenAmount);
                        throw new BizException(ResultCode.INSUFFICIENT_BALANCE_2);
                    }
                    // 商户账变
                    String merchantOrderCreateTime = merchantCollectOrders.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    Boolean merchantAccountChange = insertChangeAmountRecord(merchantCode, changeAmount, ChangeModeEnum.ADD, currency, orderNo, AccountChangeEnum.COLLECTION, merchantOrderCreateTime, remark, merchantCollectOrders.getMerchantOrder(), ChannelEnum.UPI.getName(), null, BalanceTypeEnum.FCB.getName());
                    if (!merchantAccountChange) {
                        log.error("充值商户账变处理失败, 会员ID->{}", mid);
                        throw new BizException("商户账变处理失败");
                    }
                    // 商户手续费
                    divide = merchantInfo.getTransferRate().multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                    MerchantRatesConfig merchantRatesConfig = merchantRatesConfigMapper.selectMerchantRateConfigByMerchantId(merchantCode, 1, PayTypeEnum.INDIAN_UPI.getCode());
                    if (ObjectUtils.isNotEmpty(merchantRatesConfig) && ObjectUtils.isNotEmpty(merchantRatesConfig.getFixedFee())) {
                        divide = divide.add(merchantRatesConfig.getFixedFee());
                    }
                    if (divide.compareTo(BigDecimal.ZERO) != 0) {
                        Boolean merchantDivideChange = insertChangeAmountRecord(merchantCode, divide, ChangeModeEnum.SUB, currency, orderNo, AccountChangeEnum.COLLECTION_FEE, merchantOrderCreateTime, remark, merchantCollectOrders.getMerchantOrder(), ChannelEnum.UPI.getName(), null, BalanceTypeEnum.FCB.getName());
                        if (!merchantDivideChange) {
                            log.error("充值商户奖励账变处理失败, 会员ID->{}", mid);
                            throw new BizException("商户奖励账变处理失败");
                        }
                    }
                    // 蚂蚁账变 需要变更交易中金额
                    Boolean memberChange = insertMemberChangeFrozenAmountRecord(mid, changeAmount, ChangeModeEnum.SUB, currency, orderNo, MemberAccountChangeEnum.WITHDRAW, merchantCollectOrders.getMerchantOrder(), createBy, remark, merchantCollectOrders.getExternalMemberId(), PayTypeEnum.INDIAN_UPI.getCode());
                    if (!memberChange) {
                        log.error("充值蚂蚁账变处理失败, 会员ID->{}, 交易中余额:{}", mid, antiFrozenAmount);
                        throw new BizException("蚂蚁账变处理失败");
                    }
                    // 计算奖励
                    BigDecimal sellBonusProportion = antiMemberInfo.getSellBonusProportion();
                    BigDecimal bonus = sellBonusProportion.multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                    if (bonus.compareTo(BigDecimal.ZERO) != 0) {
                        Boolean memberBonusChange = insertMemberChangeAmountRecord(mid, bonus, ChangeModeEnum.ADD, currency, orderNo, MemberAccountChangeEnum.SELL_BONUS, createBy, merchantCollectOrders.getMerchantOrder(), remark, merchantCollectOrders.getExternalMemberId());
                        if (!memberBonusChange) {
                            log.error("充值蚂蚁奖励账变处理失败, 会员ID->{}, 交易中余额:{}", mid, antiFrozenAmount);
                            throw new BizException("蚂蚁奖励账变处理失败");
                        }
                        // 蚂蚁卖出奖励总计
                        updateMemberInfo.setTotalSellBonus(antiMemberInfo.getTotalSellBonus().add(bonus));
                    }
                    updateMemberInfo.setTotalSellSuccessAmount(antiMemberInfo.getTotalSellSuccessAmount().add(changeAmount));
                    updateMemberInfo.setTotalSellSuccessCount(antiMemberInfo.getTotalSellSuccessCount() + 1);
                    updateMemberInfo.setTodaySellSuccessAmount(antiMemberInfo.getTodaySellSuccessAmount().add(changeAmount));
                    updateMemberInfo.setTodaySellSuccessCount(antiMemberInfo.getTodaySellSuccessCount() + 1);
                }
                // 提现 蚂蚁买入 代付
                if (memberAccountChangeEnum.getCode().equals(MemberAccountChangeEnum.RECHARGE.getCode())) {
                    MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(merchantOrderNo);
                    String merchantCode = merchantPaymentOrders.getMerchantCode();
                    MerchantInfo merchantInfo = merchantInfoMapper.getMerchantInfoByCode(merchantCode);

                    // 商户账变
//                    String merchantOrderCreateTime = merchantPaymentOrders.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    Boolean merchantAccountChange = insertOrUpdateAccountChange(merchantCode, changeAmount, ChangeModeEnum.SUB, currency, orderNo, AccountChangeEnum.PAYMENT, remark, merchantPaymentOrders.getMerchantOrder(), ChannelEnum.UPI.getName(), null, BalanceTypeEnum.FCB.getName());
                    if (!merchantAccountChange) {
                        log.error("提现商户账变处理失败, 会员ID->{}", mid);
                        throw new BizException("商户账变处理失败");
                    }
                    // 商户手续费
                    divide = merchantInfo.getPayRate().multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                    MerchantRatesConfig merchantRatesConfig = merchantRatesConfigMapper.selectMerchantRateConfigByMerchantId(merchantCode, 2, PayTypeEnum.INDIAN_UPI.getCode());
                    if (ObjectUtils.isNotEmpty(merchantRatesConfig) && ObjectUtils.isNotEmpty(merchantRatesConfig.getFixedFee())) {
                        divide = divide.add(merchantRatesConfig.getFixedFee());
                    }
                    if (divide.compareTo(BigDecimal.ZERO) != 0) {
                        Boolean merchantDivideChange = insertOrUpdateAccountChange(merchantCode, divide, ChangeModeEnum.SUB, currency, orderNo, AccountChangeEnum.PAYMENT_FEE, remark, merchantPaymentOrders.getMerchantOrder(), ChannelEnum.UPI.getName(), null, BalanceTypeEnum.FCB.getName());
                        if (!merchantDivideChange) {
                            log.error("提现商户奖励账变处理失败, 会员ID->{}", mid);
                            throw new BizException("商户奖励账变处理失败");
                        }
                    }
                    // 蚂蚁账变
                    Boolean memberChange = insertMemberChangeAmountRecord(mid, changeAmount, ChangeModeEnum.ADD, currency, orderNo, MemberAccountChangeEnum.RECHARGE, createBy, merchantPaymentOrders.getMerchantOrder(), remark, merchantPaymentOrders.getExternalMemberId());
                    if (!memberChange) {
                        log.error("提现蚂蚁账变处理失败, 会员ID->{}", mid);
                        throw new BizException("蚂蚁账变处理失败");
                    }
                    // 蚂蚁买入奖励
                    BigDecimal buyBonusProportion = antiMemberInfo.getBuyBonusProportion();
                    BigDecimal bonus = buyBonusProportion.multiply(changeAmount).divide(new BigDecimal(100), 2, RoundingMode.DOWN);
                    Boolean memberBonusChange = insertMemberChangeAmountRecord(mid, bonus, ChangeModeEnum.ADD, currency, orderNo, MemberAccountChangeEnum.BUY_BONUS, createBy, merchantPaymentOrders.getMerchantOrder(), remark, merchantPaymentOrders.getExternalMemberId());
                    if (bonus.compareTo(BigDecimal.ZERO) != 0) {
                        if (!memberBonusChange) {
                            log.error("提现蚂蚁奖励账变处理失败, 会员ID->{}", mid);
                            throw new BizException("蚂蚁奖励账变处理失败");
                        }
                        // 蚂蚁买入奖励总计
                        updateMemberInfo.setTotalBuyBonus(antiMemberInfo.getTotalBuyBonus().add(bonus));
                    }
                    updateMemberInfo.setTotalBuySuccessAmount(antiMemberInfo.getTotalBuySuccessAmount().add(changeAmount));
                    updateMemberInfo.setTotalBuySuccessCount(antiMemberInfo.getTotalBuySuccessCount() + 1);
                    updateMemberInfo.setTodayBuySuccessAmount(antiMemberInfo.getTodayBuySuccessAmount().add(changeAmount));
                    updateMemberInfo.setTodayBuySuccessCount(antiMemberInfo.getTodayBuySuccessCount() + 1);
                }
                memberInfoMapper.updateById(updateMemberInfo);
                return divide;
            }
        } catch (Exception e) {
            log.error("kyc自动完成账变->{},账变金额->{},账变类型->{},订单号->{},异常信息->{}", mid, changeAmount,
                    changeModeEnum.getName(), orderNo, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return null;
        } finally {
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return null;
    }

    /**
     * 会员交易中金额变更和账变
     *
     * @param mid                     mid
     * @param changeAmount            changeAmount
     * @param changeModeEnum          changeModeEnum
     * @param currency                currency
     * @param orderNo                 orderNo
     * @param memberAccountChangeEnum memberAccountChangeEnum
     * @param merchantOrder           merchantOrder
     * @param createBy                createBy
     * @param remark                  remark
     * @return Boolean
     */
    @Transactional
    public Boolean insertMemberChangeFrozenAmountRecord(String mid, BigDecimal changeAmount, ChangeModeEnum changeModeEnum, String currency, String orderNo, MemberAccountChangeEnum memberAccountChangeEnum, String merchantOrder, String createBy, String remark, String merchantMemberId, String payType) {
        log.info("开始记录会员交易中金额账变,会员ID->{},账变金额->{},账变类型->{},订单号->{}", mid, changeAmount,
                changeModeEnum.getName(), orderNo);
        String key = "ar-wallet-sell" + mid;
        RLock lock = redissonUtil.getLock(key);
        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {

                MemberInfo memberInfo = memberInfoMapper.getMemberInfoById(mid);
                if (ObjectUtils.isEmpty(memberInfo)) {
                    log.error("会员不存在,会员ID->{}", mid);
                    throw new BizException(ResultCode.MEMBER_NOT_EXIST);
                }


                MemberAccountChange accountChange = new MemberAccountChange();
                // 交易中金额
                BigDecimal balance = memberInfo.getBalance();
                BigDecimal frozenAmount = memberInfo.getFrozenAmount();
                BigDecimal totalBalance = balance.add(frozenAmount);
                accountChange.setBeforeChange(totalBalance);
                accountChange.setAmountChange(changeAmount);
                BigDecimal afterFrozenAmount;
                BigDecimal afterTotalBalance;
                if (changeModeEnum.equals(ChangeModeEnum.ADD)) {
                    afterTotalBalance = totalBalance.add(changeAmount);
                    afterFrozenAmount = frozenAmount.add(changeAmount);
                } else {
                    afterTotalBalance = totalBalance.subtract(changeAmount);
                    afterFrozenAmount = frozenAmount.subtract(changeAmount);
                }
                accountChange.setMid(mid);
                accountChange.setAfterChange(afterTotalBalance);
                accountChange.setCurrentcy(currency);
                accountChange.setChangeMode(changeModeEnum.getCode());
                accountChange.setOrderNo(orderNo);
                accountChange.setChangeType(memberAccountChangeEnum.getCode());
                accountChange.setCreateTime(LocalDateTime.now(ZoneId.systemDefault()));
                accountChange.setCreateBy(createBy);
                accountChange.setUpdateBy(createBy);
                accountChange.setRemark(remark);
                accountChange.setMerchantOrder(merchantOrder);
                accountChange.setPayType(payType);
                if (ObjectUtils.isNotEmpty(merchantMemberId)) {
                    accountChange.setMemberId(merchantMemberId);
                }
                // 设置商户名称
                if (!ObjectUtils.isEmpty(memberInfo.getMerchantName())) {
                    accountChange.setMerchantName(memberInfo.getMerchantName());
                }

                // 设置会员账号
                if (!ObjectUtils.isEmpty(memberInfo.getMemberAccount())) {
                    accountChange.setMemberAccount(memberInfo.getMemberAccount());
                }
                memberInfo.setFrozenAmount(afterFrozenAmount);
                int i = memberInfoMapper.updateById(memberInfo);
                if (i < 1) {
                    throw new Exception("更新会员交易中余额失败");
                }
                int j = memberAccountChangeMapper.insert(accountChange);
                if (j < 1) {
                    throw new Exception("新增会员账变");
                }

            } else {
                log.info("获取锁失败回滚操作,会员ID->{},账变金额->{},账变类型->{},订单号->{}", mid, changeAmount,
                        changeModeEnum.getName(), orderNo);
                throw new Exception("获取锁失败回滚操作");
            }
        } catch (Exception e) {
            log.error("会员ID->{},账变金额->{},账变类型->{},订单号->{},异常信息->{}", mid, changeAmount,
                    changeModeEnum.getName(), orderNo, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Boolean.FALSE;
        } finally {
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return Boolean.TRUE;
    }
}
