package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageRequest;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CurrencyPayTypeDTO;
import org.uu.common.pay.dto.CurrencyPayTypePageDTO;
import org.uu.common.pay.req.CurrencyPayTypeReq;
import org.uu.wallet.entity.CurrencyPayType;

import java.util.List;

/**
 * <p>
 * 货币配置表 服务类
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
public interface ICurrencyPayTypeService extends IService<CurrencyPayType> {

    /**
     * 获取币种对应的代收代付类型
     * type 1:代收 2:代付
     */
    List<CurrencyPayTypeDTO> currencyPayTypeListById(Long currencyId, Integer type);

    PageReturn<CurrencyPayTypePageDTO> currencyPayTypePage(PageRequest req);

    RestResult addCurrencyPayType(CurrencyPayTypeReq req);

    RestResult updateCurrencyPayType(Long id, CurrencyPayTypeReq req);

    boolean deleteCurrencyPayType(Long id);
}
