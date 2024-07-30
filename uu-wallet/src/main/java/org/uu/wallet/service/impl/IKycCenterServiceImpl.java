package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.core.websocket.send.member.MemberWebSocketSendMessage;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.enums.MemberWebSocketMessageTypeEnum;
import org.uu.common.core.message.CommissionAndDividendsMessage;
import org.uu.common.core.message.KycCompleteMessage;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.enums.OrderEventEnum;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.common.pay.req.PaidParamReq;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.common.web.exception.BizException;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.*;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.*;
import org.uu.wallet.service.*;
import org.uu.wallet.util.*;
import org.uu.wallet.vo.*;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.uu.wallet.webSocket.MemberMessageSender;
import org.uu.wallet.webSocket.massage.OrderStatusChangeMessage;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class IKycCenterServiceImpl implements IKycCenterService {

    @Autowired
    private IKycPartnersService kycPartnersService;

    @Autowired
    private IMemberInfoService memberInfoService;

    @Autowired
    private IKycBankService kycBankService;

    @Autowired
    private IKycApprovedOrderService kycApprovedOrderService;

    @Autowired
    private OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private RedissonUtil redissonUtil;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ArProperty arProperty;

    @Resource
    RabbitMQService rabbitMQService;


    @Resource
    AmountChangeUtil amountChangeUtil;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    PaymentOrderMapper paymentOrderMapper;
    @Autowired
    private MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    @Autowired
    private CollectionOrderMapper collectionOrderMapper;
    @Autowired
    private MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;
    @Autowired
    private DelegationOrderMapper delegationOrderMapper;
    @Autowired
    private MemberMessageSender memberMessageSender;
    @Autowired
    private MemberInfoMapper memberInfoMapper;
    @Resource
    ISystemCurrencyService systemCurrencyService;
    @Resource
    MerchantInfoMapper merchantInfoMapper;
    @Resource
    DelegationOrderUtil delegationOrderUtil;

    /**
     * 获取KYC列表
     *
     * @return {@link KycRestResult}<{@link List}<{@link KycPartnersVo}>>
     */
    @Override
    public KycRestResult<List<KycPartnersVo>> getKycPartners(KycPartnerListReq req) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取KYC列表失败: 获取会员信息失败");
            return KycRestResult.failure(ResultCode.RELOGIN);
        }

        Integer collectType = req.getCollectType();

        //查询银行列表
        List<KycBank> kycBankList = kycBankService.lambdaQuery().eq(KycBank::getDeleted, 0)
                .list();

        List<KycPartners> kycPartners = kycPartnersService.getKycPartners(memberInfo.getId(), collectType);

        if (kycPartners == null) {
            kycPartners = new ArrayList<>();
        }

        ArrayList<KycPartnersVo> kycPartnersVoList = new ArrayList<>();

        for (KycPartners kycPartner : kycPartners) {
            KycPartnersVo kycPartnersVo = new KycPartnersVo();

            BeanUtils.copyProperties(kycPartner, kycPartnersVo);

            for (KycBank kycBank : kycBankList) {
                if (kycBank.getBankCode().equals(kycPartner.getBankCode())) {
                    //设置银行连接地址
                    kycPartnersVo.setLinkUrl(kycBank.getLinkUrl());

                    //设置银行连接方式
                    kycPartnersVo.setLinkType(kycBank.getLinkType());

                    //设置 图标地址
                    kycPartnersVo.setIconUrl(kycBank.getIconUrl());

                    break;
                }
            }

            if (kycPartnersVo.getStatus() == 1) {
                kycPartnersVo.setRemark("Working");
            } else {
                kycPartnersVo.setRemark("Please relink the tool or modify the upi and relink.");
            }

            kycPartnersVoList.add(kycPartnersVo);
        }

        return KycRestResult.ok(kycPartnersVoList);
    }

    @Override
    public KycRestResult<List<KycPartnersVo>> getAvailableKycPartners(KycPartnerListReq req) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取KYC列表失败: 获取会员信息失败");
            return KycRestResult.failure(ResultCode.RELOGIN);
        }

        Integer collectType = req.getCollectType();

        //查询银行列表
        List<KycBank> kycBankList = kycBankService.lambdaQuery().eq(KycBank::getDeleted, 0)
                .list();
        int count = kycPartnersService.getKycPartnersCount(memberInfo.getId());
        List<KycPartners> kycPartners = kycPartnersService.getAvailableKycPartners(memberInfo.getId(), collectType);

        if (kycPartners == null) {
            kycPartners = new ArrayList<>();
        }

        ArrayList<KycPartnersVo> kycPartnersVoList = new ArrayList<>();

        for (KycPartners kycPartner : kycPartners) {
            KycPartnersVo kycPartnersVo = new KycPartnersVo();

            BeanUtils.copyProperties(kycPartner, kycPartnersVo);

            for (KycBank kycBank : kycBankList) {
                if (kycBank.getBankCode().equals(kycPartner.getBankCode())) {
                    //设置银行连接地址
                    kycPartnersVo.setLinkUrl(kycBank.getLinkUrl());

                    //设置银行连接方式
                    kycPartnersVo.setLinkType(kycBank.getLinkType());

                    //设置 图标地址
                    kycPartnersVo.setIconUrl(kycBank.getIconUrl());

                    break;
                }
            }

            if (kycPartnersVo.getStatus() == 1) {
                kycPartnersVo.setRemark("Working");
            } else {
                kycPartnersVo.setRemark("Please relink the tool or modify the upi and relink.");
            }

            kycPartnersVoList.add(kycPartnersVo);
        }

        KycRestResult<List<KycPartnersVo>> ok = KycRestResult.ok(kycPartnersVoList);
        ok.setTotal(count);
        return ok;
    }


    /**
     * 添加 KYC Partner
     *
     * @param kycPartnerReq
     * @param request
     * @return {@link ApiResponse}
     */
    @Override
    public KycRestResult addKycPartner(KycPartnerReq kycPartnerReq, HttpServletRequest request) {
        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();
        String lockKey = "ar-addKycPartner" + memberInfo.getId();
        RLock lock = redissonUtil.getLock(lockKey);
        boolean block = false;
        try {
            block = lock.tryLock(10, TimeUnit.SECONDS);
            if (block) {
                // 查询银行信息
                KycBank kycBank = kycBankService.getBankInfoByBankCode(kycPartnerReq.getBankCode());

                KycRestResult kycRestResult = checkKycPartnerParams(kycPartnerReq, memberInfo, kycBank);

                if (!kycRestResult.getCode().equals(ResultCode.SUCCESS.getCode())) {
                    return kycRestResult;
                }

                IAppBankTransaction appBankTransaction = SpringContextUtil.getBean(kycPartnerReq.getBankCode());

                KycBankResponseVo linkKycPartner = appBankTransaction.linkKycPartner(kycPartnerReq.getToken());

                if (linkKycPartner.getStatus()) {

                    KycPartners kycPartners = new KycPartners();

                    BeanUtils.copyProperties(kycPartnerReq, kycPartners);

                    kycPartners.setCollectionType(Integer.parseInt(kycPartnerReq.getCollectionType()));

                    if (kycPartnerReq.getCollectionType().equals(PayTypeV2Enum.BANK_CARD.getCode())) {
                        if (ObjectUtils.isEmpty(kycPartnerReq.getBankCardNumber())
                                || ObjectUtils.isEmpty(kycPartnerReq.getBankCardOwner())
                                || ObjectUtils.isEmpty(kycPartnerReq.getBankCardIfsc())
                        ) {
                            return KycRestResult.failed(ResultCode.KYC_BANK_INFO_MISSING);
                        }
                        kycPartners.setBankCardIfsc(kycPartnerReq.getBankCardIfsc());
                        kycPartners.setBankCardOwner(kycPartnerReq.getBankCardOwner());
                        kycPartners.setBankCardNumber(kycPartnerReq.getBankCardNumber());
                    }
                    // 会员id
                    kycPartners.setMemberId(String.valueOf(memberInfo.getId()));
                    if(ObjectUtils.isNotEmpty(kycPartnerReq.getUpiId())
                            && kycPartnerReq.getUpiId().contains("@")
                    ){
                        String upiId = kycPartners.getUpiId();
                        String[] split = upiId.split("@");
                        if(split.length > 2){
                            return KycRestResult.failed("upiId input invalid");
                        }
                        String account = split[0];
                        kycPartners.setAccount(account);
                    }
                    // 会员账号
                    kycPartners.setMemberAccount(memberInfo.getMemberAccount());

                    // 会员手机号
                    kycPartners.setMobileNumber(memberInfo.getMobileNumber());

                    // 银行名称
                    kycPartners.setBankName(kycBank.getBankName());

                    // 图标地址
                    kycPartners.setIconUrl(kycBank.getIconUrl());

                    // 来源
                    kycPartners.setSourceType(kycPartnerReq.getSourceType());

                    kycPartners.setStatus(1);

                    // 计算总共的kycPartner
                    int totalKycPartner = ObjectUtils.isEmpty(memberInfo.getTotalKycPartner()) ? 0 : memberInfo.getTotalKycPartner();
                    memberInfo.setTotalKycPartner(totalKycPartner + 1);
                    // 更新kyc绑定数量
                    memberInfoService.updateById(memberInfo);
                    return kycPartnersService.save(kycPartners) ? KycRestResult.ok(kycPartners.getId()) : KycRestResult.failed();
                }
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error(e.getMessage());
            return KycRestResult.failed();
        }
        return KycRestResult.failure(ResultCode.KYC_LINK_FAILED);
    }

    @Override
    public KycRestResult checkKycPartnerParams(KycPartnerReq kycPartnerReq){
        MemberInfo memberInfo = memberInfoService.getMemberInfo();
        // 查询银行信息
        KycBank kycBank = kycBankService.getBankInfoByBankCode(kycPartnerReq.getBankCode());
        return checkKycPartnerParams(kycPartnerReq, memberInfo, kycBank);
    }

    /**
     * 检查kyc入参信息
     * @param kycPartnerReq req
     * @param memberInfo memberInfo
     * @param kycBank kycBank
     * @return {@link KycRestResult}
     */
    public KycRestResult checkKycPartnerParams(KycPartnerReq kycPartnerReq, MemberInfo memberInfo, KycBank kycBank){
        if (memberInfo == null) {
            log.error("校验 KYC Partner 参数失败: 获取会员信息失败");
            return KycRestResult.failure(ResultCode.RELOGIN);
        }

        if (ObjectUtils.isEmpty(kycBank)) {
            log.error("校验 KYC Partner 参数失败: 获取KYC银行信息失败");
            return KycRestResult.failure(ResultCode.KYC_BANK_NOT_FOUND);
        }

        // 检查请求来源和银行支持来源
        if(!Objects.equals(kycBank.getSourceType(), kycPartnerReq.getSourceType()) && kycBank.getSourceType() != 0){
            log.error("校验 KYC Partner 参数失败: 银行不支持该来源");
            return KycRestResult.failure(ResultCode.KYC_BANK_NOT_FOUND);
        }



        if(ObjectUtils.isEmpty(kycBank)){
            log.error("校验 KYC Partner 参数失败: kycBank不存在: 会员信息: {}, kycBank:{}", memberInfo, kycBank);
            return KycRestResult.failure(ResultCode.KYC_BANK_NOT_FOUND);
        }

        if(kycPartnerReq.getSourceType() == 2){

            if(ObjectUtils.isEmpty(kycPartnerReq.getAccount())){
                log.error("校验 KYC Partner 参数失败: 参数非法");
                return KycRestResult.failure(ResultCode.ACCOUNT_INPUT_ERROR);
            }

            String paymentPassword = kycPartnerReq.getPaymentPassword();

            //校验支付密码
            if (!passwordEncoder.matches(paymentPassword, memberInfo.getPaymentPassword())) {
                log.error("校验 KYC Partner 参数失败: 支付密码错误: 会员信息: {}, 支付密码:{}", memberInfo, paymentPassword);
                return KycRestResult.failure(ResultCode.PASSWORD_VERIFICATION_FAILED);
            }
            // 判断手机号是否被重复使用
            if((kycPartnersService.checkDuplicates(memberInfo.getId(), kycPartnerReq.getBankCode(), kycPartnerReq.getAccount()))){
                return KycRestResult.failed(ResultCode.KYC_ACCOUNT_DUPLICATE);
            }
        }else{
            // 判断手机号是否被重复使用
            if((kycPartnersService.checkDuplicatesByUpiId(kycPartnerReq.getUpiId()))){
                return KycRestResult.failed(ResultCode.KYC_ACCOUNT_DUPLICATE);
            }
        }
        return KycRestResult.ok();
    }


    /**
     * 连接KYC
     *
     * @param linkKycPartnerReq
     * @param request
     * @return {@link ApiResponse}
     */
    @Override
    public KycRestResult linkKycPartner(LinkKycPartnerReq linkKycPartnerReq, HttpServletRequest request) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("连接KYC失败: 获取会员信息失败");
            return KycRestResult.failure(ResultCode.RELOGIN);
        }

        //查询KYC 判断该KYC是否属于该会员
        KycPartners KycPartner = kycPartnersService.lambdaQuery().eq(KycPartners::getId, linkKycPartnerReq.getId()).eq(KycPartners::getDeleted, 0).one();

        if (KycPartner == null || !KycPartner.getMemberId().equals(String.valueOf(memberInfo.getId()))) {
            log.error("连接KYC失败: KycPartner为null 或 KycPartner 不属于该会员, KycPartner: {}, 会员信息: {}", KycPartner, memberInfo);
            return KycRestResult.failure(ResultCode.DATA_NOT_FOUND);
        }

        //判断如果状态是连接成功, 就不进行连接了
//        if (KycPartner.getLinkStatus() == 1) {
//            log.info("连接KYC 当前状态是已连接, 不进行操作, KycPartner: {}, memberInfo: {}", KycPartner, memberInfo);
//            return KycRestResult.ok();
//        }

        //获取对应的银行实现类
        IAppBankTransaction appBankTransaction = SpringContextUtil.getBean(KycPartner.getBankCode());

        //连接银行
        KycBankResponseVo linkKycPartner = appBankTransaction.linkKycPartner(linkKycPartnerReq.getToken());

        //判断是否连接成功 修改连接状态
        if (linkKycPartner.getStatus()) {
            //连接成功
            log.info("连接KYC银行成功, KycPartner: {}, 会员信息: {}", KycPartner, memberInfo);

            // 创建一个 UpdateWrapper 对象，用于构建更新条件和指定更新字段
            LambdaUpdateWrapper<KycPartners> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(KycPartners::getId, KycPartner.getId())  // 指定更新条件，这里以 id 为条件
                    .set(KycPartners::getToken, linkKycPartnerReq.getToken()) // 指定更新字段
                    .set(KycPartners::getStatus, 1); // 指定更新字段

            // 这里传入的 null 表示不更新实体对象的其他字段
            boolean update = kycPartnersService.update(null, lambdaUpdateWrapper);

            return KycRestResult.ok();
        } else {

            log.error("连接KYC银行失败, KycPartner: {}, 会员信息: {}", KycPartner, memberInfo);

            //将连接状态改为未连接

            // 创建一个 UpdateWrapper 对象，用于构建更新条件和指定更新字段
            LambdaUpdateWrapper<KycPartners> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(KycPartners::getId, KycPartner.getId())  // 指定更新条件，这里以 id 为条件
                    .set(KycPartners::getStatus, 0); // 指定更新字段

            // 这里传入的 null 表示不更新实体对象的其他字段
            boolean update = kycPartnersService.update(null, lambdaUpdateWrapper);

            // 连接失败 将银行返回的信息返回给APP客户端
            return KycRestResult.failure(ResultCode.KYC_CONNECTION_FAILED, linkKycPartner.getMsg());
        }

        //TODO 将连接状态推送给APP端
    }

    /**
     * 获取银行列表
     *
     * @return {@link ApiResponse}
     */
    @Override
    public KycRestResult<List<KycBanksVo>> getBanks(KycBankReq req) {
        // 默认app请求
        List<KycBank> kycBankList = kycBankService.lambdaQuery()
                .eq(KycBank::getDeleted, 0)
                .eq(KycBank::getStatus, 1)
                .eq(KycBank::getType, req.getType())
                .and(t -> t.or().eq(KycBank::getSourceType, req.getSourceType()).or().eq(KycBank::getSourceType , 0))
                .list();

        ArrayList<KycBanksVo> kycBanksVoList = new ArrayList<>();

        for (KycBank kycBank : kycBankList) {

            KycBanksVo kycBanksVo = new KycBanksVo();

            BeanUtils.copyProperties(kycBank, kycBanksVo);

            kycBanksVoList.add(kycBanksVo);
        }

        return KycRestResult.ok(kycBanksVoList);
    }


    /**
     * 判断KYC是否在线
     *
     * @param req
     * @return {@link Boolean}
     */
//    @Override
//    public Boolean effective(AppToken req) {
//        AppToken appToken = appTokenService.lambdaQuery().eq(AppToken::getMemberId, req.getMemberId()).one();
//        Map<String, String> mapHeader = new HashMap<>();
//        mapHeader.put("Cookie", appToken.getToken());
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("direction", "DEBIT");
//        String a = RequestUtil.getForAppJson("https://www.freecharge.in/thv/moneydirection", jsonObject, mapHeader);
//        JSONObject jsonFreechargeObject = JSONObject.parseObject(a);
//        JSONArray dataArry = jsonFreechargeObject.getJSONArray("data");
//        if (dataArry.size() >= 1) {
//            return true;
//        }
//        return false;
//    }


    /**
     * 开始卖出
     *
     * @param kycSellReq
     * @param request
     * @return {@link KycRestResult}
     */
    @Override
    public KycRestResult startSell(KycSellReq kycSellReq, HttpServletRequest request) {

//        //获取当前会员信息
//        MemberInfo memberInfo = memberInfoService.getMemberInfo();
//
//        if (memberInfo == null) {
//            log.error("KYC 开始卖出处理失败: 获取会员信息失败");
//            return KycRestResult.failure(ResultCode.RELOGIN);
//        }
//
//        //查询KYC 判断该KYC是否属于该会员
//        KycPartners KycPartner = kycPartnersService.lambdaQuery().eq(KycPartners::getId, kycSellReq.getId()).eq(KycPartners::getDeleted, 0).one();
//
//        if (KycPartner == null || !KycPartner.getMemberId().equals(String.valueOf(memberInfo.getId()))) {
//            log.error("KYC 开始卖出处理失败: KycPartner为null 或 KycPartner 不属于该会员, KycPartner: {}, 会员信息: {}", KycPartner, memberInfo);
//            return KycRestResult.failure(ResultCode.DATA_NOT_FOUND);
//        }
//
//        // 创建一个 UpdateWrapper 对象，用于构建更新条件和指定更新字段
//        LambdaUpdateWrapper<KycPartners> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//        lambdaUpdateWrapper.eq(KycPartners::getId, KycPartner.getId())  // 指定更新条件，这里以 id 为条件
//                .set(KycPartners::getSellStatus, 1); // 指定只更新 sellStatus 字段
//
//        // 使用服务层的 update 方法，传入 null 和 updateWrapper
//        // 这里传入的 null 表示不更新实体对象的其他字段
//        boolean update = kycPartnersService.update(null, lambdaUpdateWrapper);

//        return update ? KycRestResult.ok() : KycRestResult.failed();
        return KycRestResult.ok();
    }


    /**
     * 停止卖出
     *
     * @param kycSellReq
     * @param request
     * @return {@link KycRestResult}
     */
    @Override
    public KycRestResult stopSell(KycSellReq kycSellReq, HttpServletRequest request) {

//        //获取当前会员信息
//        MemberInfo memberInfo = memberInfoService.getMemberInfo();
//
//        if (memberInfo == null) {
//            log.error("KYC 停止卖出处理失败: 获取会员信息失败");
//            return KycRestResult.failure(ResultCode.RELOGIN);
//        }
//
//        //查询KYC 判断该KYC是否属于该会员
//        KycPartners KycPartner = kycPartnersService.lambdaQuery().eq(KycPartners::getId, kycSellReq.getId()).eq(KycPartners::getDeleted, 0).one();
//
//        if (KycPartner == null || !KycPartner.getMemberId().equals(String.valueOf(memberInfo.getId()))) {
//            log.error("KYC 停止卖出处理失败: KycPartner为null 或 KycPartner 不属于该会员, KycPartner: {}, 会员信息: {}", KycPartner, memberInfo);
//            return KycRestResult.failure(ResultCode.DATA_NOT_FOUND);
//        }
//
//        // 创建一个 UpdateWrapper 对象，用于构建更新条件和指定更新字段
//        LambdaUpdateWrapper<KycPartners> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
//        lambdaUpdateWrapper.eq(KycPartners::getId, KycPartner.getId())  // 指定更新条件，这里以 id 为条件
//                .set(KycPartners::getSellStatus, 0); // 指定只更新 sellStatus 字段
//
//        // 使用服务层的 update 方法，传入 null 和 updateWrapper
//        // 这里传入的 null 表示不更新实体对象的其他字段
//        boolean update = kycPartnersService.update(null, lambdaUpdateWrapper);
//
//        return update ? KycRestResult.ok() : KycRestResult.failed();
        return KycRestResult.ok();
    }

    private BankKycTransactionVo findTransactionByUTR(List<BankKycTransactionVo> transactions, String utr) {
        for (BankKycTransactionVo transaction : transactions) {
            if (transaction.getUTR().equals(utr)) {
                return transaction;
            }
        }
        return null;
    }


    /**
     * 校验金额 交易类型 交易状态
     *
     * @param transaction
     * @param message
     * @return boolean
     */
    private boolean verifyTransaction(BankKycTransactionVo transaction, KycTransactionMessage message, KycPartners kycPartners) {

        //校验 收款人 UPI_ID是否一致
        if (!message.getRecipientUPI().equals(kycPartners.getUpiId())) {
            log.error("通过 KYC 验证完成订单 处理失败, 收款人 UPI_ID不一致, 卖出订单号: {}, kycTransactionMessage: {}, KYC信息: {}, transaction: {}", message.getSellerOrderId(), message, kycPartners, transaction);
            return false;
        }

        if (transaction.getAmount().compareTo(message.getAmount()) != 0) {
            log.error("通过 KYC 验证完成订单 处理失败, 订单金额不匹配, 卖出订单号: {}, kycTransactionMessage: {}, KYC信息: {}, transaction: {}", message.getSellerOrderId(), message, kycPartners, transaction);
            return false;
        }
        if (!"1".equals(transaction.getMode())) {
            log.error("通过 KYC 验证完成订单 处理失败, 交易类型不是收入, 卖出订单号: {}, kycTransactionMessage: {}, KYC信息: {}, transaction: {}", message.getSellerOrderId(), message, kycPartners, transaction);
            return false;
        }
        if (!"1".equals(transaction.getOrderStatus())) {
            log.error("通过 KYC 验证完成订单 处理失败, 交易状态不是成功, 卖出订单号: {}, kycTransactionMessage: {}, KYC信息: {}, transaction: {}", message.getSellerOrderId(), message, kycPartners, transaction);
            return false;
        }

        return true;
    }

    /**
     * 开始拉取交易记录
     */
    @Transactional
    @Override
    public KycRestResult startPullTransaction(KycAutoCompleteReq req) {
        String key = req.getSellerOrder() + req.getBuyerOrder();
        // 设置对应的过期时间
        redisUtil.setKycAutoCompleteExpireTime(key, arProperty.getKycAutoCompleteExpireTime());
        // 先执行一遍 如果成功直接退出
        boolean preProcess = pullTransaction(req, key);
        if(preProcess){
            return KycRestResult.ok();
        }
        // 失败则加入hash队列等待定时任务执行
        redisUtil.addKycAutoCompleteHash(key, JSONObject.toJSONString(req));
        return KycRestResult.ok();
    }

    /**
     * 停止拉取交易记录
     */
    @Override
    public KycRestResult stopPullTransaction(String sellerOrder, String buyOrder) {
        String key = sellerOrder + buyOrder;
        redisUtil.delKycAutoCompleteHash(key);
        // 删除过期时间阈值
        if(redisUtil.hasKycAutoCompleteExpireTime(key)){
            redisUtil.delKycAutoCompleteExpireTime(key);
        }
        return KycRestResult.ok();
    }

    @Transactional
    @Override
    public void pullTransactionJob(){
        log.info("开始拉取银行交易记录信息");
        // 获取待拉取hash列表
        Map<String, String> kycAutoCompleteHash = redisUtil.getKycAutoCompleteHash();
        if(ObjectUtils.isEmpty(kycAutoCompleteHash)){
            log.info("中断拉取银行交易记录信息,带拉取列表为空");
            return;
        }
        for (Map.Entry<String, String> stringStringEntry : kycAutoCompleteHash.entrySet()) {
            String key = stringStringEntry.getKey();
            String value = stringStringEntry.getValue();
            KycAutoCompleteReq kycAutoCompleteReq = JSONObject.parseObject(value, KycAutoCompleteReq.class);
            pullTransaction(kycAutoCompleteReq, key);
        }
        log.info("结束拉取银行交易记录信息");
    }

    @Override
    public KycRestResult activate(KycActivateReq req) {
        MemberInfo memberInfo = memberInfoService.getMemberInfo();
        Long id = req.getId();
        LambdaUpdateWrapper<KycPartners> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(KycPartners::getId, id);
        KycPartners kycPartner = kycPartnersService.getById(id);
        if(ObjectUtils.isEmpty(kycPartner)){
            return KycRestResult.failed(ResultCode.DATA_NOT_FOUND);
        }
        Integer currentStatus = kycPartner.getStatus();
        // 当前状态为未启用 设置为启用
        if(currentStatus == 0){
            //获取对应的银行实现类
            IAppBankTransaction appBankTransaction = SpringContextUtil.getBean(kycPartner.getBankCode());
            //连接银行
            KycBankResponseVo linkKycPartner = appBankTransaction.linkKycPartner(kycPartner.getToken());
            if(!linkKycPartner.getStatus()){
                return KycRestResult.failed(ResultCode.KYC_NOT_CONNECTED);
            }
            // 修改连接状态
            lambdaUpdateWrapper.set(KycPartners::getStatus, 1);
        }else{
            // 关闭
            lambdaUpdateWrapper.set(KycPartners::getStatus, 0);
        }
        boolean update = kycPartnersService.update(null, lambdaUpdateWrapper);
        if(!update){
            return KycRestResult.failed(ResultCode.DELEGATION_FAILED);
        }
        if(currentStatus == 1){
            // 关闭委托订单
            boolean closeDelegation = delegationOrderUtil.closeDelegation(memberInfo);
            if(!closeDelegation){
                return KycRestResult.failed(ResultCode.DELEGATION_FAILED);
            }
        }
        return KycRestResult.ok();
    }

    @Override
    public RestResult<KycBindLinkStatusVo> queryKycBindlinkStatus() {
        //1.获取当前会员信息
        //获取当前会员信息
//        Long currentUserId = UserContext.getCurrentUserId();
//        MemberInfo memberInfo = memberInfoService.getMemberInfoByMemberId(currentUserId.toString());
//        MemberInfo memberInfo = memberInfoService.getMemberInfoByMemberId("580275");
        MemberInfo memberInfo = memberInfoService.getMemberInfo();
        //2.获取当前会员绑定的kyc的列表
        List<KycPartners> kycPartners = kycPartnersService.getKycPartners(memberInfo.getId());
        KycBindLinkStatusVo kycBindStatusVo = new KycBindLinkStatusVo();
        kycBindStatusVo.setHasEnableKyc(false);
        //3.判断列表是否为空 为空则表示没有绑定kyc 不为空则表示已有绑定的kyc
        if (kycPartners.isEmpty()){
            kycBindStatusVo.setHasBindKyc(false);
        } else {
            kycBindStatusVo.setHasBindKyc(true);
            for (KycPartners kycPartner : kycPartners) {
                if(kycPartner.getStatus()==1){
                    kycBindStatusVo.setHasEnableKyc(true);
                }
            }
        }
        return RestResult.ok(kycBindStatusVo);
    }

    @Transactional
    public boolean pullTransaction(KycAutoCompleteReq req, String key) {
        String lockKey = "ar-wallet-pullTransaction" + key;
        RLock lock = redissonUtil.getLock(lockKey);
        boolean block = false;
        try {
            block = lock.tryLock(10, TimeUnit.SECONDS);
            if(block){
                if(!redisUtil.hasKycAutoCompleteExpireTime(key)){
                    redisUtil.delKycAutoCompleteHash(key);
                    log.error("通过 KYC 验证完成订单 处理失败, 已到达时间阈值, 不再处理, KycAutoCompleteReq: {}, kycKey: {}", req, key);
                    return false;
                }
                // 查询拉取方kycPartner信息
                KycPartners kycPartners = kycPartnersService.getKYCPartnersById(req.getKycId());
                if(ObjectUtils.isEmpty(kycPartners)){
                    log.error("通过 KYC 验证完成订单 处理失败, KYC不存在或状态未开启或未连接银行, KycAutoCompleteReq: {}, KYC信息: {}", req, kycPartners);
                    return false;
                }
                // 订单开始，需要拉取卖方的交易记录
                IAppBankTransaction appBankTransaction = SpringContextUtil.getBean(kycPartners.getBankCode());
                List<BankKycTransactionVo> kycBankTransactions = appBankTransaction.getKYCBankTransactions(kycPartners.getToken());
                for (BankKycTransactionVo kycBankTransaction : kycBankTransactions) {
                    // 获取收入订单信息
                    String mode = kycBankTransaction.getMode();
                    String recipientUpi = kycBankTransaction.getRecipientUPI();
                    BigDecimal amount = kycBankTransaction.getAmount();
                    BigDecimal orderAmount = req.getOrderAmount();
                    String orderStatus = kycBankTransaction.getOrderStatus();
                    String utr = kycBankTransaction.getUTR();
                    // 判断充值还是提现
                    // 充值需监听蚂蚁的收入记录，提现需监听蚂蚁的支出记录
                    if(!req.getType().equals(mode)
                            || !"1".equals(orderStatus)
                    ) {
                        // 监听卖家的流水信息，和交易类型不匹配和失败记录 跳过
                        continue;
                    }
                    // 如果监控蚂蚁收入记录则需要排除 不再收款列表中的记录 (用户充值)
                    if("1".equals(mode)
                            // 充值时有utr比对
                            && !utr.equals(req.getUtr())
                    ){
                        continue;
                    }
                    // 如果监控蚂蚁支出记录则需要排除 收款信息为空或者收款方upi和提现用户upi不一致的情况 (用户提现)
                    if("2".equals(mode)
                            && (ObjectUtils.isEmpty(req.getWithdrawUpi()) || !Objects.equals(recipientUpi, req.getWithdrawUpi()))
                    ){
                        continue;
                    }
                    // 加入时间参数判断
                    // 订单完成时间超过现在24小时 则跳过处理
                    LocalDateTime createTime = kycBankTransaction.getCreateTime();
                    LocalDateTime now = LocalDateTime.now();
                    String secondsBetween = DurationCalculatorUtil.secondsBetween(createTime, now);
                    int diffHour = (Integer.parseInt(secondsBetween) / 60 / 60);
                    if(diffHour > 24){
                        continue;
                    }
                    // 如果金额比对上了判断是否已经入库
                    if(amount.compareTo(orderAmount) == 0
                            && ! kycApprovedOrderService.checkKycTransactionExistsByUtr(kycBankTransaction.getUTR())
                    ){
                        // 未入库进行入库操作
                        boolean result = saveKycApprovedOrder(req, kycBankTransaction, kycPartners);
                        if(result){
                            // 关闭自动拉取
                            redisUtil.delKycAutoCompleteHash(key);
                            if("1".equals(req.getType())){
                                // 发送结算mq
                                 sendKycCompleteMsg(req, String.valueOf(req.getKycId()), kycBankTransaction.getDetail());
                            }
                            // 匹配上的银行交易记录 更新进订单表中
                            if(saveKycTransactionDetail(req, JSONObject.toJSONString(kycBankTransaction))){
                                // 退出循环
                                return true;
                            }
                        }
                    }
                }
                log.error("通过 KYC 验证完成订单 处理失败, 没有该笔UTR的交易记录, KycAutoCompleteReq: {}, KYC信息: {}, kycBankTransactions: {}", req, kycPartners, kycBankTransactions);
            }
        }catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("通过 KYC 验证完成订单 处理失败, KYC不存在或状态未开启或未连接银行, KycAutoCompleteReq: {}, 报错信息: {}", req, e.getMessage());
            return false;
        } finally {
            //释放锁
            if (block && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * kyc结算操作-账变、回调
     * @param msg msg
     * @return boolean
     */
    @Transactional
    public boolean transactionSuccessHandler(KycCompleteMessage msg) {
        String key = "ar-wallet-KycAutoCompleted" + msg.getSellerOrder();
        RLock lock = redissonUtil.getLock(key);
        String antiMemberId = "";
        String order = "";
        boolean block = false;
        String remark = msg.getRemark();
        if(ObjectUtils.isEmpty(msg.getRemark())){
            remark = msg.isManual() ? "kyc手动完成订单" : "kyc自动完成订单";
        }
        LocalDateTime now = LocalDateTime.now();
        String callBackOrderNo;
        try {
            block = lock.tryLock(10, TimeUnit.SECONDS);
            if(block){
                BigDecimal divide = null;
                MemberAccountChangeEnum currentChangeEnum = null;
                int merchantOrderStatusUpdate = 0;
                int orderStatusUpdate = 0;
                String orderStatus = OrderStatusEnum.BE_PAID.getCode();
                String orderType = MemberAccountChangeEnum.RECHARGE.getCode();
                String messageType = "-1";
                OrderEventEnum orderEventEnum = null;
                // 充值
                if("1".equals(msg.getType()) || Objects.equals(msg.getType(), "3")){
                    callBackOrderNo = msg.getBuyerOrder();
                    String createBy = msg.getSellerMemberId();
                    if(ObjectUtils.isNotEmpty(msg.isManual()) && msg.isManual()){
                        createBy = msg.getUpdateBy();
                    }
                    // 扣除蚂蚁账户的交易中金额 加商户余额 改变订单状态 加账变
                    // 根据币种获取账变类型
                    if(msg.getCurrency().equals(CurrenceEnum.INDIA.getCode())){
                        currentChangeEnum = MemberAccountChangeEnum.WITHDRAW;
                    }
                    divide = amountChangeUtil.kycAutoCompleteAccountChange(msg.getSellerMemberId(), msg.getOrderAmount(), ChangeModeEnum.SUB, currentChangeEnum, msg.getCurrency(), msg.getSellerOrder(), msg.getBuyerOrder(), createBy, remark);
                    // 获取订单信息
                    MerchantCollectOrders merchantCollectOrders = merchantCollectOrdersMapper.selectMerchantCollectOrdersForUpdate(msg.getBuyerOrder());
                    PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(msg.getSellerOrder());
                    if(ObjectUtils.isEmpty(merchantCollectOrders)){
                        throw new BizException("kyc自动完成 订单不存在!");
                    }
                    if(!merchantCollectOrders.getOrderStatus().equals(CollectionOrderStatusEnum.BE_PAID.getCode())
                        || !paymentOrder.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode())
                    ){
                        throw new BizException("kyc自动完成 订单状态异常!");
                    }
                    // 确定回调订单号为买家订单号
                    // 注册事务同步回调 事务提交成功后才执行以下操作
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //异步通知
                            TaskInfo taskInfo = new TaskInfo(callBackOrderNo, TaskTypeEnum.DEPOSIT_NOTIFICATION.getCode(), System.currentTimeMillis());
                            rabbitMQService.sendRechargeSuccessCallbackNotification(taskInfo);
                        }
                    });
                    // 更新商户订单状态
                    merchantCollectOrders.setUtr(msg.getUtr());
                    merchantCollectOrders.setOrderStatus(CollectionOrderStatusEnum.PAID.getCode());
                    merchantCollectOrders.setCompletionTime(now);
                    merchantCollectOrders.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(merchantCollectOrders.getCreateTime(), now));
                    if(ObjectUtils.isNotEmpty(msg.getUpdateBy())){
                        merchantCollectOrders.setUpdateBy(msg.getUpdateBy());
                        paymentOrder.setUpdateBy(msg.getUpdateBy());
                    }
                    merchantOrderStatusUpdate = merchantCollectOrdersMapper.updateById(merchantCollectOrders);
                    // 更新蚂蚁订单状态
                    paymentOrder.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
//                    paymentOrder.setKycTradeDetail(msg.getDetail());
                    paymentOrder.setCompletionTime(now);
                    paymentOrder.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(paymentOrder.getCreateTime(), now));
                    orderStatusUpdate = paymentOrderMapper.updateById(paymentOrder);
                    orderEventEnum = OrderEventEnum.MERCHANT_COLLECTION_ORDER_SUCCESS;

                    antiMemberId = msg.getSellerMemberId();
                    // 将蚂蚁订单状态置为成功
                    orderStatus = OrderStatusEnum.SUCCESS.getCode();
                    // 将蚂蚁订单类型置为卖出订单
                    order = msg.getSellerOrder();
                    messageType = MemberWebSocketMessageTypeEnum.SELL_INR.getMessageType();
                }
                // 提现
                else if("2".equals(msg.getType()) || Objects.equals(msg.getType(), "4")){
                    antiMemberId = msg.getBuyerMemberId();
                    order = msg.getBuyerOrder();
                    String createBy = msg.getBuyerMemberId();
                    if(ObjectUtils.isNotEmpty(msg.isManual()) && msg.isManual()){
                        createBy = msg.getUpdateBy();
                    }
                    // 获取订单信息
                    CollectionOrder collectionOrder = collectionOrderMapper.selectCollectionOrderForUpdate(msg.getBuyerOrder());
                    MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(collectionOrder.getMerchantOrder());
                    if(!merchantPaymentOrders.getOrderStatus().equals(PaymentOrderStatusEnum.HANDLING.getCode())
                        || !collectionOrder.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode())
                    ){
                        throw new BizException("kyc自动完成 订单状态异常!");
                    }
                    // 添加蚂蚁账户的交易中金额 扣商户余额 改变订单状态 加账变
                    // 根据币种获取账变类型
                    if(msg.getCurrency().equals(CurrenceEnum.INDIA.getCode())){
                        currentChangeEnum = MemberAccountChangeEnum.RECHARGE;
                    }
                    divide = amountChangeUtil.kycAutoCompleteAccountChange(msg.getBuyerMemberId(), msg.getOrderAmount(), ChangeModeEnum.ADD, currentChangeEnum, msg.getCurrency(), msg.getBuyerOrder(), merchantPaymentOrders.getPlatformOrder(), createBy, remark);
                    // 确定回调订单号为卖家订单号
                    // 注册事务同步回调 事务提交成功后才执行以下操作
                    callBackOrderNo = collectionOrder.getMerchantOrder();
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //发送提现成功 异步延时回调通知 后期转正常
                            long millis = 3000L;
                            //发送提现延时回调的MQ消息
                            TaskInfo taskInfo = new TaskInfo(callBackOrderNo, TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                            rabbitMQService.sendTimeoutTask(taskInfo, millis);
                        }
                    });
                    // 更新商户订单状态
                    merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.SUCCESS.getCode());
                    merchantPaymentOrders.setCompletionTime(now);
                    if(ObjectUtils.isNotEmpty(msg.getUpdateBy())){
                        merchantPaymentOrders.setUpdateBy(msg.getUpdateBy());
                        collectionOrder.setUpdateBy(msg.getUpdateBy());
                    }
                    merchantPaymentOrders.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(merchantPaymentOrders.getCreateTime(), now));
                    merchantOrderStatusUpdate = merchantPaymentOrdersMapper.updateById(merchantPaymentOrders);
                    // 更新蚂蚁订单状态
                    collectionOrder.setOrderStatus(OrderStatusEnum.SUCCESS.getCode());
//                    collectionOrder.setKycTradeDetail(msg.getDetail());
                    collectionOrder.setCompletionTime(now);
                    collectionOrder.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(collectionOrder.getCreateTime(), now));
                    orderStatusUpdate = collectionOrderMapper.updateById(collectionOrder);
                    orderEventEnum = OrderEventEnum.MERCHANT_PAYMENT_ORDER_SUCCESS;
                    // 将蚂蚁订单状态置为成功
                    orderStatus = OrderStatusEnum.SUCCESS.getCode();
                    // 将蚂蚁订单类型置为买入订单
                    messageType = MemberWebSocketMessageTypeEnum.BUY_INR.getMessageType();
                }
                // 账变记录
                if(ObjectUtils.isEmpty(divide)){
                    log.error("kyc自动完成 账变记录 处理失败, MQ消息：{}", msg);
                    throw new BizException("kyc自动完成 账变记录失败!");
                }
                // 状态更新判断
                if(merchantOrderStatusUpdate != 1
                    || orderStatusUpdate != 1
                ){
                    log.error("kyc自动完成 订单状态处理 处理失败, MQ消息：{}, 商户订单状态更新:{}， 订单状态更新:{}", msg, merchantOrderStatusUpdate, orderStatusUpdate);
                    throw new BizException("kyc自动完成 订单状态处理失败!");
                }
                if(ObjectUtils.isNotEmpty(msg.getKycId())){
                    // 累计蚂蚁的kyc交易金额和次数 买入次数 卖出次数 买入金额 卖出金额
                    boolean updateTransactionInfoResult = kycPartnersService.updateTransactionInfo(String.valueOf(msg.getKycId()), msg.getOrderAmount(), msg.getType());
                    if(!updateTransactionInfoResult){
                        throw new BizException("kyc自动完成 更新kyc总计信息失败!");
                    }
                }
                // 新增计算佣金事件
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        sendCommissionMsg(msg.getSellerMemberId(), msg.getSellerOrder(), msg.getBuyerMemberId(), msg.getBuyerOrder(), msg.getOrderAmount(), msg.getType());
                    }
                });
                //发送交易成功的通知给前端
                memberMessageSender.send(
                        // 构建用户WebSocket消息体
                        MemberWebSocketSendMessage.buildMemberWebSocketMessage(
                                messageType,
                                antiMemberId,
                                OrderStatusChangeMessage
                                        .builder()
                                        .orderNo(order)
                                        .orderStatus(orderStatus)
                                        .build()
                        )
                );
                // 统计数据
                OrderEventReq req = new OrderEventReq();
                req.setEventId(orderEventEnum.getCode());
                JSONObject params = new JSONObject();
                params.put("commission", divide);
                params.put("amount", msg.getOrderAmount());
                req.setParams(JSONObject.toJSONString(params));
                rabbitMQService.sendStatisticProcess(req);
                return true;
            }
        }catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("kyc自动完成 处理失败, MQ消息: {} , e: {}", msg, e.getMessage());
            return false;
        } finally {
            //释放锁
            if (block && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * 发送佣金结算mq
     * @param sellMemberId sellMemberId
     * @param sellerOrder sellerOrder
     * @param buyerMemberId buyerMemberId
     * @param buyerOrder buyerOrder
     * @param orderAmount orderAmount
     * @param mode mode
     */
    private void sendCommissionMsg(String sellMemberId, String sellerOrder,  String buyerMemberId, String buyerOrder, BigDecimal orderAmount, String mode){
        CommissionAndDividendsMessage msg = new CommissionAndDividendsMessage();
        msg.setAmount(orderAmount);
        // mode为1 蚂蚁卖出 用户充值
        if("1".equals(mode)){
            msg.setUid(Long.parseLong(sellMemberId));
            msg.setOrderNo(sellerOrder);
            msg.setChangeType(MemberAccountChangeEnum.WITHDRAW);
        }
        // mode为2 蚂蚁买入 用户提现
        if("2".equals(mode)){
            msg.setUid(Long.parseLong(buyerMemberId));
            msg.setOrderNo(buyerOrder);
            msg.setChangeType(MemberAccountChangeEnum.RECHARGE);
        }
        rabbitMQService.sendCommissionDividendsQueue(msg);
    }

    /**
     * 发送kyc结算mq
     * @param req req
     */
    public void sendKycCompleteMsg(KycAutoCompleteReq req, String kycId, String detail){
        KycCompleteMessage msg = new KycCompleteMessage();
        msg.setKycId(kycId);
        msg.setSellerOrder(req.getSellerOrder());
        msg.setBuyerOrder(req.getBuyerOrder());
        msg.setBuyerMemberId(req.getBuyerMemberId());
        msg.setSellerMemberId(req.getSellerMemberId());
        msg.setOrderAmount(req.getOrderAmount());
        msg.setCurrency(req.getCurrency());
        msg.setType(req.getType());
        msg.setDetail(detail);
        msg.setUtr(req.getUtr());
        msg.setManual(false);
        rabbitMQService.sendKycAutoCompleteQueue(msg);
    }

    /**
     * 保存匹配上的银行交易记录
     * @param req req
     * @param detail detail
     * @return boolean
     */
    public boolean saveKycTransactionDetail(KycAutoCompleteReq req, String detail) throws Exception {
        int i;
        String key = arProperty.getKycAesKey();
        String encode = AESUtils.encryptFroKyc(detail, key);
        // 充值
        if ("1".equals(req.getType())) {

            PaymentOrder paymentOrder = paymentOrderMapper.getPaymentOrderByPlatformOrder(req.getSellerOrder());
            if(ObjectUtils.isEmpty(paymentOrder)){
                return false;
            }
            paymentOrder.setKycTradeDetail(encode);
            i = paymentOrderMapper.updateById(paymentOrder);
        }else{
            CollectionOrder collectionOrder = collectionOrderMapper.getOrderByOrderNo(req.getBuyerOrder());
            if(ObjectUtils.isEmpty(collectionOrder)){
                return false;
            }
            collectionOrder.setKycTradeDetail(encode);
            i = collectionOrderMapper.updateById(collectionOrder);
        }
        return i == 1;
    }

    /**
     * 保存kyc订单
     * @param req req
     * @param kycBankTransaction kycBankTransaction
     * @param kycPartners kycPartners
     * @return boolean
     */
    private boolean saveKycApprovedOrder(KycAutoCompleteReq req, BankKycTransactionVo kycBankTransaction, KycPartners kycPartners){
        KycApprovedOrder kycApprovedOrder = new KycApprovedOrder();

        // 买入订单号
        kycApprovedOrder.setBuyerOrderId(req.getBuyerOrder());

        // 卖出订单号
        kycApprovedOrder.setSellerOrderId(req.getSellerOrder());

        // 买入会员id
        kycApprovedOrder.setBuyerMemberId(req.getBuyerMemberId());

        // 卖出会员id
        kycApprovedOrder.setSellerMemberId(req.getSellerMemberId());

        // 收款人 UPI
        kycApprovedOrder.setRecipientUpi(kycBankTransaction.getRecipientUPI());

        // 付款人 UPI
        kycApprovedOrder.setPayerUpi(kycBankTransaction.getPayerUPI());

        // 金额
        kycApprovedOrder.setAmount(req.getOrderAmount());

        // utr
        kycApprovedOrder.setUtr(kycBankTransaction.getUTR());

        // 交易状态, 1: 表示成功
        kycApprovedOrder.setTransactionStatus(kycBankTransaction.getOrderStatus());

        // 交易类型, 1: 收入, 2: 支出
        kycApprovedOrder.setTransactionType(kycBankTransaction.getMode());

        // 银行交易时间
        kycApprovedOrder.setBankTransactionTime(kycBankTransaction.getCreateTime());

        // 钱包交易时间
        kycApprovedOrder.setWalletTransactionTime(LocalDateTime.now());

        // kycId
        kycApprovedOrder.setKycId(kycPartners.getId());

        // 银行编码
        kycApprovedOrder.setBankCode(kycPartners.getBankCode());

        // 收款人账户
        kycApprovedOrder.setRecipientAccount(kycPartners.getAccount());

        // 收款人姓名
        kycApprovedOrder.setRecipientName(kycPartners.getName());

        // KYC订单号
        kycApprovedOrder.setOrderId(orderNumberGenerator.generateOrderNo("KYC"));

        return kycApprovedOrderService.save(kycApprovedOrder);
    }

    /**
     * kyc测试环境跳过银行检测
     * @param kycPartners kycPartners
     * @param req req
     * @param matchingOrder matchingOrder
     * @return boolean
     */
    @Transactional
    public boolean testTransaction(KycPartners kycPartners, KycAutoCompleteReq req, String matchingOrder){
        // 测试使用
        BankKycTransactionVo kycBankTransaction = new BankKycTransactionVo();
        kycBankTransaction.setOrderStatus("1");
        kycBankTransaction.setMode(req.getType());
        kycBankTransaction.setCreateTime(LocalDateTime.now());
        // 充值
        if(Objects.equals(req.getType(), "1")){
            List<String> upiIdByMemberId = kycPartnersService.getUpiIdByMemberId(req.getBuyerMemberId());
            if(upiIdByMemberId.isEmpty()){
                return false;
            }
            kycBankTransaction.setRecipientUPI(kycPartners.getUpiId());
            kycBankTransaction.setPayerUPI(upiIdByMemberId.get(0));
        }else{
            kycBankTransaction.setRecipientUPI(req.getWithdrawUpi());
            kycBankTransaction.setPayerUPI(kycPartners.getUpiId());
        }
        // 未入库进行入库操作
        boolean result = saveKycApprovedOrder(req, kycBankTransaction, kycPartners);
        if(result){
            // 关闭自动拉取
            redisUtil.delKycAutoCompleteHash(matchingOrder);
            // 发送结算mq
            sendKycCompleteMsg(req, String.valueOf(kycPartners.getId()), "{\"test\":\"测试写入银行交易记录详情\"}");
            return true;
        }
        return false;
    }


    @Override
    @Transactional
    public KycRestResult paid(PaidParamReq req) {
        try{
            String kycId = "";
            String sellerOrder = "";
            String buyerOrder = "";
            String buyerMemberId = "";
            String sellerMemberId = "";
            BigDecimal orderAmount = BigDecimal.ZERO;
            String currency = "";
            String id = req.getId();
            if ("1".equals(req.getType()) || "3".equals(req.getType())) {
                PaymentOrder paymentOrder;
                MerchantCollectOrders merchantCollectOrders;
                // 蚂蚁卖出
                // 获取订单信息
                if("1".equals(req.getType())){
                    paymentOrder = paymentOrderMapper.selectById(id);
                    merchantCollectOrders = merchantCollectOrdersMapper.selectMerchantCollectOrdersForUpdate(paymentOrder.getMerchantOrder());
                }else{
                    merchantCollectOrders = merchantCollectOrdersMapper.selectById(id);
                    paymentOrder = paymentOrderMapper.selectPaymentByMerchantOrderForUpdate(merchantCollectOrders.getPlatformOrder());
                }
                if(ObjectUtils.isEmpty(paymentOrder)){
                    return KycRestResult.failed("payment order not found");
                }
                if(!paymentOrder.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode())){
                    return KycRestResult.failed("payment order status error");
                }
                if(ObjectUtils.isEmpty(merchantCollectOrders)){
                    return KycRestResult.failed("merchant order not found");
                }
                if(!merchantCollectOrders.getOrderStatus().equals(CollectionOrderStatusEnum.BE_PAID.getCode())){
                    return KycRestResult.failed("merchant collection order status error");
                }
                kycId = paymentOrder.getKycId();
                // 比对实际支付金额和订单金额
                BigDecimal amount = merchantCollectOrders.getOrderAmount();
                BigDecimal actualAmount = ObjectUtils.isEmpty(req.getActualAmount()) ? merchantCollectOrders.getAmount() : req.getActualAmount();
                // 输入金额大于订单实际金额 返回报错
                if(req.getActualAmount().compareTo(merchantCollectOrders.getOrderAmount()) > 0){
                    return KycRestResult.failed("input amount more than order amount");
                }
                if(amount.compareTo(actualAmount) > 0){
                    // 多付款问题暂不处理
                    // 少支付
                    BigDecimal diff = amount.subtract(actualAmount);
                    // 实际支付金额走结算
                    // 订单金额 - 实际金额 退回给蚂蚁 判断委托订单状态(1委托中) 委托订单加上退回金额
                    DelegationOrder delegationOrder = delegationOrderMapper.selectByMemberIdForUpdate(paymentOrder.getMemberId());
                    if(ObjectUtils.isNotEmpty(delegationOrder)){
                        // 用户表委托中金额加上退回订单
                        delegationOrder.setAmount(delegationOrder.getAmount().add(diff));
                        int delegationUpdate = delegationOrderMapper.updateById(delegationOrder);
                        if(delegationUpdate != 1){
                            log.error("已支付： 更新委托订单失败 order:{}, 金额:{}", delegationOrder, diff);
                            throw new Exception("Update Delegation Order Failed");
                        }
                    }
                    // 退回给蚂蚁 减交易中金额 加余额
                    MemberInfo memberInfo = memberInfoService.getMemberInfoById(paymentOrder.getMemberId());
                    if(ObjectUtils.isEmpty(memberInfo)){
                        log.error("已支付： 多余金额退回操作 获取对应会员失败 memberId:{}, memberInfo:{}", paymentOrder.getMemberId(), memberInfo);
                        throw new Exception("Get Member Info Failed");
                    }
                    if(memberInfo.getFrozenAmount().compareTo(diff) < 0){
                        log.error("已支付： 多余金额退回操作 交易中金额小于差额 memberId:{}, memberInfo:{}", paymentOrder.getMemberId(), memberInfo);
                        throw new Exception("Frozen Amount Small than diff");
                    }
                    LambdaUpdateWrapper<MemberInfo> memberInfoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    memberInfoLambdaUpdateWrapper.eq(MemberInfo::getId, paymentOrder.getMemberId());
                    memberInfoLambdaUpdateWrapper.set(MemberInfo::getBalance, memberInfo.getBalance().add(diff))
                            .set(MemberInfo::getFrozenAmount, memberInfo.getFrozenAmount().subtract(diff));
                    boolean memberFrozenUpdate = memberInfoService.update(null, memberInfoLambdaUpdateWrapper);
                    if(!memberFrozenUpdate){
                        log.error("已支付： 多余金额退回操作 更新会员失败 order:{}, 金额:{}", delegationOrder, diff);
                        throw new Exception("Update Member Info Failed");
                    }
                    // 退回金额
//                    amountChangeUtil.insertMemberChangeAmountRecord(paymentOrder.getMemberId(), diff, ChangeModeEnum.ADD, paymentOrder.getCurrency(), paymentOrder.getPlatformOrder(), MemberAccountChangeEnum.AMOUNT_ERROR, "", "");
                }
                // 实际金额和订单金额不一致 需要更新订单实际金额
                if(ObjectUtils.isNotEmpty(req.getActualAmount()) && req.getActualAmount().compareTo(merchantCollectOrders.getAmount()) != 0){
                    paymentOrder.setActualAmount(req.getActualAmount());
                    merchantCollectOrders.setAmount(req.getActualAmount());
                    BigDecimal exchangeRate = null;
                    if(ObjectUtils.isNotEmpty(paymentOrder.getExchangeRates())){
                        exchangeRate = paymentOrder.getExchangeRates();
                    }
                    if(ObjectUtils.isNotEmpty(merchantCollectOrders.getExchangeRates())){
                        exchangeRate = merchantCollectOrders.getExchangeRates();
                    }
                    // 订单中未设置汇率 使用当前汇率 并且更新订单表
                    if(ObjectUtils.isEmpty(exchangeRate)){
                        exchangeRate = systemCurrencyService.getCurrencyExchangeRate(merchantCollectOrders.getCurrency());
                        paymentOrder.setExchangeRates(exchangeRate);
                        merchantCollectOrders.setExchangeRates(exchangeRate);
                    }
                    BigDecimal iTokenNumber = req.getActualAmount().multiply(paymentOrder.getExchangeRates()).setScale(2, RoundingMode.DOWN);
                    paymentOrder.setItokenNumber(iTokenNumber);
                    merchantCollectOrders.setItokenNumber(iTokenNumber);
                    int i = paymentOrderMapper.updateById(paymentOrder);
                    if(i != 1){
                        log.error("已支付： 更新 paymentOrder 失败 order:{}, ITokenNumber:{}， 汇率：{}", paymentOrder, iTokenNumber, exchangeRate);
                        throw new Exception("Update Payment Order Failed");
                    }
                    int i1 = merchantCollectOrdersMapper.updateById(merchantCollectOrders);
                    if(i1 != 1){
                        log.error("已支付： 更新 merchantOrder 失败 order:{}, ITokenNumber:{}， 汇率：{}", paymentOrder, iTokenNumber, exchangeRate);
                        throw new Exception("Update Merchant Order Failed");
                    }
                }
                sellerOrder = paymentOrder.getPlatformOrder();
                sellerMemberId = paymentOrder.getMemberId();
                buyerOrder = paymentOrder.getMerchantOrder();
                buyerMemberId = merchantCollectOrders.getMemberId();
                orderAmount = actualAmount;
                currency = paymentOrder.getCurrency();
            }
            else if ("2".equals(req.getType()) || "4".equals(req.getType())) {
                CollectionOrder collectionOrder;
                MerchantPaymentOrders merchantPaymentOrders;
                // 蚂蚁买入
                if("2".equals(req.getType())){
                    collectionOrder = collectionOrderMapper.selectById(id);
                    merchantPaymentOrders = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(collectionOrder.getMerchantOrder());
                }else{
                    merchantPaymentOrders = merchantPaymentOrdersMapper.selectById(id);
                    collectionOrder = collectionOrderMapper.selectCollectionOrderByMerchantOrderForUpdate(merchantPaymentOrders.getPlatformOrder());
                }
                if(ObjectUtils.isEmpty(collectionOrder)){
                    return KycRestResult.failed("collection order not found");
                }
                if(!collectionOrder.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode())){
                    return KycRestResult.failed("collection order status error");
                }
                if(ObjectUtils.isEmpty(merchantPaymentOrders)){
                    return KycRestResult.failed("merchant order not found");
                }
                if(!merchantPaymentOrders.getOrderStatus().equals(PaymentOrderStatusEnum.HANDLING.getCode())){
                    return KycRestResult.failed("merchant payment order status error");
                }
                kycId = collectionOrder.getKycId();
                sellerOrder = collectionOrder.getMerchantOrder();
                sellerMemberId = merchantPaymentOrders.getMemberId();
                buyerOrder = collectionOrder.getPlatformOrder();
                buyerMemberId = collectionOrder.getMemberId();
                orderAmount = collectionOrder.getActualAmount();
                currency = collectionOrder.getCurrency();
            }
            // 发送结算mq
            KycCompleteMessage msg = new KycCompleteMessage();
            msg.setKycId(kycId);
            msg.setManual(true);
            msg.setSellerOrder(sellerOrder);
            msg.setBuyerOrder(buyerOrder);
            msg.setBuyerMemberId(buyerMemberId);
            msg.setSellerMemberId(sellerMemberId);
            msg.setOrderAmount(orderAmount);
            msg.setCurrency(currency);
            msg.setType(req.getType());
            msg.setUpdateBy(req.getUpdateBy());
            if(ObjectUtils.isNotEmpty(req.getRemark())){
                msg.setRemark(req.getRemark());
            }
            rabbitMQService.sendKycAutoCompleteQueue(msg);
            return KycRestResult.ok();
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("已支付操作失败： e {}", e.getMessage());
            return KycRestResult.failed(e.getMessage());
        }
    }

    @Transactional
    @Override
    public KycRestResult unPaid(PaidParamReq req) {
        try{
            // 订单金额退回给蚂蚁 判断是否还有委托订单 有的话需要加上委托订单的金额
            String id = req.getId();
            String sellerOrder = "";
            String buyerOrder  = "";
            String order = "";
            String messageType = "";
            String memberId = "";

            LocalDateTime now = LocalDateTime.now();
            if ("1".equals(req.getType()) || "3".equals(req.getType())) {
                PaymentOrder paymentOrder;
                MerchantCollectOrders merchantCollectOrders;
                if("1".equals(req.getType())){
                    paymentOrder = paymentOrderMapper.selectById(id);
                    merchantCollectOrders = merchantCollectOrdersMapper.selectMerchantCollectOrdersForUpdate(paymentOrder.getMerchantOrder());
                }else{
                    merchantCollectOrders = merchantCollectOrdersMapper.selectById(id);
                    paymentOrder = paymentOrderMapper.selectPaymentByMerchantOrderForUpdate(merchantCollectOrders.getPlatformOrder());
                }
                if(ObjectUtils.isEmpty(paymentOrder)){
                    return KycRestResult.failed("payment order not found");
                }
                if(!paymentOrder.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode())){
                    return KycRestResult.failed("payment order status error");
                }
                if(ObjectUtils.isEmpty(merchantCollectOrders)){
                    return KycRestResult.failed("merchant order not found");
                }
                if(!merchantCollectOrders.getOrderStatus().equals(CollectionOrderStatusEnum.BE_PAID.getCode())){
                    return KycRestResult.failed("merchant collection order status error");
                }
                // 退回订单金额给蚂蚁
                DelegationOrder delegationOrder = delegationOrderMapper.selectByMemberIdForUpdate(paymentOrder.getMemberId());
                if(ObjectUtils.isNotEmpty(delegationOrder)){
                    // 用户表委托中金额加上退回订单
                    delegationOrder.setAmount(delegationOrder.getAmount().add(paymentOrder.getActualAmount()));
                    int delegationUpdate = delegationOrderMapper.updateById(delegationOrder);
                    if(delegationUpdate != 1){
                        log.error("未支付： 更新委托订单失败 order:{}", delegationOrder);
                        return KycRestResult.failed("Update Delegation Order Failed");
                    }
                }
                MemberInfo memberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.parseLong(paymentOrder.getMemberId()));
                if(ObjectUtils.isNotEmpty(memberInfo)){
                    BigDecimal frozenAmount = memberInfo.getFrozenAmount();
                    BigDecimal balance = memberInfo.getBalance();
                    BigDecimal actualAmount = paymentOrder.getActualAmount();
                    if(frozenAmount.compareTo(actualAmount) < 0){
                        return KycRestResult.failed("frozen amount error");
                    }
                    BigDecimal afterFrozenAmount = frozenAmount.subtract(actualAmount);
                    memberInfo.setFrozenAmount(afterFrozenAmount);
                    memberInfo.setBalance(balance.add(actualAmount));
                    int i = memberInfoMapper.updateById(memberInfo);
                    if(i != 1){
                        return KycRestResult.failed("update frozen amount error");
                    }
                }
                paymentOrder.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
                paymentOrder.setCancelTime(now);
                if(ObjectUtils.isNotEmpty(req.getRemark())){
                    paymentOrder.setRemark(req.getRemark());
                }
                int i = paymentOrderMapper.updateById(paymentOrder);
                merchantCollectOrders.setOrderStatus(CollectionOrderStatusEnum.WAS_CANCELED.getCode());
                int i1 = merchantCollectOrdersMapper.updateById(merchantCollectOrders);
                if(i != 1 || i1 != 1){
                    return KycRestResult.failed("update order status error");
                }
                memberId = paymentOrder.getMemberId();
                order = paymentOrder.getPlatformOrder();
                sellerOrder = paymentOrder.getPlatformOrder();
                buyerOrder = merchantCollectOrders.getMerchantOrder();
                messageType = MemberWebSocketMessageTypeEnum.SELL_INR.getMessageType();
                // 注册事务同步回调 事务提交成功后才执行以下操作
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //异步通知
                        TaskInfo taskInfo = new TaskInfo(merchantCollectOrders.getPlatformOrder(), TaskTypeEnum.DEPOSIT_NOTIFICATION.getCode(), System.currentTimeMillis());
                        rabbitMQService.sendRechargeSuccessCallbackNotification(taskInfo);
                    }
                });
            }
            else if ("2".equals(req.getType()) || "4".equals(req.getType())) {
                CollectionOrder collectionOrder;
                MerchantPaymentOrders merchantPaymentOrders;
                if("2".equals(req.getType())){
                    collectionOrder = collectionOrderMapper.selectById(id);
                    merchantPaymentOrders = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(collectionOrder.getMerchantOrder());
                }else{
                    merchantPaymentOrders = merchantPaymentOrdersMapper.selectById(id);
                    collectionOrder = collectionOrderMapper.selectCollectionOrderByMerchantOrderForUpdate(merchantPaymentOrders.getPlatformOrder());
                }
                if(ObjectUtils.isEmpty(collectionOrder)){
                    return KycRestResult.failed("collection order not found");
                }
                if(!collectionOrder.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode())){
                    return KycRestResult.failed("collection order status error");
                }
                if(ObjectUtils.isEmpty(merchantPaymentOrders)){
                    return KycRestResult.failed("merchant order not found");
                }
                if(!merchantPaymentOrders.getOrderStatus().equals(PaymentOrderStatusEnum.HANDLING.getCode())){
                    return KycRestResult.failed("merchant payment order status error");
                }
                collectionOrder.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
                collectionOrder.setCancelTime(now);
                if(ObjectUtils.isNotEmpty(req.getRemark())){
                    collectionOrder.setRemark(req.getRemark());
                    merchantPaymentOrders.setRemark(req.getRemark());
                }
                int i = collectionOrderMapper.updateById(collectionOrder);
                // 释放代付订单锁定金额
                MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(merchantPaymentOrders.getMerchantCode());
                LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode());
                BigDecimal allAmount = merchantPaymentOrders.getAmount().add(merchantPaymentOrders.getCost()).add(merchantPaymentOrders.getFixedFee());
                BigDecimal balance = merchantInfo.getBalance();
                if (merchantInfo.getPendingBalance().compareTo(allAmount) < 0) {
                    return KycRestResult.failed("transaction balance not enough");
                }
                lambdaUpdateWrapperMerchantInfo.set(MerchantInfo::getBalance, balance.add(allAmount))
                        .set(MerchantInfo::getPendingBalance, merchantInfo.getPendingBalance().subtract(allAmount));
                int i2 = merchantInfoMapper.update(null, lambdaUpdateWrapperMerchantInfo);
                if(i2 != 1){
                    return KycRestResult.failed("update merchant trade amount status error");
                }
                // 更新订单状态
                merchantPaymentOrders.setOrderStatus(CollectionOrderStatusEnum.WAS_CANCELED.getCode());
                int i1 = merchantPaymentOrdersMapper.updateById(merchantPaymentOrders);
                if(i != 1 || i1 != 1){
                    return KycRestResult.failed("update order status error");
                }
                order = collectionOrder.getPlatformOrder();
                memberId = collectionOrder.getMemberId();
                sellerOrder = merchantPaymentOrders.getPlatformOrder();
                buyerOrder = collectionOrder.getPlatformOrder();
                messageType = MemberWebSocketMessageTypeEnum.BUY_INR.getMessageType();
                // 注册事务同步回调 事务提交成功后才执行以下操作
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //发送提现成功 异步延时回调通知 后期转正常
                        long millis = 3000L;
                        //发送提现延时回调的MQ消息
                        TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                        rabbitMQService.sendTimeoutTask(taskInfo, millis);
                    }
                });
            }
            //发送交易取消的通知给前端
            memberMessageSender.send(
                    // 构建用户WebSocket消息体
                    MemberWebSocketSendMessage.buildMemberWebSocketMessage(
                            messageType,
                            memberId,
                            OrderStatusChangeMessage
                                    .builder()
                                    .orderNo(order)
                                    .orderStatus(OrderStatusEnum.WAS_CANCELED.getCode())
                                    .build()
                    )
            );
            // 删除
            stopPullTransaction(sellerOrder, buyerOrder);
            // 取消订单
            return KycRestResult.ok();
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("未支付操作失败： e {}", e.getMessage());
        }
        return KycRestResult.failed("un paid failed");
    }

    @Transactional
    @Override
    public KycRestResult completePayment(KycAutoCompleteReq req) {
        String type = req.getType();
        CollectionOrder collectionOrder = collectionOrderMapper.getOrderByOrderNo(req.getBuyerOrder());
        if(ObjectUtils.isEmpty(collectionOrder)){
            return KycRestResult.failed("collection order not found");
        }
        String kycTradeDetail = collectionOrder.getKycTradeDetail();
        // 空校验
        if(ObjectUtils.isEmpty(kycTradeDetail)){
            // 无数据时 需要加入队列重新调用此方法
            sendCompleteMsg(req);
            return KycRestResult.failed("kyc trade detail not found");
        }
        try{
            String key = arProperty.getKycAesKey();
            String decode = AESUtils.decryptForKyc(kycTradeDetail, key);
            BankKycTransactionVo bankKycTransactionVo = JSONObject.parseObject(decode, BankKycTransactionVo.class);
            BigDecimal amount = bankKycTransactionVo.getAmount();
            BigDecimal orderAmount = req.getOrderAmount();
            if(!(amount.compareTo(orderAmount) == 0)){
                return KycRestResult.failed("order amount not match");
            }
            if(!bankKycTransactionVo.getMode().equals(type)){
                return KycRestResult.failed("order type not match");
            }
            if (ObjectUtils.isEmpty(req.getWithdrawUpi()) || !Objects.equals(bankKycTransactionVo.getRecipientUPI(), req.getWithdrawUpi())) {
                return KycRestResult.failed("recipient info not match");
            }
            KycCompleteMessage msg = new KycCompleteMessage();
            msg.setKycId(req.getKycId());
            msg.setManual(false);
            msg.setOrderAmount(req.getOrderAmount());
            msg.setCurrency(req.getCurrency());
            msg.setType(type);
            msg.setSellerOrder(req.getSellerOrder());
            msg.setBuyerOrder(req.getBuyerOrder());
            msg.setSellerMemberId(req.getSellerMemberId());
            msg.setBuyerMemberId(req.getBuyerMemberId());
            boolean result = transactionSuccessHandler(msg);
            if(result){
                log.info("kyc完成订单成功：msg :{} ", msg);
                return KycRestResult.ok();
            }
        } catch (Exception e) {
            return KycRestResult.failed("decrypt error");
        }
        return KycRestResult.failed("complete payment error");
    }


    /**
     * 发送kyc结算mq
     * @param req req
     */
    public void sendCompleteMsg(KycAutoCompleteReq req){
        // 改成延时队列处理
        rabbitMQService.sendKycCompleteOrderProcess(JSONObject.toJSONString(req));
    }
}
