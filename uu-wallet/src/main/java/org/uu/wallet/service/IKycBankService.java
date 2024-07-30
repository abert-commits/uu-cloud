package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.KycBankDTO;
import org.uu.common.pay.req.KycBankIdReq;
import org.uu.common.pay.req.KycBankListPageReq;
import org.uu.common.pay.req.KycBankReq;
import org.uu.wallet.entity.KycBank;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author
 * @since 2024-04-16
 */
public interface IKycBankService extends IService<KycBank> {

    /**
     * 根据bankCode获取KYC银行信息
     *
     * @param bankCode
     * @return {@link KycBank}
     */
    KycBank getBankInfoByBankCode(String bankCode);

    /**
     *
     * @param req {@link KycBankListPageReq}
     * @return return {@link PageReturn} <{@link KycBankDTO}>
     */
    PageReturn<KycBankDTO> listPage(KycBankListPageReq req);

    List<String> getBankCodeList();


    boolean deleteKycBank(KycBankIdReq req);

    RestResult<KycBankDTO> addKycBank(KycBankReq req);

    RestResult<KycBankDTO> updateKycBank(KycBankReq req);

}
