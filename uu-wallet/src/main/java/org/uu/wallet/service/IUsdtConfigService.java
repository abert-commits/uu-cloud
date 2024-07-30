package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.UsdtConfigDTO;
import org.uu.common.pay.req.UsdtConfigPageReq;
import org.uu.wallet.entity.UsdtConfig;

import java.util.List;

/**
 * @author
 */
public interface IUsdtConfigService extends IService<UsdtConfig> {

    PageReturn<UsdtConfigDTO> listPage(UsdtConfigPageReq req);

    /**
     * 匹配USDT收款信息
     *
     * @param networkProtocol
     * @return {@link UsdtConfig}
     */
    UsdtConfig matchUsdtReceiptInfo(String networkProtocol);


    /**
     * 获取主网络下拉列表
     *
     * @return {@link List}
     */
    List getNetworkProtocol();
}