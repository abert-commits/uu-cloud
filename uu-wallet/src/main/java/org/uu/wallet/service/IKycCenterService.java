package org.uu.wallet.service;

import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.message.KycCompleteMessage;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.req.PaidParamReq;
import org.uu.wallet.entity.KycTransactionMessage;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.req.*;
import org.uu.wallet.vo.KycBanksVo;
import org.uu.wallet.vo.KycBindLinkStatusVo;
import org.uu.wallet.vo.KycPartnersVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IKycCenterService {


    /**
     * 获取KYC列表
     *
     * @return {@link KycRestResult}<{@link List}<{@link KycPartnersVo}>>
     */
    KycRestResult<List<KycPartnersVo>> getKycPartners(KycPartnerListReq req);

    /**
     * 获取可连接KYC列表
     * @param req req
     * @return {@link KycRestResult}<{@link List}<{@link KycPartnersVo}>>
     */
    KycRestResult<List<KycPartnersVo>> getAvailableKycPartners(KycPartnerListReq req);


    /**
     * 添加 KYC Partner
     *
     * @param kycPartnerReq
     * @param request
     * @return {@link ApiResponse}
     */
    KycRestResult addKycPartner(KycPartnerReq kycPartnerReq, HttpServletRequest request);

    /**
     * 校验kyc信息
     * @param kycPartnerReq kycPartnerReq
     * @return
     */
    KycRestResult checkKycPartnerParams(KycPartnerReq kycPartnerReq);


    /**
     * 连接KYC
     *
     * @param linkKycPartnerReq
     * @param request
     * @return {@link KycRestResult}
     */
    KycRestResult linkKycPartner(LinkKycPartnerReq linkKycPartnerReq, HttpServletRequest request);

    /**
     * 获取银行列表
     *
     * @param req
     * @return {@link KycRestResult<>}
     */
    KycRestResult<List<KycBanksVo>> getBanks(KycBankReq req);


    /**
     * 判断KYC是否在线
     *
     * @param req
     * @return {@link Boolean}
     */
//    Boolean effective(AppToken req);

    /**
     * 开始卖出
     *
     * @param kycSellReq
     * @param request
     * @return {@link KycRestResult}
     */
    KycRestResult startSell(KycSellReq kycSellReq, HttpServletRequest request);


    /**
     * 停止卖出
     *
     * @param kycSellReq
     * @param request
     * @return {@link KycRestResult}
     */
    KycRestResult stopSell(KycSellReq kycSellReq, HttpServletRequest request);

    /**
     * 开始拉取交易记录
     * @param req req
     * @return KycRestResult
     */
    KycRestResult startPullTransaction(KycAutoCompleteReq req);

    /**
     * 停止拉取交易记录
     *
     * @param sellerOrder sellerOrder
     * @param buyerOrder  buyerOrder
     * @return KycRestResult
     */
    KycRestResult stopPullTransaction(String sellerOrder, String buyerOrder);

    /**
     * 定时任务拉取交易记录
     */
    void pullTransactionJob();

    /**
     * 修改kyc激活状态
     * @param kycActivateReq
     * @return
     */
    KycRestResult activate(KycActivateReq kycActivateReq);


    /**
     * 查询kyc绑定情况:是否有绑定kyc 绑定的kyc是否有活跃的链接
     * @return
     */
    RestResult<KycBindLinkStatusVo> queryKycBindlinkStatus();

    /**
     * 已支付
     * @param req req
     */
    KycRestResult paid(PaidParamReq req);

    /**
     * 未支付
     * @param req req
     */
    KycRestResult unPaid(PaidParamReq req);


    /**
     * 完成支付
     * @return {@link KycRestResult}
     */
    KycRestResult completePayment(KycAutoCompleteReq req);
}
