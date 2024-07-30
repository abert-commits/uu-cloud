package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.BankInfoDTO;
import org.uu.common.pay.req.*;
import org.uu.wallet.entity.BankInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.wallet.vo.BankInfoVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 银行表 服务类
 * </p>
 *
 * @author 
 * @since 2024-06-07
 */
public interface IBankInfoService extends IService<BankInfo> {
    RestResult<BankInfoDTO> add(BankInfoReq req);
    RestResult<BankInfoDTO> detail(BankInfoIdReq req);
    PageReturn<BankInfoDTO> listPage(BankInfoListPageReq req);
    RestResult update(BankInfoUpdateReq req);
    RestResult deleteInfo(BankInfoIdReq req);
    RestResult updateStatus(BankInfoUpdateStatusReq req);
    RestResult<Map<String, String>> getBankCodeMap();

    /**
     * 获取银行列表
     *
     * @return {@link RestResult }<{@link List }<{@link BankInfoVo }>>
     */
    RestResult<List<BankInfoVo>> getBankList();
}
