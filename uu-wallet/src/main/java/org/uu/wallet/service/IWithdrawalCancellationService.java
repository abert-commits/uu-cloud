package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.WithdrawalCancellationDTO;
import org.uu.common.pay.req.WithdrawalCancellationReq;
import org.uu.wallet.entity.WithdrawalCancellation;

import java.util.List;


/**
* @author 
*/
    public interface IWithdrawalCancellationService extends IService<WithdrawalCancellation> {

         PageReturn<WithdrawalCancellationDTO> listPage(WithdrawalCancellationReq req);


    /**
     * 获取卖出取消原因列表
     *
     * @return {@link List}<{@link String}>
     */
    List<String> getSellCancelReasonsList();
    }
