package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.CancellationRechargeDTO;
import org.uu.common.pay.req.CancellationRechargePageListReq;
import org.uu.wallet.entity.CancellationRecharge;

import java.util.List;

/**
 * @author
 */
public interface ICancellationRechargeService extends IService<CancellationRecharge> {

    PageReturn<CancellationRechargeDTO> listPage(CancellationRechargePageListReq req);


    /**
     * 获取买入取消原因列表
     *
     * @return {@link List}<{@link String}>
     */
    List<String> getBuyCancelReasonsList();
}
