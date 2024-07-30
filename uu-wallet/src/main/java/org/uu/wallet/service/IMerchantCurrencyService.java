package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantCurrencyDTO;
import org.uu.common.pay.req.MerchantCurrencyReq;
import org.uu.wallet.entity.MerchantCurrency;

import java.util.List;

/**
 * <p>
 * 货币配置表 服务类
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
public interface IMerchantCurrencyService extends IService<MerchantCurrency> {


    List<MerchantCurrencyDTO> allCurrency();

    PageReturn<MerchantCurrencyDTO> currencyPage(PageRequest req);

    RestResult addCurrency(MerchantCurrencyReq req);

    RestResult updateSystemCurrency(Long id, MerchantCurrencyReq req);

    boolean deleteSystemCurrency(Long id);
}
