package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.req.AccountChangeReq;
import org.uu.wallet.entity.AccountChange;
import com.baomidou.mybatisplus.extension.service.IService;

import org.uu.wallet.vo.AccountChangeVo;

import java.util.List;
import java.util.Map;

/**
 * @author
 */
public interface IAccountChangeService extends IService<AccountChange> {

    /**
     * 查询商户账变记录
     * @param accountChangeReq
     * @return
     */
    PageReturn<AccountChangeVo> queryAccountChangeList(AccountChangeReq accountChangeReq);

    Map<Integer, String> fetchAccountType();


     AccountChangeVo queryTotal(AccountChangeReq req);

}
