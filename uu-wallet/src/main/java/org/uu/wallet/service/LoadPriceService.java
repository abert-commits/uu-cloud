package org.uu.wallet.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.uu.wallet.entity.SystemCurrency;
import org.uu.wallet.price.binnace.util.BinanceUtils;
import org.uu.wallet.price.huobi.util.HuobiUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 获取实时汇率
 *
 * @author simon
 * @date 2024/07/15
 */
@Service
@Slf4j
public class LoadPriceService {

    @Autowired
    private ISystemCurrencyService systemCurrencyService;

    /**
     * 获取实时汇率并更新
     *
     * @return boolean
     */
    public boolean loadPrice() {

        // 获取所有系统货币信息列表
        final List<SystemCurrency> currencyList = systemCurrencyService.getAllSystemCurrency();

        // 检查货币列表是否为空
        if (currencyList != null && currencyList.size() > 0) {

            // 遍历每个货币对象
            currencyList.forEach(currency -> {

                // 判断货币代码是否为"U"且货币名称不为"USDT" (法币与U的汇率)
                if (currency.getCurrencyCode().equals("U") && !currency.getCurrencyName().equals("USDT")) {

                    try {
                        // 调用BinanceUtils工具类的方法，获取货币的当前汇率（交易类型为"SELL"）
                        final BigDecimal price = BinanceUtils.readBinanceRate(currency.getCurrencyName(), "SELL");

                        // 如果汇率大于零，则更新该货币的汇率信息
                        if (price.compareTo(BigDecimal.ZERO) > 0) {
                            //更新自动汇率
                            LambdaUpdateWrapper<SystemCurrency> lambdaUpdateWrapperSystemCurrency = new LambdaUpdateWrapper<>();
                            lambdaUpdateWrapperSystemCurrency.eq(SystemCurrency::getId, currency.getId())  // 指定更新条件 id
                                    .set(SystemCurrency::getUsdtAuto, price) // 指定更新字段 (USDT自动汇率)
                                    .set(SystemCurrency::getUpdateTime, LocalDateTime.now()); // 指定更新字段 (更新时间)
                            // 这里传入的 null 表示不更新实体对象的其他字段
                            systemCurrencyService.update(null, lambdaUpdateWrapperSystemCurrency);
                        } else if (StringUtils.isNotBlank(currency.getCoinId()) && StringUtils.isNotBlank(currency.getCurrencyId())) {
                            // 如果币种ID和货币ID都不为空，则尝试使用HuobiUtils获取汇率
                            // 调用HuobiUtils工具类的方法，获取货币的当前汇率
                            final BigDecimal priceHb = HuobiUtils.readBinanceRate(currency.getCoinId(), currency.getCurrencyId());

                            // 如果汇率大于等于零，则更新该货币的汇率信息
                            if (priceHb.compareTo(BigDecimal.ZERO) >= 0) {

                                //更新自动汇率
                                LambdaUpdateWrapper<SystemCurrency> lambdaUpdateWrapperSystemCurrency = new LambdaUpdateWrapper<>();
                                lambdaUpdateWrapperSystemCurrency.eq(SystemCurrency::getId, currency.getId())  // 指定更新条件 id
                                        .set(SystemCurrency::getUsdtAuto, priceHb) // 指定更新字段 (USDT自动汇率)
                                        .set(SystemCurrency::getUpdateTime, LocalDateTime.now()); // 指定更新字段 (更新时间)
                                // 这里传入的 null 表示不更新实体对象的其他字段
                                systemCurrencyService.update(null, lambdaUpdateWrapperSystemCurrency);
                            }
                        }
                    } catch (Exception e) {
                        // 捕获任何异常，并抛出运行时异常
                        log.error("获取实时汇率失败");
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        return true;
    }
}
