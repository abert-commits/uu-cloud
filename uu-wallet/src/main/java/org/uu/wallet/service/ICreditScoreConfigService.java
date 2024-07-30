package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CreditScoreConfigDTO;
import org.uu.common.pay.req.CreditScoreConfigListPageReq;
import org.uu.common.pay.req.CreditScoreConfigUpdateReq;
import org.uu.wallet.entity.CreditScoreConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 信用分配置表 服务类
 * </p>
 *
 * @author
 * @since 2024-04-09
 */
public interface ICreditScoreConfigService extends IService<CreditScoreConfig> {
    PageReturn<CreditScoreConfigDTO> listPage(CreditScoreConfigListPageReq req);

    RestResult<CreditScoreConfigDTO> updateScore(CreditScoreConfigUpdateReq req);

    CreditScoreConfig getCreditScoreConfig(Integer eventType, Integer tradeType);
}
