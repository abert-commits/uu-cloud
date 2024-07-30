package org.uu.wallet.tron.service;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.tron.trident.abi.TypeDecoder;
import org.tron.trident.abi.datatypes.Address;
import org.tron.trident.abi.datatypes.generated.Uint256;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.proto.Response;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.PaymentOrderStatusEnum;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.Enum.WalletTypeEnum;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.*;
import org.uu.wallet.service.impl.TrxPaymentOrderService1;
import org.uu.wallet.service.impl.UsdtPaymentOrderService1;
import org.uu.wallet.tron.bo.BlockBo;
import org.uu.wallet.tron.bo.BlockTransaction;
import org.uu.wallet.tron.bo.TransferRes;
import org.uu.wallet.tron.utils.HttpHelper;
import org.uu.wallet.tron.utils.TronDecimalUtils;
import org.uu.wallet.tron.utils.TronUtil;
import org.uu.wallet.util.MD5Util;
import org.uu.wallet.util.OrderNumberGeneratorUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TronBlockService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private ITronAddressService tronAddressService;

    @Autowired
    private TronService tronService;

    @Autowired
    private IRechargeTronDetailService rechargeTronDetailService;

    @Autowired
    private ITronWalletService tronWalletService;

    @Autowired
    private RedissonUtil redissonUtil;

    @Autowired
    private ITronRentEnergyService tronRentEnergyService;

    @Autowired
    private ITradeConfigService tradeConfigService;

    @Autowired
    private IWithdrawTronDetailService withdrawTronDetailService;

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    @Autowired
    private ArProperty arProperty;

    @Autowired
    private OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private ICollectionOrderRecordService collectionOrderRecordService;

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    @Autowired
    private IMerchantInfoService merchantInfoService;

    @Autowired
    private UsdtPaymentOrderService1 usdtPaymentOrderService1;

    @Autowired
    private TrxPaymentOrderService1 trxPaymentOrderService1;

    // USDT合约地址，以十六进制表示
    private static final String contractAddress_HEX = "41a614f803b6fd780986a42c78ec9c7f77e6ded13c";

    // usdt合约地址
    private static final String contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";

    // USDT的单位转换比例
    private static final BigDecimal USDT_RATE = new BigDecimal(1000000);


    private static final long feeLimit = 10000000;

    private static final BigDecimal USDT_LIMIT = new BigDecimal(200);
    private static final BigDecimal USDT_LIMIT_BIG = new BigDecimal(1000);
    private static final BigDecimal USDT_LIMIT_100 = new BigDecimal(50);

    private static final BigDecimal USDT_LIMIT_50 = new BigDecimal(40);

    //最低TRX余额限制
    private static final BigDecimal FREE_LIMIT = new BigDecimal(40);

    @Autowired
    private RabbitMQService rabbitMQService;

    /**
     * 保存Tron交易详情 (钱包交易记录表)
     *
     * @param blockBos 需要同步的区块列表
     * @return boolean
     */
    public boolean transferHistory(List<BlockBo> blockBos) {
        for (BlockBo hisBo : blockBos) {

            // 如果区块的交易记录是空的则不做处理
            final List<BlockTransaction> transactions = hisBo.getTransactions();
            if (transactions == null || transactions.size() == 0) {
                continue;
            }

            //使用 ApiWrapper 对象与 TRON 网络进行交互
            final ApiWrapper wrapper = tronService.getApiWrapper("");

            //获取智能合约 从 ApiWrapper实例中获取指定合约地址的 Contract 对象
            Contract wrapperContract = wrapper.getContract(contractAddress);

            //transaction 表示区块链中的一个交易对象，包含了交易的详细信息
            for (BlockTransaction transaction : transactions) {

                //contractRet 交易结果的返回码，表示交易执行的状态 只处理成功的交易
                if (transaction.getRet() != null && transaction.getRet().get(0).getContractRet().equals("SUCCESS")) {

                    //合约地址，表示执行交易的智能合约地址。对于 TRC20 代币交易，这是代币合约的地址，用于标识该代币合约
                    final String contract = transaction.getRawData().getContract().get(0).getParameter().getValue().getContractAddress();

                    //交易数据，通常包含交易的附加信息或参数，在 TRC20 代币转账中，data 字段会包含转账方法和参数的编码值
                    final String data = transaction.getRawData().getContract().get(0).getParameter().getValue().getData();

                    //发送方的地址，即发起这笔交易的账户地址。地址以 Base58 格式表示
                    final String fromAddress = transaction.getRawData().getContract().get(0).getParameter().getValue().getOwnerAddress();

                    //交易的类型 例如 TransferContract 表示转账合约
                    final String type = transaction.getRawData().getContract().get(0).getType();

                    //接收方的地址，即接受这笔交易的账户地址。地址以 Base58 格式表示
                    final String to_address = transaction.getRawData().getContract().get(0).getParameter().getValue().getToAddress();

                    //交易金额 1000000 = 1U
                    final BigInteger amountTrx = transaction.getRawData().getContract().get(0).getParameter().getValue().getAmount();

                    // a9059cbb 是 TRC20 转账方法的签名哈希，表示该笔交易是USDT交易
                    if (StringUtils.isNotBlank(data) && (data.startsWith("a9059cbb")) && contract.equals(contractAddress_HEX) && data.length() >= 136) {

                        //处理USDT转账交易

                        //方法签名
                        String signature = "transfer(address,uint256)";

                        // 截取并解码交易数据中的接收地址（从第9个字符到第72个字符）
                        Address rawRecipient = TypeDecoder.decodeAddress(data.substring(8, 72));
                        String toAddress = rawRecipient.toString();

                        // 截取并解码交易数据中的金额（从第73个字符到第136个字符
                        Uint256 rawAmount = TypeDecoder.decodeNumeric(data.substring(72, 136), Uint256.class);
                        BigInteger amount = rawAmount.getValue();

                        TronAddress tronAddress = null;

                        //查看区块中是否包含用户的交易
                        if (redisTemplate.hasKey(RedisKeys.PENDING_USDT_ADDRESS + toAddress)) {
                            //区块中包含用户的交易

                            //查询波场用户地址表中是否存在该接收地址
                            tronAddress = tronAddressService.getTronAddressByAddress(toAddress);
                            log.info("拉取区块数据, 区块数据中包含用户的交易, tronAddress: {}", tronAddress);
                        }

                        if (tronAddress == null) {
                            continue;
                        }

                        //大于等于1U的交易才进行处理
                        if (amount.compareTo(new BigInteger("1000000")) >= 0 && tronAddress != null) {

                            // 如果交易的唯一标识符已经存在 说明该笔订单正在处理或已被处理过了
                            if (redisTemplate.hasKey(RedisKeys.TRON_TX_ID + transaction.getTxID())) {
                                continue;
                            }

                            try {
                                //使用交易的唯一标识符 来做分布式锁 1800秒(30分钟)
                                redisTemplate.opsForValue().set(RedisKeys.TRON_TX_ID + transaction.getTxID(), transaction.getTxID(), 1800);

                                //将TRON 网络中的金额转为USDT的金额
                                final BigDecimal rechargeAmount = TronDecimalUtils.safeDivide3(new BigDecimal(amount), USDT_RATE);

                                //保存交易详情 (钱包交易记录表)
                                RechargeTronDetail rechargeTronDetail = new RechargeTronDetail();

                                //商户ID
                                rechargeTronDetail.setMerchantId(tronAddress.getMerchantId());

                                //会员id
                                rechargeTronDetail.setMemberId(tronAddress.getMemberId());

                                //订单号 还未自动上分的订单号是0
                                rechargeTronDetail.setOrderId("0");

                                //付款地址
                                rechargeTronDetail.setFromAddress(TronUtil.addressHexToBase58(fromAddress));

                                //收款地址
                                rechargeTronDetail.setToAddress(toAddress);

                                //交易id
                                rechargeTronDetail.setTxid(transaction.getTxID());

                                //转账时间, 13位时间戳
                                rechargeTronDetail.setBetTime(hisBo.getBlockHeader().getRawData().getTimestamp());

                                //币种
                                rechargeTronDetail.setSymbol("USDT");

                                //交易金额
                                rechargeTronDetail.setAmount(rechargeAmount);


                                //保存交易详情 (钱包交易记录表)
                                rechargeTronDetailService.save(rechargeTronDetail);

                                //这里确保业务处理成功后 发送MQ来执行自动上分等操作
                                TaskInfo taskInfo = new TaskInfo(toAddress, TaskTypeEnum.USDT_AUTO_CREDIT.getCode(), System.currentTimeMillis());
                                rabbitMQService.sendUsdtAutoCreditMessage(taskInfo);


                                //将信息存储到 Redis 中
                                //将交易地址相关的信息存储到 Redis 中，使用哈希结构存储
                                //键: toAddress  值: tronAddress
                                HashOperations<String, String, Object> opt = redisTemplate.opsForHash();
                                opt.put(RedisKeys.TRON_RECHARGED_ADDRESS, toAddress, tronAddress);

                            } catch (Exception ex) {
                                log.error("钱包地址：{}   充值 {} USDT交易保存失败，{}", toAddress, transaction.getTxID(), ex);
                                //保存失败了 那么将redis分布式锁删除
                                redisTemplate.delete(RedisKeys.TRON_TX_ID + transaction.getTxID());
                            } finally {

                                // 创建 TRC20 合约实例
                                Trc20Contract token = new Trc20Contract(wrapperContract, tronAddress.getAddress(), wrapper);

                                //更新 USDT 余额
                                tronAddress.setUsdtBalance(TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE));

                                //更新 TRX 余额
                                tronAddress.setTrxBalance(TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE));

                                //根据U地址更新USDT余额和TRX余额
                                tronAddressService.updateBalance(tronAddress);
                            }
                        }
                    } else if (StringUtils.isNotBlank(data) && (data.startsWith("23b872dd")) && contract.equals(contractAddress_HEX) && data.length() >= 200) {

                        //处理USDT授权转账交易

                        //方法签名
                        String signature = "transfer(address,uint256)";

                        // 截取并解码交易数据中的接收地址（从第9个字符到第72个字符）
                        Address rawRecipient = TypeDecoder.decodeAddress(data.substring(72, 136));
                        String toAddress = rawRecipient.toString();

                        // 截取并解码交易数据中的金额（从第73个字符到第136个字符
                        Uint256 rawAmount = TypeDecoder.decodeNumeric(data.substring(136, 200), Uint256.class);
                        BigInteger amount = rawAmount.getValue();


                        TronAddress tronAddress = null;

                        //查看区块中是否包含用户的交易
                        if (redisTemplate.hasKey(RedisKeys.PENDING_USDT_ADDRESS + toAddress)) {
                            //区块中包含用户的交易

                            //查询波场用户地址表中是否存在该接收地址
                            tronAddress = tronAddressService.getTronAddressByAddress(toAddress);
                            log.info("拉取区块数据, 区块数据中包含用户的交易, tronAddress: {}", tronAddress);
                        }

                        if (tronAddress == null) {
                            continue;
                        }

                        //大于等于1U的充值才进行处理
                        if (amount.compareTo(new BigInteger("1000000")) >= 0 && tronAddress != null) {

                            // 如果交易的唯一标识符已经存在 说明该笔订单正在处理或已被处理过了
                            if (redisTemplate.hasKey(RedisKeys.TRON_TX_ID + transaction.getTxID())) {
                                continue;
                            }

                            try {
                                //使用交易的唯一标识符 来做分布式锁 1800秒(30分钟)
                                redisTemplate.opsForValue().set(RedisKeys.TRON_TX_ID + transaction.getTxID(), transaction.getTxID(), 1800);

                                //将TRON 网络中的金额转为USDT的金额
                                final BigDecimal rechargeAmount = TronDecimalUtils.safeDivide3(new BigDecimal(amount), USDT_RATE);

                                //保存交易详情 (钱包交易记录表)
                                RechargeTronDetail rechargeTronDetail = new RechargeTronDetail();

                                //商户ID
                                rechargeTronDetail.setMerchantId(tronAddress.getMerchantId());

                                //会员id
                                rechargeTronDetail.setMemberId(tronAddress.getMemberId());

                                //订单号 还未自动上分的订单号是0
                                rechargeTronDetail.setOrderId("0");

                                //付款地址
                                rechargeTronDetail.setFromAddress(TronUtil.addressHexToBase58(fromAddress));

                                //收款地址
                                rechargeTronDetail.setToAddress(toAddress);

                                //交易id
                                rechargeTronDetail.setTxid(transaction.getTxID());

                                //转账时间, 13位时间戳
                                rechargeTronDetail.setBetTime(hisBo.getBlockHeader().getRawData().getTimestamp());

                                //币种
                                rechargeTronDetail.setSymbol("USDT");

                                //交易金额
                                rechargeTronDetail.setAmount(rechargeAmount);

                                //保存交易详情 (钱包交易记录表)
                                rechargeTronDetailService.save(rechargeTronDetail);

                                //这里确保业务处理成功后 发送MQ来执行自动上分等操作
                                TaskInfo taskInfo = new TaskInfo(toAddress, TaskTypeEnum.USDT_AUTO_CREDIT.getCode(), System.currentTimeMillis());
                                rabbitMQService.sendUsdtAutoCreditMessage(taskInfo);

                                //将信息存储到 Redis 中
                                //将交易地址相关的信息存储到 Redis 中，使用哈希结构存储
                                //键: toAddress  值: tronAddress
                                HashOperations<String, String, Object> opt = redisTemplate.opsForHash();
                                opt.put(RedisKeys.TRON_RECHARGED_ADDRESS, toAddress, tronAddress);

                            } catch (Exception ex) {
                                log.error("钱包地址：{}   充值 {} USDT授权交易保存失败，{}", toAddress, transaction.getTxID(), ex);
                                redisTemplate.delete(RedisKeys.TRON_TX_ID + transaction.getTxID());
                            } finally {

                                // 创建 TRC20 合约实例
                                Trc20Contract token = new Trc20Contract(wrapperContract, tronAddress.getAddress(), wrapper);

                                //更新 USDT 余额
                                tronAddress.setUsdtBalance(TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE));

                                //更新 TRX 余额
                                tronAddress.setTrxBalance(TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE));

                                //根据U地址更新USDT余额和TRX余额
                                tronAddressService.updateBalance(tronAddress);
                            }
                        }
                    } else if (StringUtils.isNotBlank(to_address) && StringUtils.isNotBlank(type) && type.equals("TransferContract") && amountTrx != null && amountTrx.compareTo(new BigInteger("50000000")) >= 0) {

                        //处理TRX交易 只处理金额大于50TRX的交易

                        //十六进制地址转为 Base58 地址
                        String toAddress = TronUtil.addressHexToBase58(to_address);


                        TronAddress tronAddress = null;

                        //查看区块中是否包含用户的交易
                        if (redisTemplate.hasKey(RedisKeys.PENDING_USDT_ADDRESS + toAddress)) {
                            //区块中包含用户的交易

                            //查询波场用户地址表中是否存在该接收地址
                            tronAddress = tronAddressService.getTronAddressByAddress(toAddress);
                            log.info("拉取区块数据, 区块数据中包含用户的交易, tronAddress: {}", tronAddress);
                        }

                        if (tronAddress == null) {
                            continue;
                        }

                        // 如果交易的唯一标识符已经存在 说明该笔订单正在处理或已被处理过了
                        if (redisTemplate.hasKey(RedisKeys.TRON_TX_ID + transaction.getTxID())) {
                            continue;
                        }

                        try {

                            //使用交易的唯一标识符 来做分布式锁 1800秒(30分钟)
                            redisTemplate.opsForValue().set(RedisKeys.TRON_TX_ID + transaction.getTxID(), transaction.getTxID(), 1800);

                            //将TRON 网络中的金额转为USDT的金额
                            final BigDecimal rechargeAmount = TronDecimalUtils.safeDivide3(new BigDecimal(amountTrx), USDT_RATE);

                            //保存交易详情 (钱包交易记录表)
                            RechargeTronDetail rechargeTronDetail = new RechargeTronDetail();

                            //商户ID
                            rechargeTronDetail.setMerchantId(tronAddress.getMerchantId());

                            //会员id
                            rechargeTronDetail.setMemberId(tronAddress.getMemberId());

                            //订单号 还未自动上分的订单号是0
                            rechargeTronDetail.setOrderId("0");

                            //付款地址
                            rechargeTronDetail.setFromAddress(TronUtil.addressHexToBase58(fromAddress));

                            //收款地址
                            rechargeTronDetail.setToAddress(toAddress);

                            //交易id
                            rechargeTronDetail.setTxid(transaction.getTxID());

                            //转账时间, 13位时间戳
                            rechargeTronDetail.setBetTime(hisBo.getBlockHeader().getRawData().getTimestamp());

                            //币种
                            rechargeTronDetail.setSymbol("TRX");

                            //交易金额
                            rechargeTronDetail.setAmount(rechargeAmount);

                            rechargeTronDetailService.save(rechargeTronDetail);

                            //这里确保业务处理成功后 发送MQ来执行自动上分等操作
                            TaskInfo taskInfo = new TaskInfo(toAddress, TaskTypeEnum.TRX_AUTO_CREDIT.getCode(), System.currentTimeMillis());
                            rabbitMQService.sendTrxAutoCreditMessage(taskInfo);

                            //将信息存储到 Redis 中
                            //将交易地址相关的信息存储到 Redis 中，使用哈希结构存储
                            //键: toAddress  值: tronAddress
                            HashOperations<String, String, Object> opt = redisTemplate.opsForHash();
                            opt.put(RedisKeys.TRON_RECHARGED_ADDRESS, toAddress, tronAddress);
                        } catch (Exception ex) {

                            log.error("钱包地址：{}   充值 {} TRX保存失败，", toAddress, transaction.getTxID());
                            redisTemplate.delete(RedisKeys.TRON_TX_ID + transaction.getTxID());

                        } finally {

                            // 创建 TRC20 合约实例
                            Trc20Contract token = new Trc20Contract(wrapperContract, tronAddress.getAddress(), wrapper);

                            //更新 USDT 余额
                            tronAddress.setUsdtBalance(TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE));

                            //更新 TRX 余额
                            tronAddress.setTrxBalance(TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE));

                            //根据U地址更新USDT余额和TRX余额
                            tronAddressService.updateBalance(tronAddress);
                        }
                    }
                }
            }
        }
        return true;
    }


    /**
     * 资金归集
     */
    public boolean loadBalance() {

        //获取API包装器实例
        final ApiWrapper wrapper = tronService.getApiWrapper("");

        //获取波场钱包地址列表
        List<TronWallet> addressList = tronWalletService.lambdaQuery().list();

        if (addressList == null || addressList.size() == 0) {
            // 如果地址列表为空，则返回false
            log.error("资金归集失败 获取波场钱包地址列表为空");
            return false;
        }

        // 优先更新所有波场钱包余额
        for (TronWallet tronAddress : addressList) {

            try {

                // 获取TRX余额并更新钱包对象
                final BigDecimal trxBalance = TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE);
                tronAddress.setTrxBalance(trxBalance);

                // 获取合约实例
                Contract contract = wrapper.getContract(contractAddress);

                try {

                    // 获取USDT余额并更新钱包对象
                    Trc20Contract token = new Trc20Contract(contract, tronAddress.getAddress(), wrapper);

                    final BigDecimal usdtBalance = TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE);

                    tronAddress.setUsdtBalance(usdtBalance);
                } catch (Exception ex) {

                    // 如果获取USDT余额失败，设置余额为0
                    tronAddress.setUsdtBalance(BigDecimal.ZERO);
                }

                // 如果是提现钱包且USDT余额低于5000且Redis中没有记录，则发送余额不足警告
                if (tronAddress.getWalletType() != null && tronAddress.getWalletType() == WalletTypeEnum.WITHDRAW.getCode()
                        && tronAddress.getUsdtBalance().compareTo(new BigDecimal(5000)) < 0
                        && !redisTemplate.hasKey(RedisKeys.TRON_WALLET + tronAddress.getAddress())) {

                    // 设置Redis缓存
                    redisTemplate.opsForValue().set(RedisKeys.TRON_WALLET + tronAddress.getAddress(), tronAddress.getAddress(), 300, TimeUnit.SECONDS);
                }

                // 更新钱包余额
                tronWalletService.updateBalance(tronAddress);
            } catch (Exception ex) {
                // 记录更新余额失败的日志
                log.error("资金归集失败, update Balance fail ：{}", ex);
            }
        }

        //从 Redis 中获取指定哈希键的所有条目 (用户钱包充值地址 用户收到钱后都会存入到redis)
        final Map<String, Object> map = redisTemplate.opsForHash().entries(RedisKeys.TRON_RECHARGED_ADDRESS);

        if (map != null && map.size() > 0) {
            /**
             * 遍历账户信息
             */
            for (Object v : map.values()) {


                TronAddress tronAddress2 = (TronAddress) v;

                //获取用户钱包地址信息
                final TronAddress tronAddress = tronAddressService.getTronAddressByAddress(tronAddress2.getAddress());

                // 获取TRX余额
                final BigDecimal trxBalance = TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE);

                // 获取合约实例
                Contract contract = wrapper.getContract(contractAddress);
                Trc20Contract token = new Trc20Contract(contract, tronAddress.getAddress(), wrapper);

                //从合约获取USDT余额
                final BigDecimal usdtBalance = TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE);

                try {

                    // 如果余额有变化则更新
                    if (usdtBalance.compareTo(tronAddress.getUsdtBalance()) != 0 || trxBalance.compareTo(tronAddress.getTrxBalance()) != 0) {
                        tronAddress.setUsdtBalance(usdtBalance);
                        tronAddress.setTrxBalance(trxBalance);
                        tronAddressService.updateBalance(tronAddress);
                    }

                    //获取配置信息
                    TradeConfig tradeConfig = tradeConfigService.getById(1);

                    //如果USDT余额小于最低归集金额, 或者TRX余额低于6 那么不做处理
                    if (usdtBalance.compareTo(tradeConfig.getMinUsdtCollectionAmount()) < 0 && trxBalance.compareTo(new BigDecimal("6")) < 0) {
                        log.info("资金归集处理: 用户余额低于最低归集金额, 不做处理, usdt信息: {}, 最低归集金额: {}, usdt余额: {}, trx余额: {}", tronAddress, tradeConfig.getMinUsdtCollectionAmount(), usdtBalance, trxBalance);
                        continue;
                    }

                    //获取并检查中转账户
                    TronWallet transferWallet = tronWalletService.queryAddressByType(WalletTypeEnum.TRANSFER.getCode());

                    if (transferWallet == null) {
                        log.error("资金归集处理失败, 获取中转账号失败, 请检查");
                        break;
                    }

                    final BigDecimal transferTrx = TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(transferWallet.getAddress())), USDT_RATE);

                    //如果中转账户TRX余额小于40 那么不进行处理
                    if (transferTrx.compareTo(new BigDecimal(40)) < 0) {
                        log.error("资金归集处理失败, 中转账号TRX余额不足, 中转账户: {}", transferWallet);
                        break;
                    }

                    // 从 wrapper 对象中获取指定 Tron 地址的资源信息
                    Response.AccountResourceMessage tronAddressResource = wrapper.getAccountResource(tronAddress.getAddress());

                    // 计算该地址剩余的能量（Energy）
                    // 剩余能量 = 能量限制 - 已使用能量
                    final long tronAddressEnergy = tronAddressResource.getEnergyLimit() - tronAddressResource.getEnergyUsed();

                    // 计算该地址剩余的免费网络带宽（Free Net）
                    // 剩余免费网络带宽 = 免费网络带宽限制 - 已使用免费网络带宽
                    final long freeNet = (tronAddressResource.getFreeNetLimit() - tronAddressResource.getFreeNetUsed());

                    log.info("资金归集, 地址：{}, 带宽：{}, 能量: {}, TRX: {}, USDT: {}", tronAddress.getAddress(), freeNet, tronAddressEnergy, trxBalance, usdtBalance);

                    //当USDT余额大于0 判断账户是否激活，未激活需要转入0.5TRX进行激活
                    if (usdtBalance.compareTo(BigDecimal.ZERO) > 0 && tronAddressResource.getFreeNetLimit() == 0) {
                        tronService.transferTrx(transferWallet.getAddress(), transferWallet.getPrivateKey(), tronAddress.getAddress(), new BigDecimal("0.5"));
                        // 等待1秒
                        Thread.sleep(1000);
                        continue;
                    }

                    // 归集逻辑：根据不同条件判断是否需要租赁能量、转账等操作
                    //用户USDT余额大于40，能量小于转账能量，带宽小于转账带宽，TRX余额小于转账余额，使用中转账户给用户钱包转5TRX
                    if (usdtBalance.compareTo(USDT_LIMIT_50) > 0 && tronAddressEnergy < 31900 && freeNet < 345 && trxBalance.compareTo(new BigDecimal(0.4)) < 0) {

                        //使用中转账户给用户地址转5TRX
                        tronService.transferTrx(transferWallet.getAddress(), transferWallet.getPrivateKey(), tronAddress.getAddress(), new BigDecimal("5"));
                        //授权标记
                        tronAddress.setApproveFlag(2);

                        try {
                            //暂停一秒
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                        }
                        //USDT余额大于40 并且能量小于转账能量 带宽小于转账带宽 带宽大于转账带宽 或者 trx余额 >= 0.4
                        //或者 USDT余额小于等于40 并且USDT余额大于1 并且能量大于转账能量 并且带宽大于转账带宽
                    } else if ((usdtBalance.compareTo(USDT_LIMIT_50) > 0 && tronAddressEnergy < 31900 && (freeNet >= 345 || trxBalance.compareTo(new BigDecimal(0.4)) >= 0))
                            || (usdtBalance.compareTo(USDT_LIMIT_50) <= 0 && usdtBalance.compareTo(BigDecimal.ONE) >= 0 && tronAddressEnergy < 31900 && freeNet >= 345)) {

                        //查询资金账户
                        TronWallet tronWallet = tronWalletService.queryAddressByType(WalletTypeEnum.FUND.getCode());

                        if (tronWallet == null) {
                            log.error("资金归集处理失败, 获取资金账号失败, 请检查");
                            continue;
                        }

                        //USDT余额大于40，能量小于转账能量，带宽大于转账带宽 或者 TRX余额大于转账余额，租能量

                        //分布式锁key ar-wallet-rentEnergy2 + 钱包用户地址
                        String key = "uu-wallet-rentEnergy2" + tronAddress.getAddress();
                        RLock lock = redissonUtil.getLock(key);

                        boolean req = false;

                        try {
                            req = lock.tryLock(10, TimeUnit.SECONDS);

                            if (req) {
                                //调用能量租用方法
                                rentEnergy2GJ(tronAddress.getAddress(), 32000l, 0);
                            } else {
                            }
                        } catch (Exception e) {
                            log.error("资金归集处理失败, 租赁能量失败, 用户钱包地址: {}  e: {}", tronAddress.getAddress(), e);
                        } finally {
                            //释放锁
                            if (req && lock.isHeldByCurrentThread()) {
                                lock.unlock();
                            }
                        }

                    } else if (usdtBalance.compareTo(USDT_LIMIT_50) > 0 && tronAddressEnergy >= 31900 && (freeNet >= 345 || trxBalance.compareTo(new BigDecimal(0.4)) >= 0)) {

                        //USDT余额大于40，能量大于转账能量，带宽大于转账带宽 或者 TRX余额大于转账余额，归集

                        //获取被归集的资金账户
                        TronWallet tronWallet = tronWalletService.queryAddressByType(WalletTypeEnum.FUND.getCode());

                        if (tronWallet == null) {
                            log.error("资金归集处理失败, 获取资金账号失败, 请检查");
                            continue;
                        }

                        //归集处理
                        String txId = tronService.transfer(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), usdtBalance);

                        //新增归集记录
                        CollectionOrderRecord collectionOrderRecord = CollectionOrderRecord.builder()
                                .collectionOrderId(orderNumberGenerator.generateOrderNo("GJ"))
                                .collectionType(1)//自动归集
                                .collectionAmount(usdtBalance)
                                .fromAddress(tronAddress.getAddress())
                                .toAddress(tronWallet.getAddress())
                                .collectionBalanceType("USDT")
                                .status(2)//先默认成功 后面有问题在调整
                                .txid(txId)
                                .build();

                        boolean save = collectionOrderRecordService.save(collectionOrderRecord);
                        log.info("归集处理: 归集地址: {}, txId: {}, 保存归集记录sql结果: {}", tronAddress.getAddress(), txId, save);
                    } else if (usdtBalance.compareTo(USDT_LIMIT_50) <= 0 && tronAddressEnergy >= 31900 && usdtBalance.compareTo(BigDecimal.ONE) >= 0 && freeNet >= 345) {

                        //USDT小于40 并且 能量大于转账能量 并且USDT余额大于等于1 并且免费带宽大于转账带宽

                        //获取被归集的资金账户
                        TronWallet tronWallet = tronWalletService.queryAddressByType(WalletTypeEnum.FUND.getCode());

                        if (tronWallet == null) {
                            log.error("资金归集处理失败, 获取资金账号失败, 请检查");
                            continue;
                        }

                        //归集处理
                        String txId = tronService.transfer(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), usdtBalance);

                        //新增归集记录
                        CollectionOrderRecord collectionOrderRecord = CollectionOrderRecord.builder()
                                .collectionOrderId(orderNumberGenerator.generateOrderNo("GJ"))
                                .collectionType(1)//自动归集
                                .collectionAmount(usdtBalance)
                                .fromAddress(tronAddress.getAddress())
                                .toAddress(tronWallet.getAddress())
                                .collectionBalanceType("USDT")
                                .status(2)
                                .txid(txId)
                                .build();

                        boolean save = collectionOrderRecordService.save(collectionOrderRecord);
                        log.info("归集处理: 归集地址: {}, txId: {}, 保存归集记录sql结果: {}", tronAddress.getAddress(), txId, save);
                    } else if (trxBalance.compareTo(new BigDecimal("6")) >= 0) {

                        //trx余额大于6 归集TRX余额

                        //获取被归集的资金账户
                        TronWallet tronWallet = tronWalletService.queryAddressByType(WalletTypeEnum.FUND.getCode());

                        if (tronWallet == null) {
                            log.error("资金归集处理失败, TRX归集失败, 获取资金账号失败, 请检查");
                            continue;
                        }
                        String txId = null;

                        BigDecimal trxAmount = trxBalance;

                        if (freeNet > 300) {
                            //免费带宽大于300 将TRX余额进行归集 直接将所有的 TRX 余额转移到资金账户
                            txId = tronService.transferTrx(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), trxBalance);
                        } else {
                            trxAmount = TronDecimalUtils.safeSubtract(trxBalance, new BigDecimal(0.3));
                            //免费带宽小于300 则将 TRX 余额减去 0.3 后再转移到资金账户。这是为了保留一些 TRX 余额，以避免 TRX 余额变为 0 的情况，这可能会影响账户的其他操作
                            txId = tronService.transferTrx(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), TronDecimalUtils.safeSubtract(trxBalance, new BigDecimal(0.3)));
                        }

                        //新增归集记录
                        CollectionOrderRecord collectionOrderRecord = CollectionOrderRecord.builder()
                                .collectionOrderId(orderNumberGenerator.generateOrderNo("GJ"))
                                .collectionType(1)//自动归集
                                .collectionAmount(trxAmount)
                                .fromAddress(tronAddress.getAddress())
                                .toAddress(tronWallet.getAddress())
                                .collectionBalanceType("TRX")
                                .status(2)
                                .txid(txId)
                                .build();

                        boolean save = collectionOrderRecordService.save(collectionOrderRecord);
                        log.info("归集处理: TRX归集, 归集地址: {}, txId: {}, 保存归集记录sql结果: {}", tronAddress.getAddress(), txId, save);
                    }
                } catch (Exception ex) {
                    log.error("归集失败...{}", ex);
                } finally {

                    // 更新最新的TRX和USDT余额并更新数据库
                    tronAddress.setUsdtBalance(TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE));
                    tronAddress.setTrxBalance(TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE));

                    // 如果余额都小于等于1，则从Redis中删除该地址的记录 (收到钱时 会添加这个hash数据 key是用户的地址)
                    if (tronAddress.getTrxBalance().compareTo(BigDecimal.ONE) <= 0 && tronAddress.getUsdtBalance().compareTo(BigDecimal.ONE) <= 0) {
                        redisTemplate.opsForHash().delete(RedisKeys.TRON_RECHARGED_ADDRESS, tronAddress.getAddress());
                    }

                    // 更新数据库中的余额
                    tronAddressService.updateBalance(tronAddress);
                }
            }
        }
        // 返回true表示归集操作成功
        return true;
    }


    /**
     * 指定账户资金归集
     */
    public boolean collectFundsForAccounts(List<String> usdtAddresses) {

        if (usdtAddresses == null || usdtAddresses.isEmpty()) {
            log.error("指定账户资金归集失败, 归集账户不能为空");
            return false;
        }

        //获取API包装器实例
        final ApiWrapper wrapper = tronService.getApiWrapper("");

        //获取波场钱包地址列表
        List<TronWallet> addressList = tronWalletService.lambdaQuery().list();

        if (addressList == null || addressList.size() == 0) {
            // 如果地址列表为空，则返回false
            log.error("指定账户资金归集失败 获取波场钱包地址列表为空");
            return false;
        }

        // 优先更新所有波场钱包余额
        for (TronWallet tronAddress : addressList) {

            try {
                // 获取TRX余额并更新钱包对象
                final BigDecimal trxBalance = TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE);
                tronAddress.setTrxBalance(trxBalance);

                // 获取合约实例
                Contract contract = wrapper.getContract(contractAddress);

                try {

                    // 获取USDT余额并更新钱包对象
                    Trc20Contract token = new Trc20Contract(contract, tronAddress.getAddress(), wrapper);

                    final BigDecimal usdtBalance = TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE);

                    tronAddress.setUsdtBalance(usdtBalance);
                } catch (Exception ex) {

                    // 如果获取USDT余额失败，设置余额为0
                    tronAddress.setUsdtBalance(BigDecimal.ZERO);
                }

                // 如果是提现钱包且USDT余额低于5000且Redis中没有记录，则发送余额不足警告
                if (tronAddress.getWalletType() != null && tronAddress.getWalletType() == WalletTypeEnum.WITHDRAW.getCode()
                        && tronAddress.getUsdtBalance().compareTo(new BigDecimal(5000)) < 0
                        && !redisTemplate.hasKey(RedisKeys.TRON_WALLET + tronAddress.getAddress())) {

                    // 设置Redis缓存
                    redisTemplate.opsForValue().set(RedisKeys.TRON_WALLET + tronAddress.getAddress(), tronAddress.getAddress(), 300, TimeUnit.SECONDS);
                }

                // 更新钱包余额
                tronWalletService.updateBalance(tronAddress);
            } catch (Exception ex) {
                // 记录更新余额失败的日志
                log.error("指定账户资金归集失败, update Balance fail ：{}", ex);
            }
        }
        boolean done = false;

        //遍历账户信息
        for (String usdtAddress : usdtAddresses) {

            //获取用户钱包地址信息
            final TronAddress tronAddress = tronAddressService.getTronAddressByAddress(usdtAddress);

            // 获取TRX余额
            BigDecimal trxBalance = TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE);

            // 获取合约实例
            Contract contract = wrapper.getContract(contractAddress);
            Trc20Contract token = new Trc20Contract(contract, tronAddress.getAddress(), wrapper);

            //从合约获取USDT余额
            final BigDecimal usdtBalance = TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE);

            try {

                // 如果余额有变化则更新
                if (usdtBalance.compareTo(tronAddress.getUsdtBalance()) != 0 || trxBalance.compareTo(tronAddress.getTrxBalance()) != 0) {
                    tronAddress.setUsdtBalance(usdtBalance);
                    tronAddress.setTrxBalance(trxBalance);
                    tronAddressService.updateBalance(tronAddress);
                }

                //检查USDT余额和TRX余额 如果USDT余额小于1并且TRX余额低于6 那么不进行处理
                if (usdtBalance.compareTo(new BigDecimal("1")) < 0 && trxBalance.compareTo(new BigDecimal("6")) < 0) {
                    log.info("资金归集处理: 用户余额低于1 或trx余额低于6, 不做处理, usdt信息: {}, usdt余额: {}, trx余额: {}", tronAddress, usdtBalance, trxBalance);
                    continue;
                }

                //获取并检查中转账户
                TronWallet transferWallet = tronWalletService.queryAddressByType(WalletTypeEnum.TRANSFER.getCode());

                if (transferWallet == null) {
                    log.error("指定账户资金归集处理失败, 获取中转账号失败, 请检查");
                    break;
                }

                final BigDecimal transferTrx = TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(transferWallet.getAddress())), USDT_RATE);

                //如果中转账户TRX余额小于40 那么不进行处理
                if (transferTrx.compareTo(new BigDecimal(40)) < 0) {
                    log.error("指定账户资金归集处理失败, 中转账号TRX余额不足, 中转账户: {}", transferWallet);
                    break;
                }

                // 从 wrapper 对象中获取指定 Tron 地址的资源信息
                Response.AccountResourceMessage tronAddressResource = wrapper.getAccountResource(tronAddress.getAddress());

                // 计算该地址剩余的能量（Energy）
                // 剩余能量 = 能量限制 - 已使用能量
                long tronAddressEnergy = tronAddressResource.getEnergyLimit() - tronAddressResource.getEnergyUsed();

                // 计算该地址剩余的免费网络带宽（Free Net）
                // 剩余免费网络带宽 = 免费网络带宽限制 - 已使用免费网络带宽
                long freeNet = (tronAddressResource.getFreeNetLimit() - tronAddressResource.getFreeNetUsed());

                log.info("指定账户资金归集, 地址：{}, 带宽：{}, 能量: {}, TRX: {}, USDT: {}", tronAddress.getAddress(), freeNet, tronAddressEnergy, trxBalance, usdtBalance);

                //当USDT余额大于0 判断账户是否激活，未激活需要转入0.5TRX进行激活
                if (usdtBalance.compareTo(BigDecimal.ZERO) > 0 && tronAddressResource.getFreeNetLimit() == 0) {

                    log.info("指定账户资金归集, 账户未激活 转入0.5TRX进行激活, u地址: {}", usdtAddress);

                    //转入0.5TRX
                    tronService.transferTrx(transferWallet.getAddress(), transferWallet.getPrivateKey(), tronAddress.getAddress(), new BigDecimal("0.5"));

                    long startTime = System.currentTimeMillis();
                    //最大等待10秒
                    while (System.currentTimeMillis() - startTime < 10000) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex) {

                        }
                        //获取最新账户信息
                        Response.AccountResourceMessage walletResourceThis = wrapper.getAccountResource(tronAddress.getAddress());

                        if (walletResourceThis.getFreeNetLimit() != 0) {
                            log.info("指定账户资金归集, 账户未激活 转入0.5TRX成功, u地址: {}", usdtAddress);
                            freeNet = (walletResourceThis.getFreeNetLimit() - walletResourceThis.getFreeNetUsed());
                            break;
                        }
                    }
                }

                // 归集逻辑：根据不同条件判断是否需要租赁能量、转账等操作
                //用户USDT余额大于40，能量小于转账能量，带宽小于转账带宽，TRX余额小于转账余额，使用中转账户给用户钱包转5TRX
                if (usdtBalance.compareTo(USDT_LIMIT_50) > 0 && tronAddressEnergy < 31900 && freeNet < 345 && trxBalance.compareTo(new BigDecimal(0.4)) < 0) {

                    log.info("指定账户资金归集, 使用中转账户给用户地址转5TRX, u地址: {}", usdtAddress);

                    //使用中转账户给用户地址转5TRX
                    tronService.transferTrx(transferWallet.getAddress(), transferWallet.getPrivateKey(), tronAddress.getAddress(), new BigDecimal("5"));
                    //授权标记
                    tronAddress.setApproveFlag(2);

                    long startTime = System.currentTimeMillis();
                    //最大等待10秒
                    while (System.currentTimeMillis() - startTime < 10000) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                        }
                        //获取最新的TRX余额
                        trxBalance = TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE);

                        //TRX余额大于转账余额
                        if (trxBalance.compareTo(new BigDecimal(0.4)) > 0) {
                            log.info("指定账户资金归集, 使用中转账户给用户地址转5TRX成功, u地址: {}", usdtAddress);
                            break;
                        }
                    }
                }

                boolean rentFlag = false;

                //USDT余额大于40 并且能量小于转账能量 带宽小于转账带宽 带宽大于转账带宽 或者 trx余额 >= 0.4
                //或者 USDT余额小于等于40 并且USDT余额大于1 并且能量大于转账能量 并且带宽大于转账带宽
                if ((usdtBalance.compareTo(USDT_LIMIT_50) > 0 && tronAddressEnergy < 31900 && (freeNet >= 345 || trxBalance.compareTo(new BigDecimal(0.4)) >= 0))
                        || (usdtBalance.compareTo(USDT_LIMIT_50) <= 0 && usdtBalance.compareTo(BigDecimal.ONE) >= 0 && tronAddressEnergy < 31900 && freeNet >= 345)) {

                    //能量不足

                    //USDT余额大于40，能量小于转账能量，带宽大于转账带宽 或者 TRX余额大于转账余额，租能量

                    //分布式锁key ar-wallet-rentEnergy2 + 钱包用户地址
                    String key = "uu-wallet-rentEnergy2" + tronAddress.getAddress();
                    RLock lock = redissonUtil.getLock(key);

                    boolean req = false;

                    try {
                        req = lock.tryLock(10, TimeUnit.SECONDS);

                        if (req) {
                            //调用能量租用方法
                            rentFlag = rentEnergy2GJ(tronAddress.getAddress(), 32000l, 0);
                            log.info("指定账户资金归集, 租能量, u地址: {}", usdtAddress);
                        } else {
                        }
                    } catch (Exception e) {
                        log.error("指定账户资金归集处理失败, 租赁能量失败, 用户钱包地址: {}  e: {}", tronAddress.getAddress(), e);
                    } finally {
                        //释放锁
                        if (req && lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                }

                long startTime = System.currentTimeMillis();
                //租了能量才执行该操作
                if (rentFlag) {
                    //最大等待10秒
                    while (System.currentTimeMillis() - startTime < 10000) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ex) {

                        }
                        //获取最新的能量值
                        Response.AccountResourceMessage walletResourceThis = wrapper.getAccountResource(tronAddress.getAddress());

                        // 剩余能量 = 能量限制 - 已使用能量
                        final long walletEnergyThis = walletResourceThis.getEnergyLimit() - walletResourceThis.getEnergyUsed();

                        if (walletEnergyThis >= 31900) {
                            log.info("指定账户资金归集, 租能量成功, u地址: {}", usdtAddress);
                            //能量足够 继续执行操作
                            tronAddressEnergy = walletEnergyThis;
                            break;
                        }
                    }
                } else {
                    log.error("指定账户资金归集失败, 租能量失败, u地址: {}", usdtAddress);
                }

                //USDT余额大于40，能量大于转账能量，带宽大于转账带宽 或者 TRX余额大于转账余额，归集
                if (usdtBalance.compareTo(USDT_LIMIT_50) > 0 && tronAddressEnergy >= 31900 && (freeNet >= 345 || trxBalance.compareTo(new BigDecimal(0.4)) >= 0)) {

                    log.info("指定账户资金归集, 开始归集, u地址: {}", usdtAddress);

                    //获取被归集的资金账户
                    TronWallet tronWallet = tronWalletService.queryAddressByType(WalletTypeEnum.FUND.getCode());

                    if (tronWallet == null) {
                        log.error("指定账户资金归集处理失败, 获取资金账号失败, 请检查");
                        continue;
                    }

                    //归集处理
                    String txId = tronService.transfer(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), usdtBalance);

                    //新增归集记录
                    CollectionOrderRecord collectionOrderRecord = CollectionOrderRecord.builder()
                            .collectionOrderId(orderNumberGenerator.generateOrderNo("GJ"))
                            .collectionType(2)//手动归集
                            .collectionAmount(usdtBalance)
                            .fromAddress(tronAddress.getAddress())
                            .toAddress(tronWallet.getAddress())
                            .collectionBalanceType("USDT")
                            .status(2)
                            .txid(txId)
                            .build();

                    boolean save = collectionOrderRecordService.save(collectionOrderRecord);
                    log.info("指定账户归集处理: 归集地址: {}, txId: {}, 保存归集记录sql结果: {}", tronAddress.getAddress(), txId, save);

                    done = true;
                } else if (usdtBalance.compareTo(USDT_LIMIT_50) <= 0 && tronAddressEnergy >= 31900 && usdtBalance.compareTo(BigDecimal.ONE) >= 0 && freeNet >= 345) {

                    log.info("指定账户资金归集, 开始归集, u地址: {}", usdtAddress);

                    //USDT小于40 并且 能量大于转账能量 并且USDT余额大于等于1 并且免费带宽大于转账带宽

                    //获取被归集的资金账户
                    TronWallet tronWallet = tronWalletService.queryAddressByType(WalletTypeEnum.FUND.getCode());

                    if (tronWallet == null) {
                        log.error("指定账户资金归集处理失败, 获取资金账号失败, 请检查");
                        continue;
                    }

                    //归集处理
                    String txId = tronService.transfer(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), usdtBalance);

                    //新增归集记录
                    CollectionOrderRecord collectionOrderRecord = CollectionOrderRecord.builder()
                            .collectionOrderId(orderNumberGenerator.generateOrderNo("GJ"))
                            .collectionType(2)//手动归集
                            .collectionAmount(usdtBalance)
                            .fromAddress(tronAddress.getAddress())
                            .toAddress(tronWallet.getAddress())
                            .collectionBalanceType("USDT")
                            .status(2)
                            .txid(txId)
                            .build();

                    boolean save = collectionOrderRecordService.save(collectionOrderRecord);

                    log.info("指定账户归集处理: 归集地址: {}, txId: {}, 保存归集记录sql结果: {}", tronAddress.getAddress(), txId, save);

                    done = true;
                } else if (trxBalance.compareTo(new BigDecimal("6")) >= 0) {

                    log.info("指定账户资金归集, TRX开始归集, u地址: {}, trx余额: {}", usdtAddress, trxBalance);
                    //trx余额大于6 归集TRX余额

                    //获取被归集的资金账户
                    TronWallet tronWallet = tronWalletService.queryAddressByType(WalletTypeEnum.FUND.getCode());

                    if (tronWallet == null) {
                        log.error("指定账户资金归集处理失败, 获取资金账号失败, 请检查");
                        continue;
                    }

                    String txId = null;

                    BigDecimal trxAmount = trxBalance;

                    if (freeNet > 300) {
                        //免费带宽大于300 将TRX余额进行归集 直接将所有的 TRX 余额转移到资金账户
                        txId = tronService.transferTrx(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), trxBalance);
                    } else {
                        trxAmount = TronDecimalUtils.safeSubtract(trxBalance, new BigDecimal(0.3));
                        //免费带宽小于300 则将 TRX 余额减去 0.3 后再转移到资金账户。这是为了保留一些 TRX 余额，以避免 TRX 余额变为 0 的情况，这可能会影响账户的其他操作
                        txId = tronService.transferTrx(tronAddress.getAddress(), tronAddress.getPrivateKey(), tronWallet.getAddress(), TronDecimalUtils.safeSubtract(trxBalance, new BigDecimal(0.3)));
                    }

                    //新增归集记录
                    CollectionOrderRecord collectionOrderRecord = CollectionOrderRecord.builder()
                            .collectionOrderId(orderNumberGenerator.generateOrderNo("GJ"))
                            .collectionType(2)//手动归集
                            .collectionAmount(trxAmount)
                            .fromAddress(tronAddress.getAddress())
                            .toAddress(tronWallet.getAddress())
                            .collectionBalanceType("TRX")
                            .status(2)
                            .txid(txId)
                            .build();

                    boolean save = collectionOrderRecordService.save(collectionOrderRecord);
                    log.info("指定账户归集处理: TRX归集, 归集地址: {}, 归集结果: {}, 保存归集记录sql结果: {}", tronAddress.getAddress(), txId, save);
                    done = true;
                }
            } catch (Exception ex) {
                log.error("指定账户归集失败...{}", ex);
            } finally {

                // 更新最新的TRX和USDT余额并更新数据库
                tronAddress.setUsdtBalance(TronDecimalUtils.safeDivide(new BigDecimal(token.balanceOf(tronAddress.getAddress())), USDT_RATE));
                tronAddress.setTrxBalance(TronDecimalUtils.safeDivide(new BigDecimal(wrapper.getAccountBalance(tronAddress.getAddress())), USDT_RATE));

                // 如果余额都小于等于1，则从Redis中删除该地址的记录 (收到钱时 会添加这个hash数据 key是用户的地址)
                if (tronAddress.getTrxBalance().compareTo(BigDecimal.ONE) <= 0 && tronAddress.getUsdtBalance().compareTo(BigDecimal.ONE) <= 0) {
                    redisTemplate.opsForHash().delete(RedisKeys.TRON_RECHARGED_ADDRESS, tronAddress.getAddress());
                }

                // 更新数据库中的余额
                tronAddressService.updateBalance(tronAddress);
            }
        }
        // 返回true表示归集操作成功
        return done;
    }

    /**
     * 租能量 归集
     *
     * @param address
     * @param amount
     * @param dayNum
     * @return boolean
     */
    private boolean rentEnergy2GJ(String address, Long amount, Integer dayNum) {

        //如果3分钟内已经处理过该地址租赁能量 那就不进行处理了
        if (redisTemplate.hasKey(RedisKeys.TRON_RENT_ENERGY + address)) {
            log.error("归集失败 租赁能量失败, 3分钟内该地址已租赁过能量了, 地址: {}", address);
            return true;
        }

        TronRentEnergy tronRentEnergy = new TronRentEnergy();
        //钱包地址
        tronRentEnergy.setAddress(address);
        //租用数量
        tronRentEnergy.setAmount(amount);
        //租用时长 (0天)
        tronRentEnergy.setRentTime(dayNum.toString());


        try {
            //发送租用能量请求
            final String url = "https://openapi.trx.energy/api/v1/rent";
            Map<String, Object> params = new HashMap<String, Object>() {{
                put("receive", address);
                put("amount", amount);
                put("period", 1);
            }};

            Map<String, String> headMap = new HashMap<String, String>() {{
                put("APIKEY", "Oyj9CGzrrY8a2gCpjlogOeOgPu028qK9w41m1qQTKT4");
            }};

            final String result = HttpHelper.postHeaderMethod(url, headMap, params);
            tronRentEnergy.setRequestInfo(JSON.toJSONString(params));
            tronRentEnergy.setResponseInfo(result);
            tronRentEnergy.setResultCode("0");
            log.info("质押能量结果：：{}", result);
            if (StringUtils.isNotBlank(result) && isJSON2(result)) {
                if (StringUtils.isNotBlank(result) && JSON.parseObject(result).getString("code").equals("1000")) {
                    tronRentEnergy.setResultCode("200");
                    //保存能量租用记录
                    tronRentEnergyService.save(tronRentEnergy);

                    //设置redis锁 3分钟
                    redisTemplate.opsForValue().set(RedisKeys.TRON_RENT_ENERGY + address, tronRentEnergy.getId(), 1800, TimeUnit.SECONDS);
                    log.info("质押能量成功：：{}", result);
                    return true;
                } else {
                    tronRentEnergy.setResultCode(String.valueOf(JSON.parseObject(result).getInteger("code")));
                    tronRentEnergy.setResultMessage(JSON.parseObject(result).getString("message"));
                    //保存能量租用记录
                    tronRentEnergyService.save(tronRentEnergy);
                    log.error("质押能量失败：：{}", result);
                }
            } else {
                //保存能量租用记录
                tronRentEnergyService.save(tronRentEnergy);
            }
        } catch (Exception ex) {
            log.error("质押能量异常：：{}", ex);
            return false;
        }
        return true;
    }


    /**
     * 租能量 出款
     *
     * @param address
     * @param amount
     * @param dayNum
     * @return boolean
     */
    private boolean rentEnergy2DF(String address, Long amount, Integer dayNum) {
        TronRentEnergy tronRentEnergy = TronRentEnergy.builder()
                .address(address)
                .amount(amount)
                .rentTime(dayNum.toString())
                .build();
        try {
            final String url = "https://openapi.trx.energy/api/v1/rent";
            Map<String, Object> params = new HashMap<String, Object>() {{
                put("receive", address);
                put("amount", amount);
                put("period", 1);
            }};

            Map<String, String> headMap = new HashMap<String, String>() {{
                put("APIKEY", "Oyj9CGzrrY8a2gCpjlogOeOgPu028qK9w41m1qQTKT4");
            }};

            final String result = HttpHelper.postHeaderMethod(url, headMap, params);
            tronRentEnergy.setRequestInfo(JSON.toJSONString(params));
            tronRentEnergy.setResponseInfo(result);
            tronRentEnergy.setResultCode("0");
            log.info("质押能量结果：：{}", result);
            if (org.apache.commons.lang3.StringUtils.isNotBlank(result) && isJSON2(result)) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(result) && JSON.parseObject(result).getString("code").equals("1000")) {
                    tronRentEnergy.setResultCode("200");
                    tronRentEnergyService.save(tronRentEnergy);
                    log.info("质押能量成功：：{}", result);
                    return true;
                } else {
                    tronRentEnergy.setResultCode(String.valueOf(JSON.parseObject(result).getInteger("code")));
                    tronRentEnergy.setResultMessage(JSON.parseObject(result).getString("message"));
                    tronRentEnergyService.save(tronRentEnergy);
                    log.error("质押能量失败：：{}", result);
                }
            } else {
                tronRentEnergyService.save(tronRentEnergy);
            }
        } catch (Exception ex) {
            log.error("质押能量异常：：{}", ex);
            return false;
        }
        return true;
    }

    /**
     * USDT出款处理
     *
     * @param tronWallet            出款账户
     * @param merchantPaymentOrders 代付订单
     * @return boolean
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)//开启新事务
    public boolean autoTransfer(TronWallet tronWallet, MerchantPaymentOrders merchantPaymentOrders) {

        //分布式锁key ar-wallet-autoTransfer
        //目前先所有代付订单用同一把锁 串行化  如果出款速度太慢再使用订单号区分
        String key = "uu-wallet-autoTransfer";
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //查询redisSign
                //判断是否存在redis标识 存在才进行处理 (有效期三天)
                Object value = redisTemplate.opsForValue().get("paymentOrderSign:" + merchantPaymentOrders.getPlatformOrder());

                if (value == null) {
                    //不存在订单sign
                    log.error("处理USDT代付订单失败 autoTransfer 不存在订单sign, 订单号: {}, sign: {}", merchantPaymentOrders.getPlatformOrder(), value);
                    return true;
                }

                String paymentOrderSign = String.valueOf(value);

                String md5Sign = MD5Util.generateMD5(merchantPaymentOrders.getMerchantCode() + merchantPaymentOrders.getPlatformOrder() + merchantPaymentOrders.getUsdtAddr() + arProperty.getPaymentOrderKey());

                if (!md5Sign.equals(paymentOrderSign)) {
                    log.error("处理USDT代付订单失败 autoTransfer 代付订单redis签名校验失败, 订单号: {}, paymentOrderSign: {}, md5Sign: {}", merchantPaymentOrders.getPlatformOrder(), paymentOrderSign, md5Sign);
                    return true;
                }


                String toAddress = merchantPaymentOrders.getUsdtAddr();

                //代付订单号
                String platformOrder = merchantPaymentOrders.getPlatformOrder();

                //订单金额
                BigDecimal orderAmount = merchantPaymentOrders.getOrderAmount();

                //创建代付钱包交易记录
                final WithdrawTronDetail withdrawTronDetail = WithdrawTronDetail.builder()
                        .orderId(platformOrder)
                        .txid(platformOrder)//默认填订单号
                        .symbol("USDT")
                        .fromAddress("")
                        .toAddress(toAddress)
                        .amount(orderAmount)
                        .build();

                // 获取 Tron API Wrapper
                final ApiWrapper wrapper = tronService.getApiWrapper("");

                // 查询中转账户
                TronWallet transferWallet = tronWalletService.queryAddressByType(WalletTypeEnum.TRANSFER.getCode());
                if (transferWallet == null || transferWallet.getTrxBalance().compareTo(FREE_LIMIT) <= 0) {
                    log.error("USDT代付出款失败, 获取中转账户失败或中转账户TRX余额不足 订单号: {}, 目标地址: {}, 中转账户: {}", platformOrder, toAddress, transferWallet);

                    //更新代付钱包交易记录
                    //交易状态2: 失败
                    withdrawTronDetail.setStatus(2);
                    //失败原因
                    withdrawTronDetail.setRemark("中转账户不存在或TRX余额不足");
                    withdrawTronDetailService.save(withdrawTronDetail);

                    //更新代付订单状态 3 失败
                    merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.FAILED.getCode());
                    //转账状态 3 失败
                    merchantPaymentOrders.setTransferStatus(3);
                    //失败原因
                    merchantPaymentOrders.setRemark("中转账户不存在或TRX余额不足");
                    //更新代付订单
                    boolean updateStatus = merchantPaymentOrdersService.updateById(merchantPaymentOrders);

                    //将商户交易中金额退回到商户余额
                    //将交易中金额退回到商户余额
                    //获取商户信息 加上排他行锁
                    MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(merchantPaymentOrders.getMerchantCode());

                    //订单金额总计 (订单金额 + 费用 + 单笔手续费)
                    BigDecimal allAmount = merchantPaymentOrders.getAmount().add(merchantPaymentOrders.getCost()).add(merchantPaymentOrders.getFixedFee());

                    //更新商户余额 将订单金额所需费用划转到交易中金额
                    LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode())  // 指定更新条件 商户号
                            .set(MerchantInfo::getUsdtBalance, merchantInfo.getUsdtBalance().add(allAmount)) // 指定更新字段 (增加商户余额 + 总金额)
                            .set(MerchantInfo::getPendingUsdtBalance, merchantInfo.getPendingUsdtBalance().subtract(allAmount)); // 指定更新字段 (减少交易中金额 - 总金额)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    merchantInfoService.update(null, lambdaUpdateWrapperMerchantInfo);

                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //事务提交成功
                            //发送代付回调通知商户
                            //发送提现失败 异步延时回调通知
                            long millis = 3000L;
                            //发送提现延时回调的MQ消息
                            TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                            rabbitMQService.sendTimeoutTask(taskInfo, millis);

                            log.error("USDT代付出款失败, 获取中转账户失败或中转账户TRX余额不足 发送回调通知商户 订单号: {}, 目标地址: {}, 中转账户: {}", platformOrder, toAddress, transferWallet);
                        }
                    });
                    return true;
                }

                // 获取 USDT 合约实例
                Contract contract = wrapper.getContract(contractAddress);

                // 创建 TRC20 合约对象，使用 USDT 合约地址，出款钱包地址 API Wrapper
                Trc20Contract token = new Trc20Contract(contract, tronWallet.getAddress(), wrapper);

                // 查询出款钱包地址对中转地址的授权金额
                final BigInteger approveAmount = token.allowance(tronWallet.getAddress(), transferWallet.getAddress());

                // 如果授权金额小于 10000000 (10U)，重新进行授权
                if (approveAmount.compareTo(new BigInteger("10000000")) < 0) {
                    // 进行授权操作，将出款钱包地址的 USDT 转账权限授权给中转地址
                    String approve = tronService.approve(tronWallet.getAddress(), tronWallet.getPrivateKey(), transferWallet.getAddress());
                    log.info("USDT代付出款, 进行中转账户授权操作 订单号: {}, 目标地址: {}, 中转账户: {}, 授权返回信息: {}", platformOrder, toAddress, transferWallet, approve);
                }

                // 获取中转地址的剩余能量
                Response.AccountResourceMessage transferResource = wrapper.getAccountResource(transferWallet.getAddress());

                // 计算该地址剩余的能量（Energy）
                // 剩余能量 = 能量限制 - 已使用能量
                final long freeEnergy = transferResource.getEnergyLimit() - transferResource.getEnergyUsed();

                //检查中转账户的能量是否大于等于80000。如果是，表示中转账户能量足够，可以通过中转账户进行转账。
                if (freeEnergy >= 80000) {

                    // 通过中转账户进行转账
                    // 如果中转账户的能量（Energy）足够（大于等于80000），则通过中转账户进行转账
                    final String txId = tronService.transferFrom(tronWallet.getAddress(), transferWallet.getAddress(), transferWallet.getPrivateKey(), toAddress, orderAmount);

                    //转了钱就要把redis标识删除 只处理一次转账
                    redisTemplate.delete("paymentOrderSign:" + merchantPaymentOrders.getPlatformOrder());

                    log.info("USDT代付出款, 通过中转账户进行转账 订单号: {}, 目标地址: {}, 中转账户: {}, 转账结果: {}", platformOrder, toAddress, transferWallet, txId);

                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //事务提交成功
                            // 更新订单状态
                            usdtPaymentOrderService1.updatePaymentOrder1(platformOrder, txId, merchantPaymentOrders, tronWallet.getAddress(), withdrawTronDetail, wrapper);
                        }
                    });
                } else {
                    // 检查出款账户的能量
                    // 如果中转账户的能量不足，则检查出款账户的能量
                    Response.AccountResourceMessage walletResource = wrapper.getAccountResource(tronWallet.getAddress());
                    final long walletEnergy = walletResource.getEnergyLimit() - walletResource.getEnergyUsed();

                    if (walletEnergy >= 64900) {

                        // 通过出款账户直接进行转账
                        // 如果出款账户的能量足够（大于等于64900），则通过出款账户直接进行转账
                        String txId = tronService.transfer(tronWallet.getAddress(), tronWallet.getPrivateKey(), toAddress, orderAmount);

                        //转了钱就要把redis标识删除 只处理一次转账
                        redisTemplate.delete("paymentOrderSign:" + merchantPaymentOrders.getPlatformOrder());

                        log.info("USDT代付出款, 通过出款账户直接进行转账 订单号: {}, 目标地址: {}, 出款账户: {}, txId: {}", platformOrder, toAddress, tronWallet.getAddress(), txId);

                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                //事务提交成功
                                // 更新订单状态
                                usdtPaymentOrderService1.updatePaymentOrder1(platformOrder, txId, merchantPaymentOrders, tronWallet.getAddress(), withdrawTronDetail, wrapper);
                            }
                        });
                    } else {

                        // 尝试租赁能量进行转账
                        // 如果出款账户的能量也不足，则尝试租赁能量

                        //查看用户的余额
                        BigDecimal balance = tronService.queryUSDTBalance(toAddress);

                        //如果用户余额大于 0 那么租 32000 否则租65000
                        boolean rentFlag = rentEnergy2DF(tronWallet.getAddress(), balance.compareTo(BigDecimal.ZERO) > 0 ? 32000L : 65000L, 0);

                        //记录开始时间 最多只等待10秒
                        long startTime = System.currentTimeMillis();

                        if (rentFlag) {

                            // 如果租赁能量成功，循环检查能量是否满足转账需求 最多等待10秒
                            while (System.currentTimeMillis() - startTime < 10000) {

                                //间隔时间: 1秒
                                Thread.sleep(1000);

                                //获取出款账户能量
                                Response.AccountResourceMessage walletResourceThis = wrapper.getAccountResource(tronWallet.getAddress());

                                // 剩余能量 = 能量限制 - 已使用能量
                                final long walletEnergyThis = walletResourceThis.getEnergyLimit() - walletResourceThis.getEnergyUsed();

                                //能量租赁到账
                                if (walletEnergyThis >= 31900 && balance.compareTo(BigDecimal.ZERO) > 0) {

                                    // 如果租赁到的能量大于等于31900且用户USDT余额大于0，进行转账
                                    String txId = tronService.transfer(tronWallet.getAddress(), tronWallet.getPrivateKey(), toAddress, orderAmount);

                                    //转了钱就要把redis标识删除 只处理一次转账
                                    redisTemplate.delete("paymentOrderSign:" + merchantPaymentOrders.getPlatformOrder());

                                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                            //事务提交成功
                                            // 更新订单状态
                                            usdtPaymentOrderService1.updatePaymentOrder1(platformOrder, txId, merchantPaymentOrders, tronWallet.getAddress(), withdrawTronDetail, wrapper);
                                        }
                                    });
                                    break;
                                } else if (walletEnergyThis >= 64900 && balance.compareTo(BigDecimal.ZERO) == 0) {

                                    // 如果租赁到的能量大于等于64900且用户USDT余额为0，进行转账
                                    String txId = tronService.transfer(tronWallet.getAddress(), tronWallet.getPrivateKey(), toAddress, orderAmount);

                                    //转了钱就要把redis标识删除 只处理一次转账
                                    redisTemplate.delete("paymentOrderSign:" + merchantPaymentOrders.getPlatformOrder());

                                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                            //事务提交成功
                                            // 更新订单状态
                                            usdtPaymentOrderService1.updatePaymentOrder1(platformOrder, txId, merchantPaymentOrders, tronWallet.getAddress(), withdrawTronDetail, wrapper);
                                        }
                                    });
                                    break;
                                }
                            }
                        } else {
                            //能量租赁失败
                            log.error("USDT代付出款失败, 能量租赁失败 订单号: {}, 目标地址: {}, 出款账户: {}, 能量租赁结果: {}", platformOrder, toAddress, tronWallet.getAddress(), rentFlag);
                        }
                    }
                }
            } else {
                //没获取到锁
                log.error("USDT代付出款失败, 未获取到redis 分布式锁");
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("USDT代付出款失败,异常 订单号: {}, 目标地址: {}, 出款账户: {}, 出款金额: {}, ex: {}", merchantPaymentOrders.getPlatformOrder(), merchantPaymentOrders.getUsdtAddr(), tronWallet.getAddress(), merchantPaymentOrders.getAmount(), e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return true;
    }


    /**
     * TRX出款处理
     *
     * @param tronWallet            出款账户
     * @param merchantPaymentOrders 代付订单
     * @return boolean
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)//开启新事务
    public boolean autoTransferTRX(TronWallet tronWallet, MerchantPaymentOrders merchantPaymentOrders) {

        //分布式锁key ar-wallet-autoTransfer
        //目前先所有代付订单用同一把锁 串行化  如果出款速度太慢再使用订单号区分
        String key = "uu-wallet-autoTransfer";
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //查询redisSign
                //判断是否存在redis标识 存在才进行处理 (有效期三天)
                Object value = redisTemplate.opsForValue().get("trxPaymentOrderSign:" + merchantPaymentOrders.getPlatformOrder());

                if (value == null) {
                    //不存在订单sign
                    log.error("处理TRX代付订单失败 autoTransfer 不存在订单sign, 订单号: {}, sign: {}", merchantPaymentOrders.getPlatformOrder(), value);
                    return true;
                }

                String paymentOrderSign = String.valueOf(value);

                String md5Sign = MD5Util.generateMD5(merchantPaymentOrders.getMerchantCode() + merchantPaymentOrders.getPlatformOrder() + merchantPaymentOrders.getUsdtAddr() + arProperty.getPaymentOrderKey());

                if (!md5Sign.equals(paymentOrderSign)) {
                    log.error("处理TRX代付订单失败 autoTransfer 代付订单redis签名校验失败, 订单号: {}, paymentOrderSign: {}, md5Sign: {}", merchantPaymentOrders.getPlatformOrder(), paymentOrderSign, md5Sign);
                    return true;
                }

                String toAddress = merchantPaymentOrders.getUsdtAddr();

                //代付订单号
                String platformOrder = merchantPaymentOrders.getPlatformOrder();

                //订单金额
                BigDecimal orderAmount = merchantPaymentOrders.getOrderAmount();

                //创建代付钱包交易记录
                final WithdrawTronDetail withdrawTronDetail = WithdrawTronDetail.builder()
                        .orderId(platformOrder)
                        .txid(platformOrder)//默认填订单号
                        .symbol("TRX")
                        .fromAddress("")
                        .toAddress(toAddress)
                        .amount(orderAmount)
                        .build();

                // 获取 Tron API Wrapper
                final ApiWrapper wrapper = tronService.getApiWrapper("");


                //使用出款账户进行出款 (TRX转账)
                String txId = tronService.transferTrx(tronWallet.getAddress(), tronWallet.getPrivateKey(), toAddress, merchantPaymentOrders.getAmount());

                //转了钱就要把redis标识删除 只处理一次转账
                redisTemplate.delete("trxPaymentOrderSign:" + merchantPaymentOrders.getPlatformOrder());

                log.info("TRX代付出款, 通过出款账户进行转账 订单号: {}, 目标地址: {}, 出款账户: {}, txId: {}", platformOrder, toAddress, tronWallet, txId);

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //事务提交成功
                        // 更新订单状态
                        trxPaymentOrderService1.updatePaymentOrder1(platformOrder, txId, merchantPaymentOrders, tronWallet.getAddress(), withdrawTronDetail, wrapper);
                    }
                });

            } else {
                //没获取到锁
                log.error("TRX代付出款失败, 未获取到redis 分布式锁");
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("TRX代付出款失败,异常 订单号: {}, 目标地址: {}, 出款账户: {}, 出款金额: {}, ex: {}", merchantPaymentOrders.getPlatformOrder(), merchantPaymentOrders.getUsdtAddr(), tronWallet.getAddress(), merchantPaymentOrders.getAmount(), e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return true;
    }

    private static boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj = JSONObject.parseObject(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
}