package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.SystemCurrencyDTO;
import org.uu.common.pay.dto.SystemCurrencyPageDTO;
import org.uu.common.pay.req.SystemCurrencyReq;
import org.uu.wallet.entity.SystemCurrency;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 货币配置表 服务类
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
public interface ISystemCurrencyService extends IService<SystemCurrency> {

    /**
     * 获取所有币种汇率
     *
     * @return {@link List }<{@link SystemCurrency }>
     */
    List<SystemCurrency> getAllSystemCurrency();

    List<SystemCurrencyDTO> allCurrency();

    PageReturn<SystemCurrencyPageDTO> currencyPage(PageRequest req);

    RestResult addCurrency(SystemCurrencyReq req);

    RestResult updateSystemCurrency(Long id, SystemCurrencyReq req);

    boolean deleteSystemCurrency(Long id);

    /**
     * 获取指定货币汇率
     *
     * @param currencyName
     * @return {@link BigDecimal }
     */
    BigDecimal getCurrencyExchangeRate(String currencyName);
}
