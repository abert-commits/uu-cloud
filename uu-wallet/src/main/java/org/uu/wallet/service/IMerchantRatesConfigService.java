package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MerchantPayMentDTO;
import org.uu.common.pay.dto.MerchantRatesConfigDTO;
import org.uu.common.pay.req.MerchantRatesConfigPageReq;
import org.uu.common.pay.req.MerchantRatesConfigReq;
import org.uu.wallet.entity.MerchantRatesConfig;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户对应的代收、代付费率设置 服务类
 * </p>
 *
 * @author
 * @since 2024-07-13
 */
public interface IMerchantRatesConfigService extends IService<MerchantRatesConfig> {

    PageReturn<MerchantRatesConfigDTO> merchantRatesConfigListPage(MerchantRatesConfigPageReq req);

    RestResult addMerchantRatesConfig(MerchantRatesConfigReq req);

    boolean deleteMerchantRatesConfig(Long id);

    RestResult updateMerchantRatesConfig(Long id, MerchantRatesConfigReq req);

    RestResult<MerchantRatesConfigDTO> getMerchantRatesConfigById(Long id);

    Map<String, Map<Integer, List<MerchantPayMentDTO>>> getMerchantPayMentDTOs(List<String> merchantCodes, List<Integer> paymentTypes);

    /**
     * 根据商户号获取商户支付类型配置
     *
     * @param type         1: 代收, 2: 代付
     * @param payType      支付类型: 1: 银行卡, 2: USDT, 3: UPI 6: TRX
     * @param merchantCode 商户号
     * @return {@link MerchantRatesConfig }
     */
    MerchantRatesConfig getMerchantRatesConfigByCode(String type, String payType, String merchantCode);

    Map<String, String> getMerchantRates(Integer type, List<String> payTypes, String merchantCode);
}
