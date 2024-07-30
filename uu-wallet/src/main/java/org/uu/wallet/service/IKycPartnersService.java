package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.KycPartnersDTO;
import org.uu.common.pay.req.KycPartnerIdReq;
import org.uu.common.pay.req.KycPartnerListPageReq;
import org.uu.common.pay.req.KycPartnerMemberReq;
import org.uu.common.pay.req.KycPartnerUpdateReq;
import org.uu.wallet.entity.KycPartners;
import org.uu.wallet.bo.ActiveKycPartnersBO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * kyc信息表 服务类
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
public interface IKycPartnersService extends IService<KycPartners> {

    /**
     * 获取当前会员正在连接中的kyc
     */
    ActiveKycPartnersBO getActiveKycPartnersByMemberId(String memberId);

    /**
     * 获取KYC列表
     *
     * @param memberId
     * @return {@link List}<{@link KycPartners}>
     */
    List<KycPartners> getKycPartners(Long memberId);

    /**
     * 获取kyc总数
     * @param memberId
     * @return
     */
    int getKycPartnersCount(Long memberId);

    /**
     * 获取KYC列表
     *
     * @param collectType collectType
     * @return {@link List}<{@link KycPartners}>
     */
    List<KycPartners> getKycPartners(Long memberId, Integer collectType);

    /**
     * 获取已连接的用户kyc列表
     * @param memberId
     * @param collectType
     * @return
     */
    List<KycPartners> getAvailableKycPartners(Long memberId, Integer collectType);

    /**
     * 获取
     *
     * @param kycPartnerListPageReq KycPartnerReq {@link KycPartnerListPageReq}
     * @return {@link PageReturn} <{@link KycPartnersDTO}>
     */
    PageReturn<KycPartnersDTO> listPage(KycPartnerListPageReq kycPartnerListPageReq);


    /**
     * 删除
     *
     * @param req {@link KycPartnerIdReq} req
     * @return boolean
     */
    boolean delete(KycPartnerIdReq req);


    /**
     * 根据upi_id 获取 KYC
     *
     * @param upiId
     * @return {@link KycPartners}
     */
    KycPartners getKYCPartnersByUpiId(String upiId);

    /**
     * 根据id获取kyc
     * @param kycId kycId
     * @return
     */
    KycPartners getKYCPartnersById(String kycId);

    /**
     * 连接kyc
     */
    void kycPartnersLink();

    /**
     * 检查账号是否重复添加
     * @param memberId 会员id
     * @param bankCode 银行code
     * @param account 账号
     * @return
     */
    boolean checkDuplicates(Long memberId, String bankCode, String account);


    /**
     * 检查upiId是否重复添加
     * @param upiId upiId
     * @return boolean
     */
    boolean checkDuplicatesByUpiId(String upiId);


    /**
     * 通过账号和银行编码确定kyc信息
     * @param account account
     * @param bankCode bankCode
     * @return
     */
    KycPartners getKycPartnerByAccount(String account, String bankCode);

    /**
     * 通过会员id获取蚂蚁的kyc连接partner列表
     * @param memberId
     * @return
     */
    List<KycPartners> getKycPartnerByMemberId(String memberId);

    /**
     *
     * @param memberId memberId
     * @return {@link List}
     */
    List<String> getUpiIdByMemberId(String memberId);

    /**
     * @param kycId kycId
     * @param amount amount
     * @param type type
     * @return boolean
     */
    boolean updateTransactionInfo(String kycId, BigDecimal amount, String type);


    KycPartners updateKycPartner(KycPartnerUpdateReq req);

    List<KycPartnersDTO> getKycPartnerByMemberId(KycPartnerMemberReq req);

    /**
     * 查看当前会员是否有正在连接中的kyc 如果有的话 返回一个kyc信息
     *
     * @param memberId
     * @return {@link KycPartners }
     */
    KycPartners hasActiveKycForCurrentMember(Long memberId);
}
