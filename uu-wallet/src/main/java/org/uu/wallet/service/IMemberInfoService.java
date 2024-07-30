package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.bo.MemberInfoBO;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.common.pay.req.MemberInfoReq;
import org.uu.common.pay.vo.request.MemberAccountChangeDetailRequestVO;
import org.uu.common.pay.vo.request.MemberAccountChangeRequestVO;
import org.uu.common.pay.vo.response.MemberAccountChangeDetailResponseVO;
import org.uu.common.pay.vo.response.MemberAccountChangeResponseVO;
import org.uu.wallet.dto.GenerateTokenForWallertDTO;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.req.*;
import org.uu.wallet.vo.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
public interface IMemberInfoService extends IService<MemberInfo> {

    /*
     * 获取会员列表
     * */
    PageReturn<MemberInfolistPageDTO> listPage(MemberInfoListPageReq req);

    /*
     * 获取当前会员信息
     * */
    MemberInfoVo currentMemberInfo();

    /**
     * 根据用户名获取认证用户信息，携带角色和密码
     *
     * @param username
     * @return
     */
    MemberAuthDTO getByUsername(String username);

    /**
     * 更新会员: 扣除余额 (将会员余额转到到冻结金额中)、将进行中的卖出订单数+1 累计卖出次数 + 1
     *
     * @param memberInfo
     * @param amount
     * @return {@link Boolean}
     */
    Boolean updatedMemberInfo(MemberInfo memberInfo, BigDecimal amount);

    /**
     * 根据手机号获取会员信息
     *
     * @param phoneNumber
     * @return {@link MemberInfo}
     */
    MemberInfo getMemberByPhoneNumber(String phoneNumber);

    /**
     * 根据邮箱号获取会员信息
     *
     * @param emailAccount
     * @return {@link MemberInfo}
     */
    MemberInfo getMemberByEmailAccount(String emailAccount);

    /**
     * 重置会员登录密码
     *
     * @param id
     * @param passwd
     * @return {@link Boolean}
     */
    Boolean resetPassword(Long id, String passwd);

    /**
     * 根据会员id更新手机号和会员账号
     *
     * @param id
     * @param newPhoneNumber
     * @return {@link Boolean}
     */
    Boolean updatePhoneNumber(String id, String newPhoneNumber);

    /**
     * 根据会员id更新邮箱号
     *
     * @param id
     * @param newEmail
     * @return {@link Boolean}
     */
    Boolean updateEmail(String id, String newEmail);

    /**
     * 实名认证
     *
     * @param id
     * @param realName
     * @param idCardNumber
     * @param fileName
     * @param facePhoto
     * @return {@link Boolean}
     */
    Boolean idenAuthentication(Long id, String realName, String idCardNumber, String fileName, String facePhoto);

    /**
     * 根据实名信息获取会员信息
     *
     * @param idCardNumber
     * @return {@link MemberInfo}
     */
    MemberInfo getMemberByCardNumber(String idCardNumber);


    MemberInfolistPageDTO recharge(MemberInfoRechargeReq req);


    MemberInfolistPageDTO withdrawal(MemberInfoWithdrawalReq req);


    MemberInfolistPageDTO freeze(MemberInfoFreezeReq req) throws Exception;


    MemberInfolistPageDTO unfreeze(MemberInfoFreezeReq req) throws Exception;


    MemberInfolistPageDTO bonus(MemberInfoBonusReq req);


    MemberInfolistPageDTO resetpwd(MemberInfoIdReq req);

    MemberInfolistPageDTO remark(MemberInfoIdReq req);


    MemberInfoDTO getInfo(MemberInfoIdGetInfoReq req);

    /**
     * 获取当前会员信息
     *
     * @return {@link RestResult}<{@link MemberInformationVo}>
     */
    RestResult<MemberInformationVo> getCurrentMemberInfo();

    /**
     * 实名认证处理
     *
     * @param idCardNumber
     * @param realName
     * @param idCardImage
     * @param facePhoto
     * @return {@link RestResult}
     */
    RestResult idenAuthenticationProcess(String idCardNumber, String realName, MultipartFile idCardImage, MultipartFile facePhoto);

    /**
     * 发送短信验证码
     *
     * @param sendSmsCodeReq
     * @return {@link RestResult}
     */
    RestResult sendSmsCode(SendSmsCodeReq sendSmsCodeReq, HttpServletRequest request);

    /**
     * 发送邮箱验证码
     *
     * @param sendEmailCodeReq
     * @param request
     * @return {@link RestResult}
     */
    RestResult sendEmailCode(SendEmailCodeReq sendEmailCodeReq, HttpServletRequest request);

    /**
     * 更换手机号处理
     *
     * @param verifySmsCodeReq
     * @return {@link RestResult}
     */
    RestResult updatePhoneNumberProcess(VerifySmsCodeReq verifySmsCodeReq);

    /**
     * 更换邮箱号处理
     *
     * @param bindEmailReq
     * @return {@link RestResult}
     */
    RestResult updateEmailProcess(BindEmailReq bindEmailReq);


    /**
     * 手机号注册处理
     *
     * @param phoneSignUpReq
     * @param request
     * @return {@link RestResult}<{@link PhoneSignUpVo}>
     */
    GenerateTokenForWallertDTO phoneSignUp(PhoneSignUpReq phoneSignUpReq, HttpServletRequest request);

    /**
     * 生成登录token
     *
     * @param generateTokenForWallertDTO
     * @return {@link RestResult}<{@link PhoneSignUpVo}>
     */
    RestResult<PhoneSignUpVo> generateTokenForWallet(GenerateTokenForWallertDTO generateTokenForWallertDTO);

    /**
     * 邮箱账号注册处理
     *
     * @param emailSignUpReq
     * @param request
     * @return {@link RestResult}
     */
    RestResult emailSignUp(EmailSignUpReq emailSignUpReq, HttpServletRequest request);

    /**
     * 忘记密码处理
     *
     * @param resetPasswordReq
     * @return {@link RestResult}
     */
    RestResult resetPasswordProcess(ResetPasswordReq resetPasswordReq);


    /**
     * 邮箱注册-验证码校验
     *
     * @param emailSignUpReq
     * @return {@link Boolean}
     */
    Boolean signUpValidateEmailCode(EmailSignUpReq emailSignUpReq);


    /**
     * 手机号注册-验证码校验
     *
     * @param validateSmsCodeReq
     * @return {@link Boolean}
     */
    Boolean signUpValidateSmsCode(ValidateSmsCodeReq validateSmsCodeReq);


    /**
     * 获取当前会员信息
     *
     * @return {@link MemberInfo}
     */
    MemberInfo getMemberInfo();

    /**
     * 设置头像
     *
     * @param updateAvatarReq
     * @return {@link RestResult}
     */
    RestResult updateAvatar(UpdateAvatarReq updateAvatarReq);

    /**
     * 设置昵称
     *
     * @param updateNicknameReq
     * @return {@link RestResult}
     */
    RestResult updateNickname(UpdateNicknameReq updateNicknameReq);


    /**
     * 设置新支付密码
     *
     * @param newPaymentPasswordReq
     * @return {@link RestResult}
     */
    RestResult setNewPaymentPassword(NewPaymentPasswordReq newPaymentPasswordReq);

    /**
     * 修改支付密码
     *
     * @param updatePaymentPasswordReq
     * @return {@link RestResult}
     */
    RestResult updatePaymentPassword(UpdatePaymentPasswordReq updatePaymentPasswordReq);

    /**
     * 查看会员是否实名认证
     *
     * @return {@link RestResult}<{@link verificationStatusVo}>
     */
    RestResult<verificationStatusVo> verificationStatus();

    /**
     * 忘记支付密码
     *
     * @param resetPaymentPasswordReq
     * @return {@link RestResult}
     */
    RestResult resetPaymentPassword(ResetPaymentPasswordReq resetPaymentPasswordReq);

    /**
     * 更新会员买入统计信息
     *
     * @param memberId
     * @return {@link Boolean}
     */
    Boolean updateAddBuyInfo(String memberId);


    /**
     * 获取USDT汇率和支付类型
     *
     * @return {@link RestResult}<{@link UsdtCurrencyAndPayTypeVo}>
     */
    RestResult<UsdtCurrencyAndPayTypeVo> getUsdtCurrencyAndPayType();

    /**
     * 更新被申诉人信息 (增加被申诉次数)
     *
     * @param memberId
     * @return {@link Boolean}
     */
    Boolean updateAddAppealCount(String memberId);

    /**
     * 查看该会员是否被注册 (会员id 手机号)
     *
     * @param memberId
     * @param mobileNumber
     */
    MemberInfo checkMemberRegistered(String memberId, String mobileNumber);

    /**
     * 查看该会员是否被注册 (会员id 手机号)
     *
     * @param memberId
     * @return {@link MemberInfo}
     */
    MemberInfo checkMemberRegistered(String memberId);

    /**
     * 根据会员Id 获取会员信息
     *
     * @param memberId
     * @return {@link MemberInfo}
     */
    MemberInfo getMemberInfoByMemberId(String memberId);


    /**
     * 根据会员Id 获取会员信息
     *
     * @param memberId
     * @return {@link MemberInfo}
     */
    MemberInfo getMemberInfoById(String memberId);


    /**
     * 设置首次登录信息
     *
     * @param memberId
     * @param firstLoginIp
     * @param firstLoginTime
     * @return {@link Boolean}
     */
    Boolean setFirstLoginInfo(Long memberId, String firstLoginIp, LocalDateTime firstLoginTime);

    /**
     * 更新最后一次登录时间
     *
     * @param memberId
     * @param loginIp
     * @return
     */
    Boolean updateLastLoginInfo(Long memberId, String loginIp);

    PageReturn<MemberRealNamelistPageDTO> realName(MemberInfoRealNameListReq req);

    MemberInfolistPageDTO resetPayPwd(MemberInfoIdReq req);


    /**
     * 将会员被申诉次数+1
     *
     * @return {@link Boolean}
     */
    Boolean incrementMemberComplaintCount(MemberInfo memberInfo);

    PageReturn<MerchantMemberInfoPageDTO> merchantListPage(MemberInfoListPageReq req);

    /**
     * 完成实名认证任务
     *
     * @return {@link Boolean}
     */
    Boolean completeRealNameVerificationTask();


    PageReturn<MemberInfolistPageDTO> relationMemberList(MemberInfoListPageReq req);

    /**
     * 禁用会员，踢出登录
     *
     * @param memberId
     * @param operator
     * @return
     */
    void disableMember(String memberId, String operator, String remark);

    /**
     * 根据登录IP获取会员ID列表
     *
     * @param ip
     * @return
     */
    List<String> getMembersByByLoginIp(String ip);


    RestResult<MemberInfolistPageDTO> updateCreditScore(MemberInfoCreditScoreReq req);

    RestResult<MemberCreditScoreInfoDTO> getCreditScoreInfo(MemberCreditScoreInfoIdReq req);

    RestResult<MemberCreditScoreInfoDTO> getCreditScoreInfo();

    RestResult<List<CashBackOrderProcessDTO>> cashBackBatch(MemberInfoBatchIdReq req);

    /**
     * 完成新手引导
     *
     * @param type 1:买入引导 2:卖出引导
     * @return
     */
    RestResult finishNewUserGuide(Integer type);

    List<MemberLevelDTO> getMemberLevelInfo();

    void memberUpgrade(String memberId);

    AppVersionManagerDTO getAppVersionInfo(String currentVersion, Integer device);

    /**
     * 校验手机号是否被使用
     *
     * @param checkPhoneNumberAvailabilityReq
     * @return {@link RestResult}<{@link CheckPhoneNumberAvailabilityVo}>
     */
    RestResult<CheckPhoneNumberAvailabilityVo> checkPhoneNumberAvailability(CheckPhoneNumberAvailabilityReq checkPhoneNumberAvailabilityReq);


    /**
     * 处理新人任务历史数据
     *
     * @param taskType
     * @return
     */
    Boolean processHistoryNewUserTask(String taskType);

    /**
     * 获取每日公告内容
     *
     * @param language
     * @return {@link DailyAnnouncementVo}
     */
    RestResult<DailyAnnouncementVo> getDailyAnnouncement(Integer language);


    /**
     * 标记用户已查看今日公告
     *
     * @return {@link RestResult}
     */
    RestResult markAnnouncementAsViewed();

    /**
     * 标记用户余额退回提示弹窗
     * @return {@link RestResult}
     */
    RestResult markCashBackAttention();

    /**
     * 后台创建会员
     *
     * @param memberInfoReq
     * @param request
     * @return {@link RestResult}<{@link MemberInfolistPageDTO}>
     */
    RestResult<MemberInfolistPageDTO> createMemberInfo(MemberInfoReq memberInfoReq, HttpServletRequest request);

    /**
     * 获取当前会员帐变记录数据
     *
     * @return
     */
    RestResult<PageReturn<MemberAccountChangeResponseVO>> getMemberAccountChangeInfo(MemberAccountChangeRequestVO requestVO);

    /**
     * 获取帐变记录详情
     * @param requestVO
     * @return
     */
    RestResult<MemberAccountChangeDetailResponseVO> getMemberAccountChangeDetail(MemberAccountChangeDetailRequestVO requestVO);

    /**
     * 根据用户ID查询用户信息
     * @param uidList 用户ID
     * @return 用户信息
     */
    List<MemberInfoBO> queryMemberInfoByIds(List<Long> uidList);

    /**
     * 获取交易记录
     * 从买入表collection_order 卖出表payment_order usdt充值表中usdt_buy_order 查询出交易记录
     * @param viewTransactionHistoryReq
     * @return PageReturn<ViewTransactionHistoryVo>
     */
    RestResult<PageReturn<ViewTransactionHistoryVo>> viewTransactionHistory(ViewTransactionHistoryReq viewTransactionHistoryReq);

    /**
     * 查询团队信息
     * @param requestVO 请求实体类
     */
    RestResult<PageReturn<GroupInfoDTO>> findGroupInfo(FindGroupInfoReq requestVO);

    /**
     * 更新登录密码
     * @param requestVO 请求实体类
     */
    RestResult<Void> updatePassword(UpdateLoginPasswordReq requestVO);
}
