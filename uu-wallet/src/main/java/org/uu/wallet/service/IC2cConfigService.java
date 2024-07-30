package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.C2cConfigDTO;
import org.uu.wallet.entity.C2cConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.entity.CancellationRecharge;
import org.uu.wallet.req.C2cConfigReq;
import org.uu.wallet.req.CancellationRechargeReq;

/**
* @author 
*/
    public interface IC2cConfigService extends IService<C2cConfig> {

    PageReturn<C2cConfigDTO> listPage(C2cConfigReq req);


}
