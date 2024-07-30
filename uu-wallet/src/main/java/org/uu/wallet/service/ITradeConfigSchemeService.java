package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.TradeConfigSchemeDTO;
import org.uu.common.pay.req.TradeConfigSchemeListPageReq;
import org.uu.common.pay.req.TradeConfigSchemeReq;
import org.uu.wallet.entity.TradeConfigScheme;

/**
 * <p>
 * 交易配置方案表 服务类
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
public interface ITradeConfigSchemeService extends IService<TradeConfigScheme> {
    PageReturn<TradeConfigSchemeDTO> listPage(TradeConfigSchemeListPageReq req);
    TradeConfigSchemeDTO getDetail(Long id);

    TradeConfigSchemeDTO updateScheme(TradeConfigSchemeReq req);


    /**
     * 根据标签获取方案配置
     *
     * @param schemeTag
     * @return {@link TradeConfigScheme}
     */
    TradeConfigScheme getSchemeConfigByTag(String schemeTag);
}
