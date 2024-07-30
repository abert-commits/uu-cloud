package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.wallet.entity.TradeConfig;

import java.math.BigDecimal;


/**
 * @author
 */
public interface ITradeConfigService extends IService<TradeConfig> {

    PageReturn<TradeConfigDTO> listPage(TradeConfigListPageReq req);

    TradeConfigVoiceEnableDTO updateVoiceEnable(TradeConfigVoiceEnableReq req);

    TradeWarningConfigDTO updateWarningConfig(TradeConfigWarningConfigUpdateReq req);

    TradeWarningConfigDTO warningConfigDetail(TradeConfigIdReq req);

    TradeManualConfigDTO manualReview();

    /**
     * 验证买入奖励比例
     *
     * @param buyRewardRatio 买入奖励比例
     * @return TRUE-通过  False-不通过
     */
    Boolean verifyBuyRewardRatio(BigDecimal buyRewardRatio, BigDecimal buyRewardRatioMax);
}
