package org.uu.wallet.tron.service.impl;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.core.contract.Contract;
import org.tron.trident.core.contract.Trc20Contract;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Response;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.tron.bo.TransferRes;
import org.uu.wallet.tron.service.TronService;
import org.uu.wallet.tron.utils.BigDecimalUtils;
import org.uu.wallet.tron.utils.HttpHelper;
import org.uu.wallet.tron.utils.RSAUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class TronServiceImpl implements TronService {

    private final static Map<String, ApiWrapper> map = new HashMap<>();

    private static final String contractAddress_HEX = "41a614f803b6fd780986a42c78ec9c7f77e6ded13c";

    private static final String contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final BigDecimal USDT_RATE = new BigDecimal(1000000);

    private static final long feeLimit = 40000000;

    // 检查USDT地址是否合法的URL
    private static final String checkUrl = "http://tron.652758.cc:8090/wallet/validateaddress";

    @Autowired
    private ArProperty property;

    /**
     * 获取ApiWrapper实例，用于与TRON网络进行交互
     *
     * @param privateKey 用户的私钥
     * @return ApiWrapper实例
     */
    @Override
    public ApiWrapper getApiWrapper(String privateKey) {

        // 如果私钥不为空，尝试解密私钥
        String keyNew = "";

        if (StringUtils.isNotBlank(privateKey)) {
            try {
                // 使用RSA解密私钥
                keyNew = RSAUtils.deCodeKey(privateKey, property.getTronPrivateKey());
            } catch (Exception ex) {
                // 捕获并记录解密过程中出现的异常
                log.error("getApiWrapper KEY 解密失败  【{}】", privateKey, ex);
            }
        }

        // 生成用于缓存的键值，格式为 "10086_解密后的私钥"
        final String key = String.format("%s_%s", 10086, keyNew);

        // 如果缓存中已存在有效的ApiWrapper实例，直接返回
        if (map.containsKey(key) && map.get(key) != null) {
            ApiWrapper apiWrapper = map.get(key);
            if (!apiWrapper.channelSolidity.isShutdown()) {
                return apiWrapper;
            }
        }

        // 创建新的ApiWrapper实例
        ApiWrapper apiWrapper = new ApiWrapper("tron.652758.cc:50051", "tron.652758.cc:50051",
                StringUtils.isBlank(keyNew) ? "" : keyNew, "");

        // 将新的ApiWrapper实例存入缓存
        map.put(key, apiWrapper);

        // 返回新的ApiWrapper实例
        return apiWrapper;
    }

    /**
     * 实现TRC20代币转账操作
     *
     * @param fromAddress 转出方的地址
     * @param privateKey  转出方的私钥
     * @param toAddress   转入方的地址
     * @param amount      转账金额
     * @return 转账结果（成功时返回交易ID，失败时返回null）
     */
    @Override
    public String transfer(String fromAddress, String privateKey, String toAddress, BigDecimal amount) {
        try {

            // 获取ApiWrapper实例，用于与TRON网络进行交互
            ApiWrapper wrapper = this.getApiWrapper(privateKey);

            // 获取合约实例
            Contract contract = wrapper.getContract(contractAddress);

            // 创建TRC20合约对象，用于执行代币转账
            Trc20Contract token = new Trc20Contract(contract, fromAddress, wrapper);

            // 执行转账操作
            String transactionId = token.transfer(toAddress, BigDecimalUtils.safeMultiply(amount, USDT_RATE).longValue(), 0, "", feeLimit);

            log.info("TRC20转账提交成功, transactionId: {}, fromAddress: {}, toAddress: {}", transactionId, fromAddress, toAddress);

            // 返回转账结果
            return transactionId;  //成功时返回交易ID，失败时返回null）
        } catch (Exception ex) {
            log.error("TRC20转账失败, fromAddress: {}, toAddress: {}, e: {}", fromAddress, toAddress, ex);
        }
        return null;
    }

    /**
     * TRX转账
     *
     * @param fromAddress 发起转账的地址
     * @param privateKey  发起转账地址的私钥
     * @param toAddress   接收转账的地址
     * @param amount      转账金额
     * @return {@link String } 返回交易的结果
     */
    @Override
    public String transferTrx(String fromAddress, String privateKey, String toAddress, BigDecimal amount) {
        try {

            // 获取用于操作TRON区块链的ApiWrapper实例
            ApiWrapper apiWrapper = this.getApiWrapper(privateKey);

            // 生成转账交易
            // 将amount转换为最小单位的long类型并生成转账交易
            Response.TransactionExtention transfer = apiWrapper.transfer(fromAddress, toAddress, BigDecimalUtils.safeMultiply(amount, USDT_RATE).longValue());

            // 对交易进行签名
            Chain.Transaction transaction = apiWrapper.signTransaction(transfer);

            // 广播交易
            String transactionId = apiWrapper.broadcastTransaction(transaction);

            log.info("TRX转账提交成功, transactionId: {}, fromAddress: {}, toAddress: {}", transactionId, fromAddress, toAddress);

            // 返回交易结果
            return transactionId;
        } catch (Exception ex) {
            // 记录错误日志
            log.error("TRX转账失败 【{}】to【{}】【{}】", fromAddress, toAddress, amount, ex);
        }

        // 如果发生异常，返回null
        return null;
    }

    /**
     * 实现TRC20代币转账操作，使用中转账户进行转账
     *
     * @param fromAddress 转出方的地址
     * @param destAddr    中转账户地址
     * @param privateKey  中转账户私钥
     * @param toAddress   转入方的地址
     * @param amount      转账金额
     * @return 转账结果（成功时返回交易ID，失败时返回null）
     */
    @Override
    public String transferFrom(String fromAddress, String destAddr, String privateKey, String toAddress, BigDecimal amount) {
        try {
            // 获取ApiWrapper实例，用于与TRON网络进行交互
            ApiWrapper wrapper = this.getApiWrapper(privateKey);

            // 获取USDT合约实例
            Contract contract = wrapper.getContract(contractAddress);

            // 创建TRC20合约对象，使用USDT合约地址，中转地址和API Wrapper
            Trc20Contract token = new Trc20Contract(contract, destAddr, wrapper);

            // 执行转账操作，通过中转账户代表出款账户进行转账
            // 参数：
            // 1. fromAddress: 转出方的地址
            // 2. toAddress: 转入方的地址
            // 3. BigDecimalUtils.safeMultiply(amount, USDT_RATE).longValue(): 转账金额（单位是USDT的最小单位）
            // 4. 0: 交易的最小单位
            // 5. "": 交易数据，通常为空
            // 6. feeLimit: 最大手续费限额
            String result = token.transferFrom(fromAddress, toAddress, BigDecimalUtils.safeMultiply(amount, USDT_RATE).longValue(), 0, "", feeLimit);

            // 返回转账结果（成功时返回交易ID，失败时返回null）
            return result;
        } catch (Exception ex) {
            // 捕获异常并记录错误日志
            log.error("TRC20转账失败 【{}】to【{}】【{}】", fromAddress, toAddress, amount, ex);
        }
        return null;
    }

    /**
     * 查询指定地址的 USDT 余额
     *
     * @param address 要查询的地址
     * @return 余额（以 USDT 为单位），查询失败时返回 null
     */
    @Override
    public BigDecimal queryUSDTBalance(String address) {
        try {
            // 获取 ApiWrapper 实例，用于与 TRON 网络进行交互
            ApiWrapper wrapper = this.getApiWrapper(null);

            // 获取 USDT 合约实例
            Contract contract = wrapper.getContract(contractAddress);

            // 创建 TRC20 合约对象，使用 USDT 合约地址，查询地址和 API Wrapper
            Trc20Contract token = new Trc20Contract(contract, address, wrapper);

            // 查询指定地址的 USDT 余额
            // USDT 余额单位为最小单位（微USDT），需转换为标准单位（USDT）
            BigDecimal usdtBalance = new BigDecimal(token.balanceOf(address).toString());

            // 将微USDT 转换为 USDT (1 USDT = 1,000,000 微USDT)
            BigDecimal usdtBalanceStandard = BigDecimalUtils.safeDivide(usdtBalance, USDT_RATE);

            // 返回转换后的 USDT 余额
            return usdtBalanceStandard;
        } catch (Exception e) {
            // 捕获异常并记录错误日志
            log.error("获取 USDT 余额失败, address: " + address, e);
        }
        // 查询失败时返回 null
        return null;
    }

    /**
     * 授权
     * 授权是指将一个账户的某个代币的转账权限授予另一个账户。
     * 在这个场景中，授权 出款账户 的 USDT 转账权限给 中转账户，使得 中转账户 可以代表 出款账户 进行 USDT 转账操作。
     *
     * @param fromAddress
     * @param privateKey
     * @param toAddress
     * @return {@link String }
     */
    @Override
    public String approve(String fromAddress, String privateKey, String toAddress) {
        try {
            // 获取 ApiWrapper 实例，用于与 Tron 区块链进行交互
            ApiWrapper wrapper = this.getApiWrapper(privateKey);

            // 获取 USDT 合约实例
            Contract contract = wrapper.getContract(contractAddress);

            // 创建 TRC20 合约对象，使用 USDT 合约地址，钱包地址和 API Wrapper
            Trc20Contract token = new Trc20Contract(contract, fromAddress, wrapper);

            // 执行授权操作，将 fromAddress 的 USDT 转账权限授权给 toAddress
            // 参数：
            // 1. toAddress: 被授权的地址
            // 2. 1000000000000L: 授权金额 100万U(单位是 USDT 的最小单位，6 个小数位)
            // 3. 6: USDT 的小数位数
            // 4. "": 交易数据，通常为空
            // 5. feeLimit: 最大手续费限额
            return token.approve(toAddress, 1000000000000l, 6, "", feeLimit);
        } catch (Exception e) {
            log.error("USDT代付出款失败, 授权交易失败 目标地址: {}, e: {}", toAddress, e);
        }
        return null;
    }

    /**
     * 检查TRON地址是否有效
     *
     * @param address 要检查的TRON地址
     * @return 如果地址有效，返回true；否则返回false
     */
    @Override
    public boolean checkAddress(String address) {
        // 创建参数映射，将地址作为参数传递
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("address", address);
        }};

        // 发送POST请求，检查地址有效性
        final String result = HttpHelper.postMethod(checkUrl, params);

        // 解析返回的结果为JSON对象
        JSONObject object = JSONObject.parseObject(result);

        // 如果结果不为空，并且result字段为true且message字段以"Base58check"开头，则表示地址有效
        if (object != null && object.getBooleanValue("result") && object.getString("message").startsWith("Base58check")) {
            return true; // 地址有效
        } else {
            return false; // 地址无效
        }
    }
}
