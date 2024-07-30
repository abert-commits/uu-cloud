package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.constant.SecurityConstants;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.core.utils.AssertUtil;
import org.uu.common.core.utils.CommonUtils;
import org.uu.common.core.utils.UserAgentUtil;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.bo.MemberInfoBO;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;
import org.uu.common.pay.req.MemberInfoReq;
import org.uu.common.pay.vo.request.MemberAccountChangeDetailRequestVO;
import org.uu.common.pay.vo.request.MemberAccountChangeRequestVO;
import org.uu.common.pay.vo.response.MemberAccountChangeDetailResponseVO;
import org.uu.common.pay.vo.response.MemberAccountChangeResponseVO;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.common.web.exception.BizException;
import org.uu.common.web.utils.ConvertDateTimeUtil;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.async.RegisterRecordAsync;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.constants.WalletConstant;
import org.uu.wallet.dto.GenerateTokenForWallertDTO;
import org.uu.wallet.email.EmailService;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.InviteLinkMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.MemberManualLogMapper;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.mapper.*;
import org.uu.wallet.oss.OssService;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.*;
import org.uu.wallet.service.*;
import org.uu.wallet.sms.SmsService;
import org.uu.wallet.thirdParty.IdAnalyzerClient;
import org.uu.wallet.thirdParty.IdAnalyzerStatus;
import org.uu.wallet.thirdParty.MessageClient;
import org.uu.wallet.thirdParty.MessageStatus;
import org.uu.wallet.util.*;
import org.uu.wallet.vo.*;
import org.uu.wallet.webSocket.NotifyLoginOutWebSocketService;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.uu.common.core.result.ResultCode.INVALID_NEW_USER_GUID_TYPE;
import static org.uu.common.core.result.ResultCode.USER_NOT_EXIST;
import static org.uu.wallet.Enum.RewardTaskTypeEnum.STARTER_QUESTS_BUY;
import static org.uu.wallet.Enum.RewardTaskTypeEnum.STARTER_QUESTS_SELL;

/**
 * @author
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MemberInfoServiceImpl extends ServiceImpl<MemberInfoMapper, MemberInfo> implements IMemberInfoService {

    @Autowired
    private ICollectionOrderService collectionOrderService;
    private final RedissonUtil redissonUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    private final MemberManualLogMapper mapper;
    private final WalletMapStruct walletMapStruct;
    private final ICreditScoreLogsService iCreditScoreLogsService;
    private final ICreditScoreConfigService creditScoreConfigService;
    private final RabbitMQService rabbitMQService;
    private final ValidateCodeService validateCodeService;
    private final InviteLinkMapper inviteLinkMapper;
    private final AntRelationsService antRelationsService;
    private final DividendConfigService dividendConfigService;
    private final KycPartnersMapper kycPartnersMapper;
    private final FrontPageConfigService frontPageConfigService;
    @Autowired
    private TradeConfigHelperUtil tradeConfigHelperUtil;

    @Autowired
    private OssService ossService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ArProperty arProperty;

    @Autowired
    private EmailService emailService;


    @Autowired
    private IMemberNotificationService memberNotificationService;


    @Autowired
    private IMemberGroupService memberGroupService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ITradeConfigService tradeConfigService;

    @Autowired
    private MessageClient messageClient;


    @Autowired
    private AmountChangeUtil amountChangeUtil;

    @Autowired
    private OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private IUserVerificationCodesService userVerificationCodesService;

    @Autowired
    private IdAnalyzerClient idAnalyzerClient;

    @Autowired
    private SmsService smsService;

    @Autowired
    private IMerchantInfoService merchantInfoService;

    @Autowired
    private IControlSwitchService controlSwitchService;

    @Autowired
    private IMemberTaskStatusService memberTaskStatusService;

    @Autowired
    private ITaskManagerService taskManagerService;
    @Autowired
    private IMemberBlackService memberBlackService;
    @Autowired
    private IMemberLevelWelfareConfigService memberLevelWelfareConfigService;

    @Autowired
    private ICashBackOrderService cashBackOrderService;
    @Autowired
    private MerchantInfoMapper merchantInfoMapper;
    @Autowired
    private IMemberAccountChangeService memberAccountChangeService;
    @Autowired
    private PaymentOrderMapper paymentOrderMapper;
    @Autowired
    private IPaymentOrderService paymentOrderService;
    @Autowired
    private IUsdtBuyOrderService usdtBuyOrderService;

    @Resource
    private ISystemCurrencyService systemCurrencyService;

    @Override
    public RestResult<Void> updatePassword(UpdateLoginPasswordReq requestVO) {
        switch (requestVO.getUpdateType()) {
            case 1:
                return this.resetLoginPassword(requestVO.getPhoneNumber(), requestVO.getVerificationCode(), requestVO.getNewPassword());
            case 2:
                return updateLoginPassword(requestVO.getPhoneNumber(), requestVO.getVerificationCode(), requestVO.getOldPassword(), requestVO.getNewPassword());
            default:
                return RestResult.failed("Unsupported update type");
        }
    }

    private RestResult<Void> updateLoginPassword(String phoneNumber, String verificationCode, String oldPassword, String newPassword) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }
        MemberInfo memberInfo = this.getById(currentUserId);
        if (Objects.isNull(memberInfo)) {
            return RestResult.failed(USER_NOT_EXIST);
        }

        // 校验是否传递原密码
        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(oldPassword.trim())) {
            return RestResult.failed("Please specify the oldPassword of current update");
        }

        // 校验验证码
        if (!this.validateCodeService.validate(phoneNumber, verificationCode)) {
            return RestResult.failed(ResultCode.VERIFICATION_CODE_ERROR);
        }

        // 校验原密码
        if (!this.passwordEncoder.matches(oldPassword, memberInfo.getPassword())) {
            return RestResult.failed(ResultCode.PASSWORD_VERIFICATION_FAILED);
        }

        // 校验前后密码是否一致
        if (oldPassword.equals(newPassword)) {
            return RestResult.failed("The oldPassword and newPassword can not same");
        }

        boolean update = this.lambdaUpdate()
                .set(MemberInfo::getPassword, passwordEncoder.encode(newPassword))
                .set(MemberInfo::getUpdateTime, LocalDateTime.now())
                .eq(MemberInfo::getMemberId, memberInfo.getMemberId())
                .update();
        return update ? RestResult.ok() : RestResult.failed("Update login password failed");
    }

    private RestResult<Void> resetLoginPassword(String phoneNumber, String verificationCode, String password) {
        MemberAuthDTO memberInfo = this.getByUsername(phoneNumber);
        if (Objects.isNull(memberInfo)) {
            return RestResult.failed(USER_NOT_EXIST);
        }
        // 校验验证码
        if (!this.validateCodeService.validate(phoneNumber, verificationCode)) {
            return RestResult.failed(ResultCode.VERIFICATION_CODE_ERROR);
        }
        boolean update = this.lambdaUpdate()
                .set(MemberInfo::getPassword, passwordEncoder.encode(password))
                .set(MemberInfo::getUpdateTime, LocalDateTime.now())
                .eq(MemberInfo::getMemberId, memberInfo.getMemberId())
                .update();
        return update ? RestResult.ok() : RestResult.failed("Reset login password failed");
    }

    /**
     * 获取当前会员帐变记录信息
     * 买入: collection_order 表
     * 卖出: payment_order 表
     *
     * @return {@link RestResult}<{@link MemberAccountChangeVo}>
     */
    @Override
    public RestResult<PageReturn<MemberAccountChangeResponseVO>> getMemberAccountChangeInfo(MemberAccountChangeRequestVO requestVO) {
        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("获取帐变信息失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        LambdaQueryChainWrapper<MemberAccountChange> queryChainWrapper = this.memberAccountChangeService.lambdaQuery();
        Page<MemberAccountChange> pageCollectionOrder = new Page<>();
        pageCollectionOrder.setCurrent(requestVO.getPageNo());
        pageCollectionOrder.setSize(requestVO.getPageSize());

        ZoneId zoneId = ZoneId.systemDefault();
        String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(GlobalConstants.HEADER_TIME_ZONE);
        if (StringUtils.isNotEmpty(timeZone)) {
            zoneId = ZoneId.of(timeZone);
        }
        if (Objects.nonNull(requestVO.getChangeDate())) {
            queryChainWrapper.ge(MemberAccountChange::getCreateTime, ConvertDateTimeUtil.getStartDateTimeByLocalDate(requestVO.getChangeDate(), zoneId))
                    .le(MemberAccountChange::getCreateTime, ConvertDateTimeUtil.getEndDateTimeByLocalDate(requestVO.getChangeDate(), zoneId));
        }
        if (Objects.nonNull(requestVO.getChangeType()) && requestVO.getChangeType() > 0) {
            queryChainWrapper.eq(MemberAccountChange::getChangeType, requestVO.getChangeType());
        }

        queryChainWrapper.eq(MemberAccountChange::getMid, memberInfo.getId())
                .orderByDesc(MemberAccountChange::getCreateTime);
        Page<MemberAccountChange> accountChangePage = queryChainWrapper.page(pageCollectionOrder);
        List<MemberAccountChange> accountChangeList = accountChangePage.getRecords();
        List<MemberAccountChangeResponseVO> resultList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountChangeList)) {
            resultList = accountChangeList.stream()
                    .filter(Objects::nonNull)
                    .map(item -> MemberAccountChangeResponseVO.builder()
                            .changeMode(item.getChangeType())
                            .amountChange(item.getAmountChange())
                            .orderNo(item.getOrderNo())
                            .createTime(item.getCreateTime())
                            .build())
                    .collect(Collectors.toList());
        }
        return RestResult.ok(PageUtils.flush(pageCollectionOrder, resultList));
    }

    @Override
    public RestResult<MemberAccountChangeDetailResponseVO> getMemberAccountChangeDetail(MemberAccountChangeDetailRequestVO requestVO) {
        MemberAccountChangeEnum memberAccountChangeEnum = MemberAccountChangeEnum.buildMemberAccountChangeEnumByCode(requestVO.getChangeType().toString());
        if (Objects.isNull(memberAccountChangeEnum)) {
            return RestResult.failed("Unknown account change type");
        }
        switch (memberAccountChangeEnum) {
            case RECHARGE:
                return this.collectionOrderService.selectByOrderNo(requestVO.getOrderNo());
            case WITHDRAW:
                PaymentOrder paymentOrder = this.paymentOrderMapper.getPaymentOrderByPlatformOrder(requestVO.getOrderNo());
                if (Objects.isNull(paymentOrder)) {
                    return RestResult.failed("Order does not exist");
                }
                // 获取KYC信息
                String kycId = paymentOrder.getKycId();
                KycPartners kycPartners = Objects.nonNull(kycId) && org.apache.commons.lang3.StringUtils.isNotEmpty(kycId) ? this.kycPartnersMapper.selectById(kycId) : null;
                return RestResult.ok(
                        MemberAccountChangeDetailResponseVO.builder()
                                .amount(paymentOrder.getAmount())
                                .changeType(MemberAccountChangeEnum.WITHDRAW.getCode())
                                .orderNo(requestVO.getOrderNo())
                                .utr(paymentOrder.getUtr())
                                .upiId(paymentOrder.getUpiId())
                                .upiName(paymentOrder.getUpiName())
                                .remark(paymentOrder.getRemark())
                                .completionTime(paymentOrder.getCompletionTime())
                                .kycAccount(Objects.isNull(kycPartners) ? null : kycPartners.getAccount())
                                .kycBankName(Objects.isNull(kycPartners) ? null : kycPartners.getBankName())
                                .build()
                );
            case UPPER_DIVISION:
            case LOWER_DIVISION:
            case BUY_BONUS:
            case SELL_BONUS:
            case BUY_COMMISSION:
            case SELL_COMMISSION:
            case PLATFORM_DIVIDENDS:
                return this.memberAccountChangeService.selectByOrderNo(requestVO.getOrderNo());
            case USDT_RECHARGE:
                UsdtBuyOrder usdtBuyOrder = this.usdtBuyOrderService.lambdaQuery()
                        .eq(UsdtBuyOrder::getPlatformOrder, requestVO.getOrderNo())
                        .one();
                if (Objects.isNull(usdtBuyOrder)) {
                    return RestResult.failed("Order does not exist");
                }
                return RestResult.ok(
                        MemberAccountChangeDetailResponseVO.builder()
                                .amount(usdtBuyOrder.getArbActualNum())
                                .changeType(MemberAccountChangeEnum.USDT_RECHARGE.getCode())
                                .orderNo(requestVO.getOrderNo())
                                .remark(usdtBuyOrder.getRemark())
                                .usdtAddress(usdtBuyOrder.getUsdtAddr())
                                .completionTime(usdtBuyOrder.getPaymentTime())
                                .build()
                );
            default:
                return RestResult.failed("Unsupported type");
        }
    }

    @Override
    public List<MemberInfoBO> queryMemberInfoByIds(List<Long> uidList) {
        List<MemberInfoBO> memberInfoBOList = Collections.emptyList();
        if (CollectionUtils.isEmpty(uidList)) {
            memberInfoBOList = this.baseMapper.selectMemberInfoByIds(uidList);
        }
        return memberInfoBOList;
    }

    /**
     * 获取交易记录
     * 从买入表collection_order 卖出表payment_order usdt充值表中usdt_buy_order 查询出交易记录
     *
     * @param req
     * @return
     */
    @Override
    public RestResult<PageReturn<ViewTransactionHistoryVo>> viewTransactionHistory(ViewTransactionHistoryReq req) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }

        if (req.getTransactionType().equals(1) && Objects.isNull(req.getSubTransactionType())) {
            return RestResult.failed("Please specify the sub transaction type while the transaction type equals 1");
        }

        ZoneId zoneId = ZoneId.systemDefault();
        String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(GlobalConstants.HEADER_TIME_ZONE);
        if (StringUtils.isNotEmpty(timeZone)) {
            zoneId = ZoneId.of(timeZone);
        }
        Integer transactionType = req.getTransactionType();
        switch (transactionType) {
            // 买入
            case 1:
                Integer subTransactionType = req.getSubTransactionType();
                switch (subTransactionType) {
                    // 买入订单记录
                    case 11:
                        Page<CollectionOrder> collectionOrderPage = new Page<>();
                        collectionOrderPage.setCurrent(req.getPageNo());
                        collectionOrderPage.setSize(req.getPageSize());
                        LambdaQueryChainWrapper<CollectionOrder> chainWrapper = this.collectionOrderService.lambdaQuery();
                        chainWrapper.eq(CollectionOrder::getMemberId, currentUserId)
                                .orderByDesc(CollectionOrder::getCreateTime);
                        if (Objects.nonNull(OrderStatusEnum.buildOrderStatusEnumByCode(req.getTransactionStatus().toString()))) {
                            chainWrapper.eq(CollectionOrder::getOrderStatus, req.getTransactionStatus());
                        }
                        if (Objects.nonNull(req.getDate())) {
                            chainWrapper.ge(CollectionOrder::getCreateTime, ConvertDateTimeUtil.getStartDateTimeByLocalDate(req.getDate(), zoneId))
                                    .lt(CollectionOrder::getCreateTime, ConvertDateTimeUtil.getEndDateTimeByLocalDate(req.getDate(), zoneId));
                        }
                        Page<CollectionOrder> collectionOrderPageResult = chainWrapper.page(collectionOrderPage);
                        List<CollectionOrder> collectionOrderList = collectionOrderPageResult.getRecords();
                        List<ViewTransactionHistoryVo> responseVOList = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(collectionOrderList)) {
                            responseVOList = collectionOrderList.stream()
                                    .filter(Objects::nonNull)
                                    .map(item -> ViewTransactionHistoryVo.builder()
                                            .transactionStatus(item.getOrderStatus())
                                            .createTime(item.getCreateTime())
                                            .inrOrUsdtAmount(item.getActualAmount())
                                            .arbAmount(item.getActualAmount().multiply(item.getExchangeRates()).setScale(2, RoundingMode.DOWN))
                                            .platformOrder(item.getPlatformOrder())
                                            .transactionType(1)
                                            .subTransactionType(11)
                                            .build())
                                    .collect(Collectors.toList());
                        }
                        return RestResult.ok(PageUtils.flush(collectionOrderPage, responseVOList));
                    // USDT充值订单记录
                    case 12:
                        Page<UsdtBuyOrder> usdtBuyOrderPage = new Page<>();
                        usdtBuyOrderPage.setCurrent(req.getPageNo());
                        usdtBuyOrderPage.setSize(req.getPageSize());
                        LambdaQueryChainWrapper<UsdtBuyOrder> usdtBuyOrderQueryWrapper = this.usdtBuyOrderService.lambdaQuery();
                        usdtBuyOrderQueryWrapper.eq(UsdtBuyOrder::getMemberId, currentUserId)
                                .orderByDesc(UsdtBuyOrder::getCreateTime);
                        if (Objects.nonNull(OrderStatusEnum.buildOrderStatusEnumByCode(req.getTransactionStatus().toString()))) {
                            usdtBuyOrderQueryWrapper.eq(UsdtBuyOrder::getStatus, req.getTransactionStatus());
                        }
                        if (Objects.nonNull(req.getDate())) {
                            usdtBuyOrderQueryWrapper.ge(UsdtBuyOrder::getCreateTime, ConvertDateTimeUtil.getStartDateTimeByLocalDate(req.getDate(), zoneId))
                                    .lt(UsdtBuyOrder::getCreateTime, ConvertDateTimeUtil.getEndDateTimeByLocalDate(req.getDate(), zoneId));
                        }
                        Page<UsdtBuyOrder> usdtBuyOrderPageResult = usdtBuyOrderQueryWrapper.page(usdtBuyOrderPage);
                        List<UsdtBuyOrder> usdtBuyOrderList = usdtBuyOrderPageResult.getRecords();
                        List<ViewTransactionHistoryVo> responseVOOfUSDTOrderList = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(usdtBuyOrderList)) {
                            responseVOOfUSDTOrderList = usdtBuyOrderList.stream()
                                    .filter(Objects::nonNull)
                                    .map(item -> ViewTransactionHistoryVo.builder()
                                            .transactionStatus(item.getStatus())
                                            .createTime(item.getCreateTime())
                                            .inrOrUsdtAmount(item.getUsdtNum())
                                            .arbAmount(item.getArbActualNum())
                                            .platformOrder(item.getPlatformOrder())
                                            .transactionType(1)
                                            .subTransactionType(12)
                                            .build())
                                    .collect(Collectors.toList());
                        }
                        return RestResult.ok(PageUtils.flush(usdtBuyOrderPage, responseVOOfUSDTOrderList));
                    default:
                        return RestResult.failed("The type of sub transaction type is not supported");
                }
                // 卖出
            case 2:
                Page<PaymentOrder> paymentOrderPage = new Page<>();
                paymentOrderPage.setCurrent(req.getPageNo());
                paymentOrderPage.setSize(req.getPageSize());
                LambdaQueryChainWrapper<PaymentOrder> paymentOrderQueryWrapper = this.paymentOrderService.lambdaQuery();
                paymentOrderQueryWrapper.eq(PaymentOrder::getMemberId, currentUserId)
                        .orderByDesc(PaymentOrder::getCreateTime);
                if (Objects.nonNull(OrderStatusEnum.buildOrderStatusEnumByCode(req.getTransactionStatus().toString()))) {
                    paymentOrderQueryWrapper.eq(PaymentOrder::getOrderStatus, req.getTransactionStatus());
                }
                if (Objects.nonNull(req.getDate())) {
                    paymentOrderQueryWrapper.ge(PaymentOrder::getCreateTime, ConvertDateTimeUtil.getStartDateTimeByLocalDate(req.getDate(), zoneId))
                            .lt(PaymentOrder::getCreateTime, ConvertDateTimeUtil.getEndDateTimeByLocalDate(req.getDate(), zoneId));
                }
                Page<PaymentOrder> paymentOrderPageResult = paymentOrderQueryWrapper.page(paymentOrderPage);
                List<PaymentOrder> paymentOrderList = paymentOrderPageResult.getRecords();
                List<ViewTransactionHistoryVo> responseVOOfPaymentList = new ArrayList<>();
                if (!CollectionUtils.isEmpty(paymentOrderList)) {
                    responseVOOfPaymentList = paymentOrderList.stream()
                            .filter(Objects::nonNull)
                            .map(item -> ViewTransactionHistoryVo.builder()
                                    .transactionStatus(item.getOrderStatus())
                                    .createTime(item.getCreateTime())
                                    .inrOrUsdtAmount(item.getActualAmount())
                                    .arbAmount(item.getActualAmount().multiply(item.getExchangeRates()).setScale(2, RoundingMode.DOWN))
                                    .platformOrder(item.getPlatformOrder())
                                    .transactionType(2)
//                                    .subTransactionType(2)
                                    .build())
                            .collect(Collectors.toList());
                }
                return RestResult.ok(PageUtils.flush(paymentOrderPage, responseVOOfPaymentList));
            default:
                return RestResult.failed("The type of transaction is not supported");
        }
    }

    @Override
    public RestResult<PageReturn<GroupInfoDTO>> findGroupInfo(FindGroupInfoReq requestVO) {
        // 查询前n级用户
        AntRelations parentMember = this.antRelationsService.selectParentByCount(requestVO.getCurrentMemberId(), requestVO.getCountOfBefore());
        // 查询后n级会员列表
        List<AntRelations> childMemberList = this.antRelationsService.selectChildByCount(requestVO.getCurrentMemberId(), requestVO.getCountOfOfter());
        // 全会员集合ID
        List<Long> allUserIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(childMemberList)) {
            allUserIdList = childMemberList.stream()
                    .filter(Objects::nonNull)
                    .map(AntRelations::getAntId)
                    .collect(Collectors.toList());
        }
        if (Objects.nonNull(parentMember)) {
            allUserIdList.add(parentMember.getAntId());
        }

        Page<MemberInfo> page = new Page<>();
        List<GroupInfoDTO> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(allUserIdList)) {
            return RestResult.ok(PageUtils.flush(page, resultList));
        }

        page.setCurrent(requestVO.getPageNo())
                .setSize(requestVO.getPageSize());
        LambdaQueryChainWrapper<MemberInfo> lambdaQuery = this.lambdaQuery();

        lambdaQuery.in(MemberInfo::getId, allUserIdList);
        if (Objects.nonNull(parentMember)) {
            lambdaQuery.orderByAsc(MemberInfo::getId);
        }
        lambdaQuery.orderByDesc(MemberInfo::getCreateTime);
        Page<MemberInfo> memberInfoPage = lambdaQuery.page(page);
        List<MemberInfo> memberInfoList = memberInfoPage.getRecords();
        if (!CollectionUtils.isEmpty(memberInfoList)) {
            resultList = memberInfoList.stream()
                    .filter(Objects::nonNull)
                    .map(item -> GroupInfoDTO.builder()
                            .groupRelation(Objects.nonNull(parentMember) && item.getId().equals(parentMember.getAntId()) ? 1 : 2)
                            .memberId(item.getId())
                            .mobileNumber(item.getMobileNumber())
                            .buyCommissionAmount(item.getTotalBuyCommissionAmount())
                            .sellCommissionAmount(item.getTotalSellCommissionAmount())
                            .buyAmount(item.getTotalBuySuccessAmount())
                            .sellAmount(item.getTotalSellSuccessAmount())
                            .createTime(item.getCreateTime())
                            .build())
                    .collect(Collectors.toList());
        }

        return RestResult.ok(PageUtils.flush(page, resultList));
    }

    private final RegisterRecordAsync registerRecordAsync;

    /*
     * 获取会员列表
     * */
    @Override
    @SneakyThrows
    public PageReturn<MemberInfolistPageDTO> listPage(MemberInfoListPageReq req) {


        Page<MemberInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        OrderItem orderItem = new OrderItem();

        LambdaQueryChainWrapper<MemberInfo> lambdaQuery = lambdaQuery();
        // 新增统计金额字段总计字段
        LambdaQueryWrapper<MemberInfo> queryWrapper = new QueryWrapper<MemberInfo>()
                .select("IFNULL(sum(balance), 0) as balanceTotal," +
                        "IFNULL(sum(total_buy_success_amount), 0) as totalBuyAmountTotal," +
                        "IFNULL(sum(total_sell_success_amount), 0) as totalSellAmountTotal," +
                        "IFNULL(sum(frozen_amount), 0) as frozenAmountTotal," +
                        "IFNULL(sum(bi_frozen_amount), 0) as biFrozenAmountTotal," +
                        "IFNULL(sum(recharge_total_amount), 0) as rechargeAmountTotal," +
                        "IFNULL(sum(withdraw_total_amount), 0) as withdrawAmountTotal," +
                        "IFNULL(sum(total_buy_bonus), 0) as totalBuyBonusTotal," +
                        "IFNULL(sum(total_sell_bonus), 0) as totalSellBonusTotal," +
                        "IFNULL(sum(platform_dividends), 0) as platformDividendsTotal," +
                        "IFNULL(sum(total_buy_commission_amount + total_sell_commission_amount), 0) as teamRewardsTotal," +
                        "IFNULL(sum(total_buy_bonus + total_sell_bonus + total_buy_commission_amount + total_sell_commission_amount + platform_dividends), 0) as totalBonusTotal"
                )
                .lambda();
        if (org.apache.commons.lang3.StringUtils.isBlank(req.getColumn())) {
            lambdaQuery.orderByDesc(MemberInfo::getId);
        } else {
            if (req.getColumn().equals("totalBuyCount")) {
                req.setColumn("totalBuySuccessCount");
            } else if (req.getColumn().equals("totalBuyAmount")) {
                req.setColumn("totalBuySuccessAmount");
            } else if (req.getColumn().equals("totalSellCount")) {
                req.setColumn("totalSellSuccessCount");
            } else if (req.getColumn().equals("totalSellAmount")) {
                req.setColumn("totalSellSuccessAmount");
            } else if (req.getColumn().equals("rechargeNum")) {
                req.setColumn("rechargeNum");
            } else if (req.getColumn().equals("rechargeTotalAmount")) {
                req.setColumn("rechargeTotalAmount");
            } else if (req.getColumn().equals("withdrawNum")) {
                req.setColumn("withdrawNum");
            } else if (req.getColumn().equals("withdrawTotalAmount")) {
                req.setColumn("withdrawTotalAmount");
            }
            if (req.getColumn().equals("transactionSuccessAmount")) {
                req.setColumn("(total_buy_success_amount + total_sell_success_amount)");
            }
            if (req.getColumn().equals("transactionSuccessCount")) {
                req.setColumn("(total_buy_success_count + total_sell_success_count)");
            }
            orderItem.setColumn(StrUtil.toUnderlineCase(req.getColumn()));
            orderItem.setAsc(req.isAsc());
            page.addOrder(orderItem);
        }
        //--动态查询 会员id
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.eq(MemberInfo::getId, req.getMemberId());
            queryWrapper.eq(MemberInfo::getId, req.getMemberId());
        }

        if (!StringUtils.isEmpty(req.getMerchantName())) {
            lambdaQuery.eq(MemberInfo::getMerchantName, req.getMerchantName());
            queryWrapper.eq(MemberInfo::getMerchantName, req.getMerchantName());
        }

        if (!StringUtils.isEmpty(req.getExternalMemberId())) {
            lambdaQuery.apply("SUBSTRING(member_id, LENGTH(merchant_code)+1) = '" + req.getExternalMemberId() + "'");
            queryWrapper.apply("SUBSTRING(member_id, LENGTH(merchant_code)+1) = '" + req.getExternalMemberId() + "'");
        }

        //--动态查询 会员id
        if (!ObjectUtils.isEmpty(req.getMemberGroupId())) {
            lambdaQuery.eq(MemberInfo::getMemberGroup, req.getMemberGroupId());
            queryWrapper.eq(MemberInfo::getMemberGroup, req.getMemberGroupId());
        }

        //--动态查询 会员账号
        if (!StringUtils.isEmpty(req.getMemberAccount())) {
            lambdaQuery.eq(MemberInfo::getMemberAccount, req.getMemberAccount());
            queryWrapper.eq(MemberInfo::getMemberAccount, req.getMemberAccount());
        }

        //--动态查询 钱包地址
        if (!StringUtils.isEmpty(req.getWalletAddress())) {
            lambdaQuery.eq(MemberInfo::getWalletAddress, req.getWalletAddress());
            queryWrapper.eq(MemberInfo::getWalletAddress, req.getWalletAddress());
        }

        //--动态查询 真是姓名
        if (!StringUtils.isEmpty(req.getRealName())) {
            lambdaQuery.eq(MemberInfo::getRealName, req.getRealName());
            queryWrapper.eq(MemberInfo::getRealName, req.getRealName());
        }

        //--动态查询 用户类型
        if (!StringUtils.isEmpty(req.getMemberType())) {
            lambdaQuery.eq(MemberInfo::getMemberType, req.getMemberType());
            queryWrapper.eq(MemberInfo::getMemberType, req.getMemberType());
        }


        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalBuyAmountStart())) {
            lambdaQuery.ge(MemberInfo::getTotalBuyAmount, req.getTotalBuyAmountStart());
            queryWrapper.ge(MemberInfo::getTotalBuyAmount, req.getTotalBuyAmountStart());
        }
        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalBuyAmountEnd())) {
            lambdaQuery.le(MemberInfo::getTotalBuyAmount, req.getTotalBuyAmountEnd());
            queryWrapper.le(MemberInfo::getTotalBuyAmount, req.getTotalBuyAmountEnd());
        }

        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalSellAmountStart())) {
            lambdaQuery.ge(MemberInfo::getTotalSellAmount, req.getTotalSellAmountStart());
            queryWrapper.ge(MemberInfo::getTotalSellAmount, req.getTotalSellAmountStart());
        }
        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalSellAmountEnd())) {
            lambdaQuery.le(MemberInfo::getTotalSellAmount, req.getTotalSellAmountEnd());
            queryWrapper.le(MemberInfo::getTotalSellAmount, req.getTotalSellAmountEnd());
        }
        // 新增账号状态 在线状态 买入状态 卖出状态筛选条件
        if (ObjectUtils.isNotEmpty(req.getStatus())) {
            lambdaQuery.eq(MemberInfo::getStatus, req.getStatus());
            queryWrapper.eq(MemberInfo::getStatus, req.getStatus());
        }

        if (ObjectUtils.isNotEmpty(req.getOnlineStatus())) {
            Map<Object, Object> onlineUserAccount = redisUtils.hmget(GlobalConstants.ONLINE_USER_KEY);
            List<String> accountList = new ArrayList<>();
            for (Map.Entry<Object, Object> objectObjectEntry : onlineUserAccount.entrySet()) {
                accountList.add(objectObjectEntry.getKey().toString());
            }
            if (accountList.isEmpty()) {
                accountList.add("-1");
            }
            if (req.getOnlineStatus().equals(MemberOnlineStatusEnum.ON_LINE.getCode())) {
                lambdaQuery.in(MemberInfo::getMemberAccount, accountList);
                queryWrapper.in(MemberInfo::getMemberAccount, accountList);
            }
            if (req.getOnlineStatus().equals(MemberOnlineStatusEnum.OFF_LINE.getCode())) {
                lambdaQuery.notIn(MemberInfo::getMemberAccount, accountList);
                queryWrapper.notIn(MemberInfo::getMemberAccount, accountList);
            }
        }
        if (ObjectUtils.isNotEmpty(req.getBuyStatus())) {
            lambdaQuery.eq(MemberInfo::getBuyStatus, req.getBuyStatus());
            queryWrapper.eq(MemberInfo::getBuyStatus, req.getBuyStatus());
        }

        if (ObjectUtils.isNotEmpty(req.getSellStatus())) {
            lambdaQuery.eq(MemberInfo::getSellStatus, req.getSellStatus());
            queryWrapper.eq(MemberInfo::getSellStatus, req.getSellStatus());
        }
        // 添加等级筛选
        if (ObjectUtils.isNotEmpty(req.getLevel())) {
            lambdaQuery.eq(MemberInfo::getLevel, req.getLevel());
            queryWrapper.eq(MemberInfo::getLevel, req.getLevel());
        }
        // 添加信用分区间筛选
        if (ObjectUtils.isNotEmpty(req.getMinCreditScore())) {
            lambdaQuery.ge(MemberInfo::getCreditScore, req.getMinCreditScore());
            queryWrapper.ge(MemberInfo::getCreditScore, req.getMinCreditScore());
        }
        if (ObjectUtils.isNotEmpty(req.getMaxCreditScore())) {
            lambdaQuery.le(MemberInfo::getCreditScore, req.getMaxCreditScore());
            queryWrapper.le(MemberInfo::getCreditScore, req.getMaxCreditScore());
        }

        Page<MemberInfo> finalPage = page;
        CompletableFuture<MemberInfo> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));
        CompletableFuture<Page<MemberInfo>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));
        CompletableFuture.allOf(totalFuture, resultFuture);
        page = resultFuture.get();
        MemberInfo totalInfo = totalFuture.get();
        JSONObject extent = new JSONObject();

        extent.put("balanceTotal", totalInfo.getBalanceTotal().toPlainString());
        extent.put("totalBuyAmountTotal", totalInfo.getTotalBuyAmountTotal().toPlainString());
        extent.put("totalSellAmountTotal", totalInfo.getTotalSellAmountTotal().toPlainString());
        extent.put("frozenAmountTotal", totalInfo.getFrozenAmountTotal().toPlainString());
        extent.put("biFrozenAmountTotal", totalInfo.getBiFrozenAmountTotal().toPlainString());
        extent.put("rechargeAmountTotal", totalInfo.getRechargeAmountTotal().toPlainString());
        extent.put("withdrawAmountTotal", totalInfo.getWithdrawAmountTotal().toPlainString());
        extent.put("totalBuyBonusTotal", totalInfo.getTotalBuyBonusTotal().toPlainString());
        extent.put("totalSellBonusTotal", totalInfo.getTotalSellBonusTotal().toPlainString());
        extent.put("platformDividendsTotal", totalInfo.getPlatformDividendsTotal().toPlainString());
        extent.put("teamRewardsTotal", totalInfo.getTeamRewardsTotal().toPlainString());
        extent.put("commissionTotal",totalInfo.getPlatformDividendsTotal().add(totalInfo.getTeamRewardsTotal()));
        extent.put("successTransactionTotal",totalInfo.getTotalBuyAmountTotal().add(totalInfo.getTotalSellAmountTotal()));
        extent.put("totalBonusTotal",totalInfo.getTotalBonusTotal());
        BigDecimal balancePageTotal = BigDecimal.ZERO;
        BigDecimal totalBuyAmountPageTotal = BigDecimal.ZERO;
        BigDecimal totalSellAmountPageTotal = BigDecimal.ZERO;
        BigDecimal frozenAmountPageTotal = BigDecimal.ZERO;
        BigDecimal biFrozenAmountPageTotal = BigDecimal.ZERO;
        BigDecimal rechargeAmountPageTotal = BigDecimal.ZERO;
        BigDecimal withdrawAmountPageTotal = BigDecimal.ZERO;

        BigDecimal totalBuyBonusPageTotal = BigDecimal.ZERO;
        BigDecimal totalSellBonusPageTotal = BigDecimal.ZERO;
        BigDecimal platformDividendsPageTotal = BigDecimal.ZERO;
        BigDecimal teamRewardsPageTotal = BigDecimal.ZERO;
        BigDecimal commissionPageTotal = BigDecimal.ZERO;
        BigDecimal successTransactionPageTotal = BigDecimal.ZERO;
        BigDecimal totalBonusPageTotal = BigDecimal.ZERO;
        List<MemberInfo> records = page.getRecords();

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<MemberInfolistPageDTO> list = new ArrayList<>();
        for (MemberInfo record : records) {
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(record, memberInfolistPageDTO);

            //优化买入次数 买入金额 卖出次数 卖出金额 只显示成功的
            if (redisUtils.hHasKey(GlobalConstants.ONLINE_USER_KEY, record.getMemberAccount())) {
                Long value = Long.parseLong(redisUtils.hget(GlobalConstants.ONLINE_USER_KEY, record.getMemberAccount()) + "");
                Long currentTime = System.currentTimeMillis();
                if (value > currentTime) {
                    // 说明该用户登录的令牌还没有过期
                    memberInfolistPageDTO.setOnlineStatus(MemberOnlineStatusEnum.ON_LINE.getCode());
                } else {
                    memberInfolistPageDTO.setOnlineStatus(MemberOnlineStatusEnum.OFF_LINE.getCode());
                }
            }
            //累计买入次数
            memberInfolistPageDTO.setTotalBuyCount(record.getTotalBuySuccessCount());

            //累计卖出次数
            memberInfolistPageDTO.setTotalSellCount(record.getTotalSellSuccessCount());

            //累计买入金额
            memberInfolistPageDTO.setTotalBuyAmount(record.getTotalBuySuccessAmount());

            //累计卖出金额
            memberInfolistPageDTO.setTotalSellAmount(record.getTotalSellSuccessAmount());
            if (org.apache.commons.lang3.StringUtils.isNotBlank(record.getMemberId()) && org.apache.commons.lang3.StringUtils.isNotBlank(record.getMerchantCode()) &&
                    record.getMemberId().contains(record.getMerchantCode())) {
                String externalMemberId = record.getMemberId().substring(record.getMerchantCode().length());
                memberInfolistPageDTO.setExternalMemberId(externalMemberId);
            }
            balancePageTotal = balancePageTotal.add(record.getBalance());
            totalBuyAmountPageTotal = totalBuyAmountPageTotal.add(record.getTotalBuySuccessAmount());
            totalSellAmountPageTotal = totalSellAmountPageTotal.add(record.getTotalSellSuccessAmount());
            frozenAmountPageTotal = frozenAmountPageTotal.add(record.getFrozenAmount());
            biFrozenAmountPageTotal = biFrozenAmountPageTotal.add(record.getBiFrozenAmount());
            rechargeAmountPageTotal = rechargeAmountPageTotal.add(record.getRechargeTotalAmount());
            withdrawAmountPageTotal = withdrawAmountPageTotal.add(record.getWithdrawTotalAmount());

            totalBuyBonusPageTotal = totalBuyBonusPageTotal.add(record.getTotalBuyBonus());
            totalSellBonusPageTotal = totalSellBonusPageTotal.add(record.getTotalSellBonus());
            platformDividendsPageTotal = platformDividendsPageTotal.add(record.getPlatformDividends());
            teamRewardsPageTotal = teamRewardsPageTotal.add(record.getTotalBuyCommissionAmount().add(record.getTotalSellCommissionAmount()));
            commissionPageTotal = commissionPageTotal.add(record.getTotalBuyBonus().add(record.getTotalSellBonus()).add(record.getPlatformDividends()));
            successTransactionPageTotal = successTransactionPageTotal.add(record.getTotalBuySuccessAmount().add(record.getTotalSellSuccessAmount()));
            // 总金额次数
            memberInfolistPageDTO.setTransactionSuccessCount(record.getTotalBuySuccessCount() + record.getTotalSellSuccessCount());

            // 总交易金额
            memberInfolistPageDTO.setTransactionSuccessAmount(record.getTotalSellSuccessAmount().add(record.getTotalBuySuccessAmount()));
            // 计算总奖励字段
            memberInfolistPageDTO.setTotalBonus(memberInfolistPageDTO.getTotalBuyBonus()
                    .add(memberInfolistPageDTO.getTotalSellBonus())
                    .add(memberInfolistPageDTO.getTotalBuyCommissionAmount())
                    .add(memberInfolistPageDTO.getTotalSellCommissionAmount())
                    .add(memberInfolistPageDTO.getPlatformDividends())
            );
            totalBonusPageTotal = totalBonusPageTotal.add(memberInfolistPageDTO.getTotalBonus());
            // 计算团队奖励
            memberInfolistPageDTO.setTeamRewards(memberInfolistPageDTO.getTotalSellCommissionAmount().add(memberInfolistPageDTO.getTotalBuyCommissionAmount()));

            list.add(memberInfolistPageDTO);
        }
        extent.put("balancePageTotal", balancePageTotal.toPlainString());
        extent.put("totalBuyAmountPageTotal", totalBuyAmountPageTotal.toPlainString());
        extent.put("totalSellAmountPageTotal", totalSellAmountPageTotal.toPlainString());
        extent.put("frozenAmountPageTotal", frozenAmountPageTotal.toPlainString());
        extent.put("biFrozenAmountPageTotal", biFrozenAmountPageTotal.toPlainString());
        extent.put("rechargeAmountPageTotal", rechargeAmountPageTotal.toPlainString());
        extent.put("withdrawAmountPageTotal", withdrawAmountPageTotal.toPlainString());

        extent.put("totalBuyBonusPageTotal", totalBuyBonusPageTotal.toPlainString());
        extent.put("totalSellBonusPageTotal", totalSellBonusPageTotal.toPlainString());
        extent.put("platformDividendsPageTotal", platformDividendsPageTotal.toPlainString());
        extent.put("teamRewardsPageTotal", teamRewardsPageTotal.toPlainString());
        extent.put("commissionPageTotal", commissionPageTotal.toPlainString());
        extent.put("successTransactionPageTotal", successTransactionPageTotal.toPlainString());
        extent.put("totalBonusPageTotal", totalBonusPageTotal.toPlainString());
//        IPage<CollectionOrderListVo> convert = page.convert(CollectionOrder -> BeanUtil.copyProperties(CollectionOrder, CollectionOrderListVo.class));
        return PageUtils.flush(page, list, extent);


    }

    /*
     * 获取当前会员信息
     * */
    @Override
    public MemberInfoVo currentMemberInfo() {
        Long currentUserId = UserContext.getCurrentUserId();
        AssertUtil.notEmpty(currentUserId, ResultCode.RELOGIN);
        MemberInfo memberInfo = lambdaQuery().eq(MemberInfo::getId, currentUserId).one();
        AssertUtil.notEmpty(memberInfo, ResultCode.USERNAME_OR_PASSWORD_ERROR);
        MemberInfoVo memberInfoVo = new MemberInfoVo();
        BeanUtils.copyProperties(memberInfo, memberInfoVo);

        return memberInfoVo;
    }

    /**
     * 根据用户名获取认证用户信息，携带角色和密码
     *
     * @param username
     * @return
     */
    @Override
    public MemberAuthDTO getByUsername(String username) {
        MemberAuthDTO memberAuthDTO = this.baseMapper.getByUsername(username);
        return memberAuthDTO;
    }

    /**
     * 更新会员: 扣除余额 (将会员余额转到到冻结金额中)、将进行中的卖出订单数+1 累计卖出次数 + 1
     *
     * @param memberInfo
     * @param amount
     * @return {@link Boolean}
     */
    @Override
    public Boolean updatedMemberInfo(MemberInfo memberInfo, BigDecimal amount) {

        // 扣除会员余额
        memberInfo.setBalance(memberInfo.getBalance().subtract(amount));

        //增加冻结金额
        memberInfo.setFrozenAmount(memberInfo.getFrozenAmount().add(amount));

        //将进行中的订单数+1
//        memberInfo.setActiveSellOrderCount(memberInfo.getActiveSellOrderCount() + 1);

        //累计卖出次数 + 1
        memberInfo.setTotalSellCount(memberInfo.getTotalSellCount() + 1);

        //累计卖出金额
//        memberInfo.setTotalSellAmount(memberInfo.getTotalSellAmount().add(amount));

        return updateById(memberInfo);
    }

    /**
     * 根据手机号获取会员信息
     *
     * @param phoneNumber
     * @return {@link MemberInfo}
     */
    @Override
    public MemberInfo getMemberByPhoneNumber(String phoneNumber) {
        return lambdaQuery().eq(MemberInfo::getMemberAccount, phoneNumber).or().eq(MemberInfo::getMobileNumber, phoneNumber).one();
    }

    /**
     * 根据邮箱号获取会员信息
     *
     * @param emailAccount
     * @return {@link MemberInfo}
     */
    @Override
    public MemberInfo getMemberByEmailAccount(String emailAccount) {
        return lambdaQuery().eq(MemberInfo::getMemberAccount, emailAccount).or().eq(MemberInfo::getEmailAccount, emailAccount).one();
    }

    /**
     * 重置会员登录密码
     *
     * @param id
     * @param passwd
     * @return {@link Boolean}
     */
    @Override
    public Boolean resetPassword(Long id, String passwd) {
        return lambdaUpdate().eq(MemberInfo::getId, id).set(MemberInfo::getPassword, passwd).update();
    }

    /**
     * 根据会员id更新手机号
     *
     * @param id
     * @param newPhoneNumber
     * @return {@link Boolean}
     */
    @Override
    public Boolean updatePhoneNumber(String id, String newPhoneNumber) {
        //更新手机号
        return lambdaUpdate().eq(MemberInfo::getId, id).set(MemberInfo::getMobileNumber, newPhoneNumber).update();
    }

    /**
     * 根据会员id更新邮箱号
     *
     * @param id
     * @param newEmail
     * @return {@link Boolean}
     */
    @Override
    public Boolean updateEmail(String id, String newEmail) {
        //更新邮箱号
        return lambdaUpdate().eq(MemberInfo::getId, id).set(MemberInfo::getEmailAccount, newEmail).update();
    }

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
    @Override
    public Boolean idenAuthentication(Long id, String realName, String idCardNumber, String fileName, String facePhoto) {
        return lambdaUpdate()
                .eq(MemberInfo::getId, id)
                .set(MemberInfo::getRealName, realName)//真实姓名
                .set(MemberInfo::getIdCardNumber, idCardNumber)//身份证号
                .set(MemberInfo::getIdCardImage, fileName)//证件图片
                .set(MemberInfo::getFacePhoto, facePhoto)//人脸照片
                .set(MemberInfo::getAuthenticationStatus, MemberAuthenticationStatusEnum.AUTHENTICATED.getCode())//实名认证状态
                .set(MemberInfo::getRealNameVerificationTime, LocalDateTime.now(ZoneId.systemDefault()))//实名认证时间
                .update();
    }

    /**
     * 根据实名信息获取会员信息
     *
     * @param idCardNumber
     * @return {@link MemberInfo}
     */
    @Override
    public MemberInfo getMemberByCardNumber(String idCardNumber) {
        return lambdaQuery().eq(MemberInfo::getIdCardNumber, idCardNumber).last("LIMIT 1").one();
    }

    @Override
    public MemberInfolistPageDTO recharge(MemberInfoRechargeReq req) {
        String updateBy = UserContext.getCurrentUserName();
        amountChangeUtil.insertMemberChangeAmountRecord(req.getId().toString(), req.getBuyAmount(), ChangeModeEnum.ADD, "ARB", orderNumberGenerator.generateOrderNo("HYSF"), MemberAccountChangeEnum.UPPER_DIVISION, updateBy, "", req.getRemark(), "");
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        MemberManualLogDTO memberManualLogDTO = new MemberManualLogDTO();
        memberManualLogDTO.setMemberId(req.getId().toString());
        memberManualLogDTO.setRemark(req.getRemark());
        memberManualLogDTO.setAmount(req.getBuyAmount());
        memberManualLogDTO.setOpType(Integer.parseInt(MemberManualEnum.UPPER_DIVISION.getCode()));
        memberManualLogDTO.setCreateBy(UserContext.getCurrentUserName());
        memberManualLogDTO.setUpdateBy(UserContext.getCurrentUserName());
        mapper.insert(memberManualLogDTO);
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        return memberInfolistPageDTO;
    }

    @Override
    public MemberInfolistPageDTO withdrawal(MemberInfoWithdrawalReq req) {
        String updateBy = UserContext.getCurrentUserName();
        amountChangeUtil.insertMemberChangeAmountRecord(req.getId().toString(), req.getSellAmount(), ChangeModeEnum.SUB, "ARB", orderNumberGenerator.generateOrderNo("HYXF"), MemberAccountChangeEnum.LOWER_DIVISION, updateBy, "", req.getRemark(), "");
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        MemberManualLogDTO memberManualLogDTO = new MemberManualLogDTO();
        memberManualLogDTO.setMemberId(req.getId().toString());
        memberManualLogDTO.setRemark(req.getRemark());
        memberManualLogDTO.setAmount(req.getSellAmount());
        memberManualLogDTO.setOpType(Integer.parseInt(MemberManualEnum.LOWER_DIVISION.getCode()));
        memberManualLogDTO.setCreateBy(UserContext.getCurrentUserName());
        memberManualLogDTO.setUpdateBy(UserContext.getCurrentUserName());
        mapper.insert(memberManualLogDTO);
        return memberInfolistPageDTO;
    }

    @Override
    public MemberInfolistPageDTO freeze(MemberInfoFreezeReq req) throws Exception {
        String updateBy = UserContext.getCurrentUserName();
        Boolean changeResult = amountChangeUtil.insertMemberChangeAmountRecord(req.getId().toString(), req.getFrozenAmount(), ChangeModeEnum.SUB, "ARB", orderNumberGenerator.generateOrderNo("DJ"), MemberAccountChangeEnum.FREEZE, updateBy, req.getRemark());
        if (!changeResult) {
            throw new Exception("冻结失败!");
        }
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        MemberManualLogDTO memberManualLogDTO = new MemberManualLogDTO();
        memberManualLogDTO.setMemberId(req.getId().toString());
        memberManualLogDTO.setRemark(req.getRemark());
        memberManualLogDTO.setAmount(req.getFrozenAmount());
        memberManualLogDTO.setOpType(Integer.parseInt(MemberManualEnum.FREEZE.getCode()));
        memberManualLogDTO.setCreateBy(UserContext.getCurrentUserName());
        memberManualLogDTO.setUpdateBy(UserContext.getCurrentUserName());
        mapper.insert(memberManualLogDTO);
        return memberInfolistPageDTO;
    }


    @Override
    public MemberInfolistPageDTO unfreeze(MemberInfoFreezeReq req) throws Exception {
        String updateBy = UserContext.getCurrentUserName();
        Boolean changeResult = amountChangeUtil.insertMemberChangeAmountRecord(req.getId().toString(), req.getFrozenAmount(), ChangeModeEnum.ADD, "ARB", orderNumberGenerator.generateOrderNo("JD"), MemberAccountChangeEnum.UNFREEZE, updateBy, req.getRemark());
        if (!changeResult) {
            throw new Exception("解冻失败!");
        }
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        MemberManualLogDTO memberManualLogDTO = new MemberManualLogDTO();
        memberManualLogDTO.setMemberId(req.getId().toString());
        memberManualLogDTO.setRemark(req.getRemark());
        memberManualLogDTO.setAmount(req.getFrozenAmount());
        memberManualLogDTO.setOpType(Integer.parseInt(MemberManualEnum.UNFREEZE.getCode()));
        memberManualLogDTO.setCreateBy(UserContext.getCurrentUserName());
        memberManualLogDTO.setUpdateBy(UserContext.getCurrentUserName());
        mapper.insert(memberManualLogDTO);
        return memberInfolistPageDTO;
    }

    @Override
    public MemberInfolistPageDTO bonus(MemberInfoBonusReq req) {
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        memberInfo.setBuyBonusProportion(req.getBuyBonusProportion());
        memberInfo.setSellBonusProportion(req.getSellBonusProportion());
        baseMapper.updateById(memberInfo);
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        return memberInfolistPageDTO;
    }


    @Override
    public MemberInfolistPageDTO resetpwd(MemberInfoIdReq req) {
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        memberInfo.setPassword(passwordEncoder.encode("aa123456"));
        baseMapper.updateById(memberInfo);
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        return memberInfolistPageDTO;
    }

    @Override
    public MemberInfolistPageDTO resetPayPwd(MemberInfoIdReq req) {
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        memberInfo.setPaymentPassword(passwordEncoder.encode("1234"));
        baseMapper.updateById(memberInfo);
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        return memberInfolistPageDTO;
    }

    /**
     * 将会员被申诉次数+1
     *
     * @return {@link Boolean}
     */
    @Override
    public Boolean incrementMemberComplaintCount(MemberInfo memberInfo) {
        return lambdaUpdate()
                .eq(MemberInfo::getId, memberInfo.getId())
                .set(MemberInfo::getAppealCount, memberInfo.getAppealCount() + 1)
                .update();
    }


    @Override
    public PageReturn<MerchantMemberInfoPageDTO> merchantListPage(MemberInfoListPageReq req) {
        Page<MemberInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        OrderItem orderItem = new OrderItem();

        LambdaQueryChainWrapper<MemberInfo> lambdaQuery = lambdaQuery();
        if (org.apache.commons.lang3.StringUtils.isBlank(req.getColumn())) {
            lambdaQuery.orderByDesc(MemberInfo::getId);
        } else {
            orderItem.setColumn(StrUtil.toUnderlineCase(req.getColumn()));
            orderItem.setAsc(req.isAsc());
            page.addOrder(orderItem);
        }
        //--动态查询 会员id
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.eq(MemberInfo::getId, req.getMemberId());
        }

        if (!StringUtils.isEmpty(req.getMerchantCode())) {
            lambdaQuery.eq(MemberInfo::getMerchantCode, req.getMerchantCode());
        }

        if (!StringUtils.isEmpty(req.getMerchantName())) {
            lambdaQuery.eq(MemberInfo::getMerchantName, req.getMerchantName());
        }

        if (!StringUtils.isEmpty(req.getExternalMemberId())) {
            lambdaQuery.like(MemberInfo::getMemberId, req.getExternalMemberId());
        }

        //--动态查询 会员id
        if (!ObjectUtils.isEmpty(req.getMemberGroupId())) {
            lambdaQuery.eq(MemberInfo::getMemberGroup, req.getMemberGroupId());
        }

        //--动态查询 会员账号
        if (!StringUtils.isEmpty(req.getMemberAccount())) {
            lambdaQuery.eq(MemberInfo::getMemberAccount, req.getMemberAccount());
        }

        //--动态查询 钱包地址
        if (!StringUtils.isEmpty(req.getWalletAddress())) {
            lambdaQuery.eq(MemberInfo::getWalletAddress, req.getWalletAddress());
        }

        //--动态查询 真是姓名
        if (!StringUtils.isEmpty(req.getRealName())) {
            lambdaQuery.eq(MemberInfo::getRealName, req.getRealName());
        }

        //--动态查询 用户类型
        if (!StringUtils.isEmpty(req.getMemberType())) {
            lambdaQuery.eq(MemberInfo::getMemberType, req.getMemberType());
        }

        //--动态查询 用户类型
        if (!StringUtils.isEmpty(req.getStatus())) {
            lambdaQuery.eq(MemberInfo::getStatus, req.getStatus());
        }

        //--动态查询 用户类型
        if (!StringUtils.isEmpty(req.getBuyStatus())) {
            lambdaQuery.eq(MemberInfo::getBuyStatus, req.getBuyStatus());
        }

        //--动态查询 用户类型
        if (!StringUtils.isEmpty(req.getSellStatus())) {
            lambdaQuery.eq(MemberInfo::getSellStatus, req.getSellStatus());
        }


        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalBuyAmountStart())) {
            lambdaQuery.ge(MemberInfo::getTotalBuyAmount, req.getTotalBuyAmountStart());
        }
        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalBuyAmountEnd())) {
            lambdaQuery.le(MemberInfo::getTotalBuyAmount, req.getTotalBuyAmountEnd());
        }

        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalSellAmountStart())) {
            lambdaQuery.ge(MemberInfo::getTotalSellAmount, req.getTotalSellAmountStart());
        }
        //--动态查询 买入金额范围开始
        if (ObjectUtils.isNotEmpty(req.getTotalSellAmountEnd())) {
            lambdaQuery.le(MemberInfo::getTotalSellAmount, req.getTotalSellAmountEnd());
        }

        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MemberInfo> records = page.getRecords();
        for (MemberInfo record : records) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(record.getMemberId()) && org.apache.commons.lang3.StringUtils.isNotBlank(record.getMerchantCode()) &&
                    record.getMemberId().contains(record.getMerchantCode())) {
                String externalMemberId = record.getMemberId().substring(record.getMerchantCode().length());
                record.setMemberId(externalMemberId);
            }
        }
        List<MerchantMemberInfoPageDTO> list = walletMapStruct.merchantListPage(records);
        //IPage＜实体＞转 IPage＜Vo＞
        return PageUtils.flush(page, list);
    }


    /**
     * 完成实名认证任务
     *
     * @return {@link Boolean}
     */
    @Override
    @Transactional
    public Boolean completeRealNameVerificationTask() {

        //分布式锁key ar-wallet-handleDailyTask
        String key = "ar-wallet-handleDailyTask";
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取实名认证的任务信息
                TaskManager taskManager = taskManagerService.getTaskDetailsByType(RewardTaskTypeEnum.REAL_AUTH.getCode());

                if (taskManager == null) {
                    log.error("完成实名认证任务 job处理失败 获取任务信息失败");
                    return false;
                }

                //查询所有已经实名认证过并且尚未完成实名认证任务的会员
                List<MemberInfo> verifiedMembers = selectVerifiedMembersWithoutTask();

                // 2. 准备批量插入的任务状态记录
                List<MemberTaskStatus> taskStatusRecords = new ArrayList<>();

                for (MemberInfo member : verifiedMembers) {
                    MemberTaskStatus taskStatus = new MemberTaskStatus();
                    taskStatus.setMemberId(member.getId());//会员id
                    taskStatus.setTaskType(Integer.valueOf(taskManager.getTaskType()));/* 实名认证任务类型 */
                    taskStatus.setTaskId(taskManager.getId());//实名认证任务的ID
                    taskStatus.setCompletionStatus(1); // 任务完成
                    taskStatus.setRewardClaimed(0); // 奖励未领取
                    taskStatus.setCompletionDate(LocalDate.now());//任务完成日期 今天
                    taskStatus.setOrderNo(orderNumberGenerator.generateOrderNo("RW"));//任务订单号
                    taskStatus.setCreateTime(LocalDateTime.now());//任务完成时间
                    taskStatus.setTaskCycle(1);//任务周期 1:一次性任务 2:周期性-每天

                    taskStatusRecords.add(taskStatus);
                }

                // 3. 批量插入任务完成记录
                if (!taskStatusRecords.isEmpty()) {
                    //批量插入 批次大小限制: 1000条记录
                    boolean saveBatch = memberTaskStatusService.saveBatch(taskStatusRecords, 1000);

                    if (!saveBatch) {
                        //执行失败了 手动抛出异常回滚
                        log.error("完成实名认证任务 job处理失败 sql执行结果: {}", saveBatch);
                        throw new RuntimeException();
                    } else {
                        log.info("完成实名认证任务 job处理成功 sql执行结果: {}", saveBatch);
                        return true;
                    }
                }

                return true;
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("完成实名认证任务失败", e.getMessage());
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    public PageReturn<MemberInfolistPageDTO> relationMemberList(MemberInfoListPageReq req) {

        Page<MemberInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MemberInfo> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(req.getMemberId())) {
            lambdaQuery.eq(MemberInfo::getId, req.getMemberId());
        }
        if (StringUtils.isNotBlank(req.getExternalMemberId())) {
            lambdaQuery.like(MemberInfo::getMemberId, req.getExternalMemberId());
        }
        if (StringUtils.isNotBlank(req.getMemberAccount())) {
            lambdaQuery.eq(MemberInfo::getMemberAccount, req.getMemberAccount());
        }
        if (StringUtils.isNotBlank(req.getMerchantName())) {
            lambdaQuery.eq(MemberInfo::getMerchantName, req.getMerchantName());
        }
        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(MemberInfo::getMerchantCode, req.getMerchantCode());
        }
        if (StringUtils.isNotBlank(req.getRelationsIp())) {
            lambdaQuery.eq(MemberInfo::getLoginIp, req.getRelationsIp());
        }
        if (StringUtils.isNotBlank(req.getLevel())) {
            lambdaQuery.eq(MemberInfo::getLevel, req.getLevel());
        }
        lambdaQuery.orderByDesc(MemberInfo::getCreateTime);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MemberInfo> records = page.getRecords();
        for (MemberInfo item : records) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(item.getMemberId()) && org.apache.commons.lang3.StringUtils.isNotBlank(item.getMerchantCode()) &&
                    item.getMemberId().contains(item.getMerchantCode())) {
                String externalMemberId = item.getMemberId().substring(item.getMerchantCode().length());
                item.setMemberId(externalMemberId);
            }
            item.setTotalBuyAmount(item.getTotalBuySuccessAmount());
            item.setTotalSellAmount(item.getTotalSellSuccessAmount());
        }
        List<MemberInfolistPageDTO> list = walletMapStruct.relationMemberToDto(records);
        return PageUtils.flush(page, list);
    }


    /**
     * 查询所有已经实名认证过并且尚未完成实名认证任务的会员
     *
     * @return {@link List}<{@link MemberInfo}>
     */
    public List<MemberInfo> selectVerifiedMembersWithoutTask() {
        // 查询所有已经实名认证的会员
        List<MemberInfo> verifiedMembers = lambdaQuery()
                .in(MemberInfo::getAuthenticationStatus, Arrays.asList(1, 3))
                .list();

        // 查询所有有实名认证任务完成记录的会员ID
        List<Long> membersWithCompletedTask = memberTaskStatusService.lambdaQuery()
                .eq(MemberTaskStatus::getTaskType, RewardTaskTypeEnum.REAL_AUTH.getCode())
                .list()
                .stream()
                .map(MemberTaskStatus::getMemberId)
                .collect(Collectors.toList());

        // 过滤掉那些已经有实名认证任务完成记录的会员
        return verifiedMembers.stream()
                .filter(member -> !membersWithCompletedTask.contains(member.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public MemberInfolistPageDTO remark(MemberInfoIdReq req) {
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        memberInfo.setRemark(req.getRemark());
        baseMapper.updateById(memberInfo);
        MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
        BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
        return memberInfolistPageDTO;
    }

    @Override
    public MemberInfoDTO getInfo(MemberInfoIdGetInfoReq req) {
        MemberInfo memberInfo = baseMapper.getMemberInfoById(req.getId().toString());
        if (ObjectUtils.isEmpty(memberInfo)) {
            return null;
        }
        //baseMapper.updateById(memberInfo);
        MemberInfoDTO memberInfoDTO = new MemberInfoDTO();
        BeanUtils.copyProperties(memberInfo, memberInfoDTO);

        //注册时间
        memberInfoDTO.setRegisterTime(memberInfo.getCreateTime());

        //优化显示
        //累计买入金额 (只取成功的)
        memberInfoDTO.setTotalBuyAmount(memberInfo.getTotalBuySuccessAmount());

        //累计卖出次数 (只取成功的)
        memberInfoDTO.setTotalSellAmount(memberInfo.getTotalSellSuccessAmount());

        //商户会员ip
        memberInfoDTO.setMerchantMemberIp(redisUtil.getMemberLastLoginIp(String.valueOf(memberInfo.getId())));
        // 商户会员id
        if (org.apache.commons.lang3.StringUtils.isNotBlank(memberInfoDTO.getMemberId()) && org.apache.commons.lang3.StringUtils.isNotBlank(memberInfoDTO.getMerchantCode()) &&
                memberInfoDTO.getMemberId().contains(memberInfoDTO.getMerchantCode())) {
            String externalMemberId = memberInfoDTO.getMemberId().substring(memberInfoDTO.getMerchantCode().length());
            memberInfoDTO.setExternalMemberId(externalMemberId);
        }
        return memberInfoDTO;
    }

    /**
     * 获取当前会员信息
     *
     * @return {@link RestResult}<{@link MemberInformationVo}>
     */
    @Override
    public RestResult<MemberInformationVo> getCurrentMemberInfo() {

        //获取当前会员信息
        MemberInfo memberInfo = getById(UserContext.getCurrentUserId());

        if (memberInfo == null) {
            log.error("获取当前会员信息失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //创建返回VO对象
        MemberInformationVo memberInformationVo = new MemberInformationVo();

        //拷贝值
        BeanUtils.copyProperties(memberInfo, memberInformationVo);

        //设置实名认证状态
        memberInformationVo.setAuthenticationStatus("2".equals(memberInfo.getAuthenticationStatus()) ? "0" : "1");

        //前台显示 主键自增ID
        memberInformationVo.setMemberId(String.valueOf(memberInfo.getId()));

        //获取通知数
        memberInformationVo.setNotificationCount(memberNotificationService.getNotificationCountByMemberId(memberInformationVo.getMemberId()));
        //INR余额 1-1
        memberInformationVo.setInrBalance(memberInformationVo.getBalance());

        //是否设置了支付密码
        if (StringUtils.isNotEmpty(memberInfo.getPaymentPassword())) {
            memberInformationVo.setHasPaymentPassword(1);
        }

        //判断会员类型 如果是商户会员 需要获取以下信息
//        if (!MemberTypeEnum.WALLET_MEMBER.getCode().equals(memberInfo.getMemberType())) {
//            //商户会员
//
//            //获取商户信息
//            MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(memberInfo.getMerchantCode());
//
//            if (merchantInfo == null) {
//                log.error("获取当前会员信息失败: 获取商户信息失败, 会员信息: {}", memberInfo);
//                return RestResult.failure(ResultCode.MERCHANT_NOT_EXIST);
//            }
//
//            //商户名称
//            memberInformationVo.setMerchantName(merchantInfo.getUsername());
//
//            //商户logo
//            memberInformationVo.setMerchantIcon(merchantInfo.getIcon());
//
//            //快捷金额
//            memberInformationVo.setQuickAmount(merchantInfo.getQuickAmount());
//        }

        if (controlSwitchService.isSwitchEnabled(SwitchIdEnum.CHECK_ACTIVE_TASKS.getSwitchId())) {
            //任务活动总开关
            memberInformationVo.setTaskSwitch("1");
        }

        // 获取会员等级福利配置
        MemberLevelWelfareConfig welfareByLevel = memberLevelWelfareConfigService.getWelfareByLevel(memberInformationVo.getLevel());
        if (welfareByLevel != null) {
            memberInformationVo.setSelfSelectionBuy(welfareByLevel.getSelfSelectionBuy());
            //根据会员标签获取对应配置信息
            TradeConfigScheme schemeConfigByMemberTag = tradeConfigHelperUtil.getSchemeConfigByMemberTag(memberInfo);
            memberInformationVo.setQuickBuyMinLimit(String.valueOf(schemeConfigByMemberTag.getSchemeMinPurchaseAmount().longValue()));
            memberInformationVo.setQuickBuyMaxLimit(welfareByLevel.getSingleAmountLimit() == null ? null : String.valueOf(welfareByLevel.getSingleAmountLimit().longValue()));
        }

        // 获取交易信用分限额
        TradeConfig tradeConfig = tradeConfigService.getById(1);
        memberInformationVo.setTradeCreditScoreLimit(tradeConfig.getTradeCreditScoreLimit());

        // 如果 会员有累计买入成功或卖出成功记录的 那么就可以看到自选列表
        int buyCount = memberInformationVo.getTotalBuySuccessCount() != null ? memberInformationVo.getTotalBuySuccessCount() : 0;
        int sellCount = memberInformationVo.getTotalSellSuccessCount() != null ? memberInformationVo.getTotalSellSuccessCount() : 0;

        if (buyCount > 0 || sellCount > 0) {
            memberInformationVo.setSelfSelectionBuy(1);
        }

        //银行卡号长度限制
        memberInformationVo.setBankCardNumberLengthLimit(tradeConfig.getMinBankCodeNumber() + "-" + tradeConfig.getMaxBankCodeNumber());

        log.info("获取当前会员信息成功 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), memberInformationVo);
        memberInformationVo.setIsCashBack(memberInfo.getCashBackAttention());

        // 设置分红最低级响应
        DividendConfig dividendConfig = this.dividendConfigService.lambdaQuery().eq(DividendConfig::getDividendsLevel, 1).one();
        if (Objects.isNull(dividendConfig)) {
            RestResult.failed("Dividends config error");
        }

        // 设置平台分红 展示最低等级的平台分红
        memberInformationVo.setPlatformDividends(dividendConfig.getRewardRatio());

        // 设置充值奖励
        memberInformationVo.setRechargeRewardRatio(memberInfo.getBuyBonusProportion() == null ? new BigDecimal("0") : memberInfo.getBuyBonusProportion());

        // 设置INR买入IToken汇率
        memberInformationVo.setInrBuyItokenRatio(tradeConfig.getBuyInrRatio());

        // 设置USDT买入IToken汇率
        memberInformationVo.setUsdtBuyItoken(systemCurrencyService.getCurrencyExchangeRate(CurrenceEnum.INDIA.getCode()));
        return RestResult.ok(memberInformationVo);
    }

    /**
     * 获取每日公告内容
     *
     * @param language
     * @return {@link RestResult}<{@link DailyAnnouncementVo}>
     */
    @Override
    public RestResult<DailyAnnouncementVo> getDailyAnnouncement(Integer language) {
        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("获取每日公告内容失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //判断用户是否查看过 今日公告内容
        if (hasUserViewedAnnouncement(memberInfo.getId())) {
            //已经看过每日公告内容了 直接返回
            DailyAnnouncementVo dailyAnnouncementVo = new DailyAnnouncementVo();
            dailyAnnouncementVo.setDailyAnnouncementStatus(1);
            return RestResult.ok(dailyAnnouncementVo);
        }

        String lang = String.valueOf(language);
        // 检索Redis
        FrontPageConfig frontPageConfig = (FrontPageConfig) redisUtils.hget(RedisConstants.FRONT_PAGE_CONFIG, lang);
        if (Objects.isNull(frontPageConfig)) {
            FrontPageConfig frontPageConfigByLang = this.frontPageConfigService.lambdaQuery()
                    .eq(FrontPageConfig::getLang, lang)
                    .one();
            if (Objects.isNull(frontPageConfigByLang)) {
                return RestResult.ok(new DailyAnnouncementVo());
            }
            // 同步至Redis
            redisUtils.hset(RedisConstants.FRONT_PAGE_CONFIG, lang, frontPageConfigByLang);
            redisUtils.sSet(getTodayRedisKey(), memberInfo.getId().toString());
            return RestResult.ok(
                    DailyAnnouncementVo.builder()
                            .dailyAnnouncementStatus(0)
                            .dailyAnnouncementContent(frontPageConfigByLang.getContent())
                            .build()
            );
        }

        return RestResult.ok(
                DailyAnnouncementVo.builder()
                        .dailyAnnouncementStatus(0)
                        .dailyAnnouncementContent(frontPageConfig.getContent())
                        .build()
        );
    }


    /**
     * 检查用户是否已查看今日公告
     *
     * @param userId 用户的ID
     * @return true 如果用户已查看公告，否则返回 false
     */
    public boolean hasUserViewedAnnouncement(Long userId) {
        String todayKey = getTodayRedisKey();
        return redisTemplate.opsForSet().isMember(todayKey, userId.toString());
    }

    /**
     * 标记用户已查看今日公告
     *
     * @return {@link RestResult}
     */
    @Override
    public RestResult markAnnouncementAsViewed() {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("标记用户已查看今日公告失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        // 获取今天的日期作为键的一部分
        String todayKey = getTodayRedisKey();
        Long userId = memberInfo.getId();

        // 添加用户ID到Redis Set中
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Long added = setOps.add(todayKey, userId.toString());

        // 检查是否成功添加元素，如果是，则可能需要设置过期时间
        if (added > 0) {
            // 检查这个键是否已经设置过期时间，如果没有设置（新键），则设置
            Long ttl = redisTemplate.getExpire(todayKey);
            if (ttl == null || ttl == -1) { // 检查是否已经设置过期时间
                setExpireAtMidnight(todayKey);
            }
        }

        return RestResult.ok();
    }

    @Override
    public RestResult markCashBackAttention() {
        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("标记用户退回订单已弹窗: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }
        memberInfo.setCashBackAttention(0);
        boolean b = updateById(memberInfo);
        return b ? RestResult.ok() : RestResult.failed();
    }

//    private BigDecimal sellRewardRatio() {
//        TradeConfig tradeConfig = this.tradeConfigService.getById(1);
//        if (Objects.isNull(tradeConfig)) {
//            log.error("交易配置异常");
//            return BigDecimal.ZERO;
//        }
//        return tradeConfig.getSellRewardRatio();
//    }

    /**
     * 后台创建会员
     *
     * @param memberInfoReq
     * @param request
     * @return {@link RestResult}<{@link MemberInfolistPageDTO}>
     */
    @Override
    @Transactional
    public RestResult<MemberInfolistPageDTO> createMemberInfo(MemberInfoReq memberInfoReq, HttpServletRequest request) {

        String regex = "^\\d{10,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(memberInfoReq.getMemberAccount());
        if (!matcher.matches()) {
            return RestResult.failed("Account(Phone Number) Invalid");
        }

        //查看会员账号是否被使用
        //判断手机号是否被注册
        if (getMemberByPhoneNumber(memberInfoReq.getMemberAccount()) != null) {
            return RestResult.failed("该会员账号已被使用");
        }

        if (getMemberByEmailAccount(memberInfoReq.getMemberAccount()) != null) {
            return RestResult.failed("该会员账号已被使用");
        }

        if (getByUsername(memberInfoReq.getMemberAccount()) != null) {
            return RestResult.failed("该会员账号已被使用");
        }

        // 校验会员类型
        MemberTypeEnum memberTypeEnum = MemberTypeEnum.buildMemberTypeEnumByCode(memberInfoReq.getMemberType());
        if (Objects.isNull(memberInfoReq.getMemberType()) || Objects.isNull(memberTypeEnum)) {
            return RestResult.failed("Unsupported member types");
        }

        // 邀请码为空
        if (StringUtils.isEmpty(memberInfoReq.getInviteCode()) || StringUtils.isEmpty(memberInfoReq.getInviteCode().trim())) {
            return createSuperLevelMember(
                    memberInfoReq.getMemberAccount(),
                    memberInfoReq.getPassword(),
                    memberInfoReq.getRemark(),
                    memberTypeEnum,
                    false,
                    request
            );
        }
        // 邀请码不为空
        return createMemberByInviteCode(
                memberInfoReq.getInviteCode(),
                memberInfoReq.getMemberAccount(),
                memberInfoReq.getPassword(),
                memberInfoReq.getRemark(),
                memberTypeEnum,
                request
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public RestResult<MemberInfolistPageDTO> createMemberByInviteCode(
            String inviteCode,
            String phoneNumber,
            String password,
            String remark,
            MemberTypeEnum memberType,
            HttpServletRequest request
    ) {
        // 校验邀请码是否存在
        InviteLink inviteLink = this.inviteLinkMapper.selectOne(
                new QueryWrapper<InviteLink>()
                        .lambda()
                        .eq(InviteLink::getInviteCode, inviteCode)
        );
        if (Objects.isNull(inviteLink)) {
            log.error("邀请码不存在 邀请码: {}", inviteCode);
            return RestResult.failed(String.format("Invite coke(%s) is not exist", inviteCode));
        }

        MemberInfo memberInfoOfInviteCode = this.getById(inviteLink.getAntId());
        // 校验邀请码是否有对应的用户
        if (Objects.isNull(memberInfoOfInviteCode)) {
            log.error("邀请码对应的用户不存在 邀请码: {}", inviteCode);
            return RestResult.failed(String.format("User does not exist of current invite code(%s)", inviteCode));
        }

        // 校验邀请码对应的用户类型与当前用户类型是否一致
        if (!memberType.equals(MemberTypeEnum.buildMemberTypeEnumByCode(memberInfoOfInviteCode.getMemberType()))) {
            log.error("邀请码对应的用户类型与当前指定的用户类型不一致 邀请码: {}", inviteCode);
            return RestResult.failed(String.format("The memberType(%s) of current inviteCode(%s) is different from current memberType(%s)", memberInfoOfInviteCode.getMemberType(), inviteCode, memberType.getCode()));
        }
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMemberAccount(phoneNumber);
        // 设置会员手机号
        memberInfo.setMobileNumber(phoneNumber);
        //设置密码
        memberInfo.setPassword(passwordEncoder.encode(password));
        //设置注册设备
        memberInfo.setRegisterDevice(UserAgentUtil.getDeviceType(request.getHeader("user-agent")));
        //设置钱包地址
        memberInfo.setWalletAddress(CryptoWalletGenerator.generateWalletAddress());
        //设置注册ip
        memberInfo.setRegisterIp(IpUtil.getRealIP(request));
        //设置会员类型
        memberInfo.setMemberType(memberType.getCode());
        //设置随机昵称
        memberInfo.setNickname(NicknameGeneratorUtil.generateNickname());
        // 设置买入和卖出奖励比例
        setRewardRatio(memberInfo);

        // 设置备注
        memberInfo.setRemark(remark);

        // 设置会员等级
        memberInfo.setAntLevel(inviteLink.getLevel() + 1);

        boolean save = save(memberInfo);
        if (save) {
            this.antRelationsService.updateRelationsWhenInsert(inviteLink.getAntId(), memberInfo.getId());
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            registerRecordAsync.insertRegisterRecord(memberInfo.getId(), inviteCode, request);
            return RestResult.ok(memberInfolistPageDTO);
        }
        return RestResult.failed();
    }

    public void setRewardRatio(MemberInfo memberInfo) {
        TradeConfig tradeConfig = this.tradeConfigService.getById(1);
        if (Objects.isNull(tradeConfig)) {
            log.error("手机号注册处理失败: 交易配置为空");
            throw new BizException(ResultCode.TRADE_CONFIG_NOT_EXIST);
        }
        // 设置买入奖励比例
        memberInfo.setBuyBonusProportion(tradeConfig.getBuyRewardRatio());

        // 设置卖出奖励比例
        memberInfo.setSellBonusProportion(tradeConfig.getSellRewardRatio());
    }

    @Transactional(rollbackFor = Exception.class)
    public RestResult<MemberInfolistPageDTO> createSuperLevelMember(
            String phoneNumber,
            String password,
            String remark,
            MemberTypeEnum memberType,
            Boolean createInviteCode,
            HttpServletRequest request) {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMemberAccount(phoneNumber);
        // 设置会员手机号
        memberInfo.setMobileNumber(phoneNumber);
        //设置密码
        memberInfo.setPassword(passwordEncoder.encode(password));
        //设置注册设备
        memberInfo.setRegisterDevice(UserAgentUtil.getDeviceType(request.getHeader("user-agent")));
        //设置钱包地址
        memberInfo.setWalletAddress(CryptoWalletGenerator.generateWalletAddress());
        //设置注册ip
        memberInfo.setRegisterIp(IpUtil.getRealIP(request));
        //设置会员类型
        memberInfo.setMemberType(memberType.getCode());
        //设置随机昵称
        memberInfo.setNickname(NicknameGeneratorUtil.generateNickname());
        // 设置买入和卖出奖励比例
        setRewardRatio(memberInfo);

        String referrerCode = null;
        if (createInviteCode) {
            referrerCode = UniqueCodeGeneratorUtil.generateInvitationCode();
            memberInfo.setReferrerCode(referrerCode);
        }
        memberInfo.setRemark(remark);
        boolean save = this.save(memberInfo);
        if (save) {
            // 维护层级关系
            this.antRelationsService.updateRelationsWhenInsert(memberInfo.getId(), memberInfo.getId());
            if (createInviteCode) {
                // 维护邀请链接
                int insertInviteCode = this.inviteLinkMapper.insert(
                        InviteLink.builder()
                                .antId(memberInfo.getId())
                                .title("System-Invite-Code")
                                .inviteCode(referrerCode)
                                .defaultLink(0)
                                .treeFlag(memberInfo.getId())
                                .deleted(0)
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build()
                );
                if (insertInviteCode < 1) {
                    throw new RuntimeException("Invite Code create failed");
                }
            }
            MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
            BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
            return RestResult.ok(memberInfolistPageDTO);
        }
        return RestResult.failed();
    }

    private String getTodayRedisKey() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "announcement:viewed:" + datePart;
    }

    private void setExpireAtMidnight(String key) {
        LocalDateTime tomorrowMidnight = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        long secondsUntilMidnight = Duration.between(LocalDateTime.now(), tomorrowMidnight).getSeconds();
        redisTemplate.expire(key, Duration.ofSeconds(secondsUntilMidnight));
    }

    /**
     * 实名认证处理
     *
     * @param idCardNumber
     * @param realName
     * @param idCardImage
     * @param facePhoto
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult idenAuthenticationProcess(String idCardNumber, String realName, MultipartFile idCardImage, MultipartFile facePhoto) {


        Long currentUserId = UserContext.getCurrentUserId();

        //分布式锁key ar-wallet-idenAuthenticationProcess+会员id
        String key = "ar-wallet-idenAuthenticationProcess" + currentUserId;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取当前会员信息
                MemberInfo memberInfo = getById(currentUserId);

                if (memberInfo == null) {
                    log.error("实名认证处理失败: 获取会员信息失败");
                    return RestResult.failure(ResultCode.RELOGIN);
                }

                //校验该账号是否被实名认证过
                if (!MemberAuthenticationStatusEnum.UNAUTHENTICATED.getCode().equals(memberInfo.getAuthenticationStatus())) {
                    log.error("实名认证处理失败: 该账号已被实名认证, 会员账号: {}, idCardNumber: {}, realName: {}", memberInfo.getMemberAccount(), idCardNumber, realName);
                    return RestResult.failure(ResultCode.ACCOUNT_ALREADY_VERIFIED);
                }

                //判断此实名信息是否被别的账号使用
                MemberInfo memberByRealName = getMemberByCardNumber(idCardNumber);
                if (memberByRealName != null) {
                    log.error("实名认证处理失败: 此实名信息已被使用, 会员账号: {}, idCardNumber: {}, realName: {}", memberInfo.getMemberAccount(), idCardNumber, realName);
                    return RestResult.failure(ResultCode.VERIFICATION_INFO_ALREADY_USED);
                }

                //校验文件合法性
                RestResult validateFile = FileUtil.validateFile(idCardImage, arProperty.getMaxImageFileSize(), "image");
                RestResult validateFile2 = FileUtil.validateFile(facePhoto, arProperty.getMaxImageFileSize(), "image");
                if (validateFile != null || validateFile2 != null) {
                    log.error("实名认证处理失败 文件校验失败 会员信息: {}, 错误信息: {}", memberInfo, validateFile.getMsg());
                    return validateFile;
                }

                String appEnv = arProperty.getAppEnv();
                boolean isTestEnv = "sit".equals(appEnv) || "dev".equals(appEnv);

                IdAnalyzerStatus idAnalyzerStatus = null;

                if (!isTestEnv) {
                    //线上环境 校验实名认证信息是否正确

                    //实名认证接口每日最大请求次数
                    Integer maxRealNameAuthRequestsPerDay = arProperty.getMaxRealNameAuthRequestsPerDay();

                    //查看是否达到了实名认证每次请求次数限制
                    if (!canRequestRealNameAuth(memberInfo.getId(), maxRealNameAuthRequestsPerDay)) {
                        //该会员已经达到每日次数限制
                        log.error("实名认证处理失败 该会员已达到每日请求实名认证接口次数限制 会员信息: {}, 实名认证接口每日最大请求次数: {}", memberInfo, maxRealNameAuthRequestsPerDay);
                        return RestResult.failure(ResultCode.DAILY_REAL_NAME_AUTH_LIMIT_REACHED);
                    }

                    //请求实名认证接口...
                    idAnalyzerStatus = idAnalyzerClient.exmainIdentity(encodeMultipartFileToBase64(idCardImage), null, encodeMultipartFileToBase64(facePhoto), realName, idCardNumber);
                } else {
                    //测试环境 默认通过
                    idAnalyzerStatus = new IdAnalyzerStatus();
                    idAnalyzerStatus.setStatus(true);
                }


                //验证成功  将会员的信息更新到会员表
                if (idAnalyzerStatus != null && idAnalyzerStatus.getStatus() == true) {
//                if (true) {
                    //认证成功

                    //将证件图片信息上传到 阿里云存储服务器
                    String fileName = ossService.uploadFile(idCardImage);

                    //将人脸图片信息上传到 阿里云存储服务器
                    String facePhotoFileName = ossService.uploadFile(facePhoto);

                    if (fileName == null || facePhotoFileName == null) {
                        log.error("实名认证处理失败: 上传文件至阿里云失败, 会员信息: {}", memberInfo);
                        return RestResult.failure(ResultCode.FILE_UPLOAD_FAILED);
                    }

                    if (idenAuthentication(memberInfo.getId(), realName, idCardNumber, fileName, facePhotoFileName)) {
                        log.info("实名认证处理成功: 会员账号: {}, idCardNumber: {}, realName: {}", memberInfo.getMemberAccount(), idCardNumber, realName);

                        //获取任务信息 (不管有没有开启实名认证任务 都要将任务完成, 没开启实名认证只是不能领取奖励)
                        TaskManager taskManager = taskManagerService.getTaskDetailsByType(RewardTaskTypeEnum.REAL_AUTH.getCode());

                        if (taskManager != null) {
                            //完成实名认证任务
                            if (!memberTaskStatusService.completeOnceTask(memberInfo, taskManager)) {
                                log.info("实名认证处理失败, 会员信息: {}, 任务信息: {}", memberInfo, taskManager);
                                //完成实名认证任务失败, 手动抛出异常进行回滚
                                throw new RuntimeException();
                            } else {
                                log.info("实名认证处理成功, 会员信息: {}, 任务信息: {}", memberInfo, taskManager);
                            }
                        } else {
                            log.info("实名认证处理成功, 实名认证活动未开启, 会员信息: {}", memberInfo);
                        }
                        return RestResult.ok();
                    }
                }
            }
        } catch (Exception e) {
            log.info("实名认证处理失败: 实名认证不通过: 会员id:{}, idCardNumber: {}, realName: {}, e: {}", currentUserId, idCardNumber, realName, e);

            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return RestResult.failure(ResultCode.REAL_NAME_VERIFICATION_FAILED);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        log.info("实名认证处理失败: 实名认证不通过: 会员id:{}, idCardNumber: {}, realName: {}", currentUserId, idCardNumber, realName);

        return RestResult.failure(ResultCode.REAL_NAME_VERIFICATION_FAILED);
    }


    /**
     * 查看实名认证接口是否达到了每日限制次数
     *
     * @return {@link Boolean}
     */
    public boolean canRequestRealNameAuth(Long memberId, Integer maxRealNameAuthRequestsPerDay) {

        String key = buildRedisKeyForMember(memberId);
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        // 直接尝试增加请求次数，Redis会自动将不存在的key视作0处理
        Long currentRequests = ops.increment(key);

        // 检查当前请求次数是否超过限制
        if (currentRequests != null && currentRequests > maxRealNameAuthRequestsPerDay) {
            // 超过限制，确保次数正确（因为increment会增加次数）
            ops.decrement(key);
            return false;
        }

        // 如果是新键（即当前请求次数为1），设置到期时间至次日凌晨
        if (currentRequests != null && currentRequests == 1) {
            setExpirationAtMidnight(key);
        }
        return true;
    }


    private String buildRedisKeyForMember(Long memberId) {
        LocalDate today = LocalDate.now();
        return String.format("auth_request:%d:%s", memberId, today);
    }


    /**
     * 设置键的过期时间为当天午夜
     *
     * @param key
     */
    public void setExpirationAtMidnight(String key) {
        long ttl = calculateSecondsUntilMidnight();
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
    }

    /**
     * 计算当前时间至午夜的秒数
     *
     * @return long
     */
    public long calculateSecondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        return java.time.Duration.between(now, midnight).getSeconds();
    }


    public String encodeMultipartFileToBase64(MultipartFile file) {
        String base64Encoded = "";
        try {
            // 获取文件的字节数组
            byte[] bytes = file.getBytes();
            // 使用 Base64 编码器将字节数组转换为 Base64 编码的字符串
            base64Encoded = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("实名认证, 将图片base64编码错误, e: {}", e);
            e.printStackTrace();
        }
        return base64Encoded;
    }

    /**
     * 发送短信验证码
     *
     * @param sendSmsCodeReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult sendSmsCode(SendSmsCodeReq sendSmsCodeReq, HttpServletRequest request) {
        String appEnv = arProperty.getAppEnv();
        boolean isTestEnv = "sit".equals(appEnv) || "dev".equals(appEnv);

        String realIP = IpUtil.getRealIP(request);

        String ipKey = "sms:limit:" + realIP;

        // 尝试获取当前IP的发送计数
        Long sendCountIp = redisTemplate.opsForValue().increment(ipKey, 1);

        // 首次设置过期时间，如果键已存在，则此操作不会重置过期时间
        if (sendCountIp == 1) {
            redisTemplate.expire(ipKey, 5, TimeUnit.MINUTES); // 设置5分钟过期
        }

        if (!isTestEnv && sendCountIp > 5) { // 每5分钟限制5次
            log.info("同一个IP发送短信验证码频繁, ip: {}, 手机号: {}", realIP, sendSmsCodeReq.getMobileNumber());
            long retryAfterSeconds = redisTemplate.getExpire(ipKey, TimeUnit.SECONDS);
            return RestResult.failure(ResultCode.SEND_VERIFICATION_CODE_FREQUENTLY, "Verification codes are sent frequently, please try again after" + retryAfterSeconds + "seconds.");
        }

        //获取redis 大key
        BoundHashOperations hashKey = redisTemplate.boundHashOps(arProperty.getSmsCodePrefix() + sendSmsCodeReq.getMobileNumber());

        //如果5分钟内 发了5次验证码 那么需要等到5分钟后 才能进行发送验证码
        String countStr = String.valueOf(hashKey.get("count"));

        if (countStr == null || "null".equals(countStr)) {
            countStr = "0";
        }

        int sendCount = Integer.parseInt(countStr);

        if (sendCount >= 5) {
            log.info("同一个手机号发送短信验证码频繁, 手机号: {}", sendSmsCodeReq.getMobileNumber());
            return RestResult.failure(ResultCode.SEND_VERIFICATION_CODE_FREQUENTLY, "Verification codes are sent frequently, please try again after" + redisTemplate.getExpire(arProperty.getSmsCodePrefix() + sendSmsCodeReq.getMobileNumber()) + "seconds.");
        }

        // 生成短信验证码
        String smsCode = isTestEnv ? "123456" : SmsCodeGeneratorUtil.generateCode();

        //短信内容
//        String smsContent = "[AR-Wallet] Your verification code is " + smsCode + ". For your account's security, please do not share this code with anyone.";

        //如果不是91开头的手机号 自动补上91前缀
        String telephone = StringUtil.startsWith91(sendSmsCodeReq.getMobileNumber()) ? sendSmsCodeReq.getMobileNumber() : "91" + sendSmsCodeReq.getMobileNumber();

        // 如果手机号长度是10位（即没有区号的情况），则在前面拼接上91
        telephone = telephone.length() == 10 ? "91" + telephone : telephone;
        //调用接口发送短信验证码...
        //直接发送内容 (这种方式怕有延迟  优先使用模板模式发送短信)
//        MessageStatus messageStatus = messageClient.sendMessage(telephone, smsContent);

        boolean sendSmsStatus = false;

        if (!isTestEnv) {

            // 选择短信运营商
            String providerId = arProperty.getSmsServiceProvider();

            switch (SmsProviderEnum.fromId(providerId)) {
                case BK:
                    // 不卡运营商
                    sendSmsStatus = smsService.sendBkSms(telephone, smsCode);
                    break;
                case SL:
                    // 颂量运营商
                    sendSmsStatus = smsService.sendSlSms(telephone, smsCode);
                    break;
                case SUBMAIL:
                    // SUBMAIL运营商
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", smsCode);
                    MessageStatus messageStatus = messageClient.sendMessage(telephone, arProperty.getSmsVerificationTemplateId(), jsonObject);
                    sendSmsStatus = messageStatus != null && messageStatus.getStatus();
                    break;
            }
        } else {
            // 在测试环境下模拟成功的MessageStatus
            sendSmsStatus = true;
        }

        if (sendSmsStatus || WalletConstant.TEST_MOBILE.contains(sendSmsCodeReq.getMobileNumber())) {
            //将手机号和短信验证码存入Redis
            hashKey.put("code", smsCode);
            hashKey.put("count", sendCount + 1);

            //设置验证码有效时间: 5分钟
            hashKey.expire(arProperty.getValidityDuration(), TimeUnit.MINUTES);

            log.info("发送短信验证码成功, 手机号: {}", sendSmsCodeReq.getMobileNumber());

            LocalDateTime now = LocalDateTime.now();

            //将验证码信息存入到数据库
            UserVerificationCodes userVerificationCodes = new UserVerificationCodes();

            //验证码
            userVerificationCodes.setVerificationCode(smsCode);

            //发送时间
            userVerificationCodes.setSendTime(now);

            //过期时间
            userVerificationCodes.setExpirationTime(now.plus(Duration.ofMinutes(arProperty.getValidityDuration())));

            //验证码类型（短信SMS、邮箱EMAIL）
            userVerificationCodes.setCodeType(VerificationCodeTypeEnum.SMS.name());

            //接收验证码的号码或邮箱
            userVerificationCodes.setReceiver(telephone);

            //IP地址
            userVerificationCodes.setIpAddress(realIP);

            //用户代理信息
            userVerificationCodes.setUserAgent(request.getHeader("user-agent"));

            //操作设备
            userVerificationCodes.setDevice(UserAgentUtil.getDeviceType(request.getHeader("user-agent")));

            userVerificationCodesService.save(userVerificationCodes);

            return RestResult.ok();
        }

        log.error("发送短信验证码失败, 手机号: {}", telephone);
        return RestResult.failure(ResultCode.SEND_VERIFICATION_CODE_FAILED);
    }

    /**
     * 发送邮箱验证码
     *
     * @param sendEmailCodeReq
     * @param request
     * @return {@link RestResult}
     */
    @Override
    public RestResult sendEmailCode(SendEmailCodeReq sendEmailCodeReq, HttpServletRequest request) {

        String realIP = IpUtil.getRealIP(request);

        //获取redis 大key
        BoundHashOperations hashKey = redisTemplate.boundHashOps(arProperty.getEmailCodePrefix() + sendEmailCodeReq.getEmailAccount());

        //如果5分钟内 发了5次验证码 那么需要等到5分钟后 才能进行发送验证码
        String countStr = String.valueOf(hashKey.get("count"));

        if (countStr == null || "null".equals(countStr)) {
            countStr = "0";
        }

        int sendCount = Integer.parseInt(countStr);

        if (sendCount >= 5) {
            log.info("发送邮箱验证码频繁: {}", JSON.toJSONString(sendEmailCodeReq));
            return RestResult.failure(ResultCode.SEND_VERIFICATION_CODE_FREQUENTLY, "Verification codes are sent frequently, please try again after" + redisTemplate.getExpire(arProperty.getEmailCodePrefix() + sendEmailCodeReq.getEmailAccount()) + "seconds.");
        }

        String appEnv = arProperty.getAppEnv();
        boolean isTestEnv = "sit".equals(appEnv) || "dev".equals(appEnv);

        //生成邮箱验证码
        // 生成短信验证码
        String emailCode = isTestEnv ? "123456" : SmsCodeGeneratorUtil.generateCode();

        Boolean sendSimpleMessage;

        //调用接口发送邮箱验证码...
        if (!isTestEnv) {
            sendSimpleMessage = emailService.sendMimeMessage(sendEmailCodeReq.getEmailAccount(), emailCode);
        } else {
            sendSimpleMessage = true;
        }

        if (!sendSimpleMessage) {
            log.error("邮箱验证码发送失败: sendEmailCodeReq: {}", sendEmailCodeReq);
            return RestResult.failure(ResultCode.SEND_VERIFICATION_CODE_FAILED);
        }

        //将邮箱号和验证码存入Redis
        hashKey.put("code", emailCode);
        hashKey.put("count", sendCount + 1);

        //设置验证码有效时间: 5分钟
        hashKey.expire(arProperty.getValidityDuration(), TimeUnit.MINUTES);


        LocalDateTime now = LocalDateTime.now();

        //将验证码信息存入到数据库
        UserVerificationCodes userVerificationCodes = new UserVerificationCodes();

        //验证码
        userVerificationCodes.setVerificationCode(emailCode);

        //发送时间
        userVerificationCodes.setSendTime(now);

        //过期时间
        userVerificationCodes.setExpirationTime(now.plus(Duration.ofMinutes(arProperty.getValidityDuration())));

        //验证码类型（短信SMS、邮箱EMAIL）
        userVerificationCodes.setCodeType(VerificationCodeTypeEnum.EMAIL.name());

        //接收验证码的号码或邮箱
        userVerificationCodes.setReceiver(sendEmailCodeReq.getEmailAccount());

        //IP地址
        userVerificationCodes.setIpAddress(realIP);

        //用户代理信息
        userVerificationCodes.setUserAgent(request.getHeader("user-agent"));

        //操作设备
        userVerificationCodes.setDevice(UserAgentUtil.getDeviceType(request.getHeader("user-agent")));

        userVerificationCodesService.save(userVerificationCodes);

        log.info("邮箱验证码发送成功: {}", sendEmailCodeReq);
        return RestResult.ok();
    }

    /**
     * 更换手机号处理
     *
     * @param verifySmsCodeReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult updatePhoneNumberProcess(VerifySmsCodeReq verifySmsCodeReq) {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("更换手机号处理失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //校验该手机号是否已被使用
        MemberInfo memberByPhoneNumber = getMemberByPhoneNumber(verifySmsCodeReq.getMobileNumber());
        if (memberByPhoneNumber != null) {

            //判断使用该手机号的用户是否是当前用户, 因为有些用户是从后台注册的, 会员账号就是手机号, 但是手机号是空
            String memberByPhoneNumberId = String.valueOf(memberByPhoneNumber.getId());

            //当前会员id
            String memberId = String.valueOf(memberInfo.getId());

            //如果不是同一个会员 使用相同的手机号 才进行驳回
            if (!memberByPhoneNumberId.equals(memberId)) {
                log.error("更换手机号处理失败: 该手机号已被使用 req: {}, 会员账号: {}", verifySmsCodeReq, memberInfo.getMemberAccount());
                return RestResult.failure(ResultCode.MOBILE_NUMBER_ALREADY_USED);
            }
        }

        //验证手机短信验证码
        if (memberNotificationService.validateSmsCode(verifySmsCodeReq)) {
            log.info("更换手机号处理 验证码验证成功: {}, 会员账号: {}", verifySmsCodeReq, memberInfo.getMemberAccount());
            //验证成功 更新会员手机号

            Boolean b = updatePhoneNumber(String.valueOf(memberInfo.getId()), verifySmsCodeReq.getMobileNumber());

            if (b) {
                log.info("更换手机号处理成功 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), verifySmsCodeReq, b);
                return RestResult.ok();
            } else {
                log.error("更换手机号处理失败 sql执行失败 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), verifySmsCodeReq, b);
            }
        }
        log.error("更换手机号处理失败: 验证码错误 req: {}, 会员信息: {}", verifySmsCodeReq, memberInfo);
        return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
    }

    /**
     * 更新邮箱号处理
     *
     * @param bindEmailReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult updateEmailProcess(BindEmailReq bindEmailReq) {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("更新邮箱号处理失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //判断邮箱账号是否被使用
        if (getMemberByEmailAccount(bindEmailReq.getEmailAccount()) != null) {
            log.error("更新邮箱号处理 失败: 该邮箱账号已被使用 req: {}, 会员信息: {}", bindEmailReq, memberInfo);
            return RestResult.failure(ResultCode.EMAIL_ALREADY_USED);
        }

        //校验验证码是否正确
        if (memberNotificationService.validateEmailCode(bindEmailReq, memberInfo)) {
            //验证成功 更新会员邮箱号
            Boolean b = updateEmail(String.valueOf(memberInfo.getId()), bindEmailReq.getEmailAccount());

            if (b) {
                log.info("更新邮箱号处理成功 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), bindEmailReq, b);
                return RestResult.ok();
            } else {
                log.error("更新邮箱号处理失败 sql执行失败 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), bindEmailReq, b);
            }

            log.info("更新邮箱号处理 成功: {}, 会员账号: {}", bindEmailReq, memberInfo.getMemberAccount());
            return RestResult.ok();
        }
        log.error("更新邮箱号处理 失败: 验证码错误 req: {}, 会员信息: {}", bindEmailReq, memberInfo);
        return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
    }


    /**
     * 手机号注册处理
     *
     * @param phoneSignUpReq
     * @param request
     * @return {@link RestResult}<{@link PhoneSignUpVo}>
     */
    @Override
    @Transactional
    public GenerateTokenForWallertDTO phoneSignUp(PhoneSignUpReq phoneSignUpReq, HttpServletRequest request) {

        //分布式锁key six-nine-pay-phoneSignUp+手机号
        String key = "six-nine-pay-phoneSignUp" + phoneSignUpReq.getMobileNumber();
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                GenerateTokenForWallertDTO generateTokenForWallertDTO = new GenerateTokenForWallertDTO();

                String requestIp = IpUtil.getRealIP(request);

                log.info("手机号注册处理, 手机号: {}, 验证码: {}, 邀请码: {}", phoneSignUpReq.getMobileNumber(), phoneSignUpReq.getVerificationCode(), phoneSignUpReq.getReferrerCode());

                // 判断手机号是否被注册
                if (getMemberByPhoneNumber(phoneSignUpReq.getMobileNumber()) != null) {
                    log.error("手机号注册处理失败: 该手机号已被注册, 手机号: {}, 验证码: {}, 邀请码: {}", phoneSignUpReq.getMobileNumber(), phoneSignUpReq.getVerificationCode(), phoneSignUpReq.getReferrerCode());

                    generateTokenForWallertDTO.setResultCode(ResultCode.MOBILE_ALREADY_REGISTERED);
                    return generateTokenForWallertDTO;
                }

                // 校验验证码
                if (!this.validateCodeService.validate(phoneSignUpReq.getMobileNumber(), phoneSignUpReq.getVerificationCode())) {
                    log.error("手机号注册处理失败: 验证码错误, 手机号: {}, 验证码: {}, 邀请码: {}", phoneSignUpReq.getMobileNumber(), phoneSignUpReq.getVerificationCode(), phoneSignUpReq.getReferrerCode());
                    generateTokenForWallertDTO.setResultCode(ResultCode.VERIFICATION_CODE_ERROR);
                    return generateTokenForWallertDTO;
                }

                MemberInfo memberInfo = new MemberInfo();

                // 校验邀请码
                if (StringUtils.isBlank(phoneSignUpReq.getReferrerCode())) {
                    log.error("手机号注册处理失败: 邀请码不能为空, 手机号: {}, 验证码: {}, 邀请码: {}", phoneSignUpReq.getMobileNumber(), phoneSignUpReq.getVerificationCode(), phoneSignUpReq.getReferrerCode());
                    generateTokenForWallertDTO.setResultCode(ResultCode.INVITATION_CODE_INVALID);
                    return generateTokenForWallertDTO;
                }
                InviteLink inviteLink = this.inviteLinkMapper.selectOne(
                        new QueryWrapper<InviteLink>()
                                .lambda()
                                .eq(InviteLink::getInviteCode, phoneSignUpReq.getReferrerCode())
                );
                if (Objects.isNull(inviteLink)) {
                    log.error("手机号注册处理失败: 邀请码不存在, 手机号: {}, 验证码: {}, 邀请码: {}", phoneSignUpReq.getMobileNumber(), phoneSignUpReq.getVerificationCode(), phoneSignUpReq.getReferrerCode());
                    generateTokenForWallertDTO.setResultCode(ResultCode.INVITATION_CODE_INVALID);
                    return generateTokenForWallertDTO;
                }

                // 校验是否内部邀请码是否对应内部用户
                MemberInfo memberInfoOfInviteCode = this.getById(inviteLink.getAntId());
                if (Objects.isNull(memberInfoOfInviteCode)) {
                    log.error("手机号注册处理失败: 邀请码对应的用户不存在, 邀请码: {}", phoneSignUpReq.getReferrerCode());
                    generateTokenForWallertDTO.setResultCode(USER_NOT_EXIST);
                    return generateTokenForWallertDTO;
                }
                if (MemberTypeEnum.INTERNAL_MERCHANT_MEMBER.getCode().equals(memberInfoOfInviteCode.getMemberType())) {
                    log.error("手机号注册处理失败: 内部邀请码, 邀请码: {}", phoneSignUpReq.getReferrerCode());
                    generateTokenForWallertDTO.setResultCode(ResultCode.INVITATION_CODE_INVALID);
                    return generateTokenForWallertDTO;
                }

                //设置上级邀请码
                memberInfo.setReferrerCode(phoneSignUpReq.getReferrerCode());
                // 设置买入和卖出奖励比例
                setRewardRatio(memberInfo);
                //设置会员账号
                memberInfo.setMemberAccount(phoneSignUpReq.getMobileNumber());
                //设置手机号
                memberInfo.setMobileNumber(phoneSignUpReq.getMobileNumber());
                //设置密码
                memberInfo.setPassword(passwordEncoder.encode(phoneSignUpReq.getPassword()));
                //设置会员类型 (69Pay会员)
                memberInfo.setMemberType(MemberTypeEnum.SIX_NINE_MEMBER.getCode());
                //设置注册设备
                memberInfo.setRegisterDevice(UserAgentUtil.getDeviceType(request.getHeader("user-agent")));
                //设置注册ip
                memberInfo.setRegisterIp(IpUtil.getRealIP(request));
                //设置钱包地址
                memberInfo.setWalletAddress(CryptoWalletGenerator.generateWalletAddress());
                //设置随机昵称
                memberInfo.setNickname(NicknameGeneratorUtil.generateNickname());
                //设置首次登录ip
                memberInfo.setFirstLoginIp(requestIp);
                //设置首次登录时间
                memberInfo.setFirstLoginTime(LocalDateTime.now());
                // 设置会员层级
                memberInfo.setAntLevel(inviteLink.getLevel() + 1);

                boolean save = save(memberInfo);
                if (save) {
                    memberInfo.setMemberId(String.valueOf(memberInfo.getId()));
                    boolean update = updateById(memberInfo);
                    this.antRelationsService.updateRelationsWhenInsert(inviteLink.getAntId(), memberInfo.getId());
                    log.info("手机号注册成功 会员信息: {}, sql执行结果: {}", memberInfo, update);
                    generateTokenForWallertDTO.setMemberId(String.valueOf(memberInfo.getId()));
                    generateTokenForWallertDTO.setRequestIp(requestIp);
                    generateTokenForWallertDTO.setMobileNumber(phoneSignUpReq.getMobileNumber());
                    generateTokenForWallertDTO.setMemberAccount(memberInfo.getMemberAccount());
                    // 异步添加注册记录
                    this.registerRecordAsync.insertRegisterRecord(memberInfo.getId(), phoneSignUpReq.getReferrerCode(), request);
                    return generateTokenForWallertDTO;
                }
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("手机号注册失败, phoneSignUpReq: {}, e: ", phoneSignUpReq, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return null;
    }


    /**
     * 生成登录token
     *
     * @param generateTokenForWallertDTO
     * @return {@link RestResult}<{@link PhoneSignUpVo}>
     */
    public RestResult<PhoneSignUpVo> generateTokenForWallet(GenerateTokenForWallertDTO generateTokenForWallertDTO) {

        if (generateTokenForWallertDTO.getResultCode() != null) {
            return RestResult.failure(generateTokenForWallertDTO.getResultCode());
        }

        //生成登录token

        try {

            //加密用户名
            byte[] decodedKey = Base64.getDecoder().decode("wz+glqDb2YceJ3piABkWig==");
            SecretKeySpec reqKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            HashMap<String, String> reqMap = new HashMap<>();
            reqMap.put("data", RsaUtil.encryptData(generateTokenForWallertDTO.getMemberAccount(), reqKey));

            String res = RequestUtil.get("http://127.0.0.1:20001/oauth/generateTokenForWallet", reqMap, generateTokenForWallertDTO.getRequestIp());

            if (res == null) {
                log.error("钱包注册自动登录失败, 获取AES密钥和token失败: res = null, 会员id: {}, 请求ip: {}, 手机号: {}", generateTokenForWallertDTO.getMemberId(), generateTokenForWallertDTO.getRequestIp(), generateTokenForWallertDTO.getMobileNumber());
                return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
            }

            //解密数据
            //从redis里面获取 AES密钥 和token
            JSONObject tokenAndKey = retrieveTokenAndKey(generateTokenForWallertDTO.getMemberAccount());

            if (tokenAndKey == null) {
                log.error("钱包注册自动登录失败, 获取AES密钥和token失败: tokenAndKey = null, 请求ip: {}, 手机号: {}", generateTokenForWallertDTO.getRequestIp(), generateTokenForWallertDTO.getMobileNumber());
                return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
            }

            // 2. 使用AES密钥解密数据
            String token = RsaUtil.decryptData((String) tokenAndKey.get("encryptedData"), convertStringToAESKey((String) tokenAndKey.get("aesKey")));
            String refreshToken = RsaUtil.decryptData((String) tokenAndKey.get("encryptedData2"), convertStringToAESKey((String) tokenAndKey.get("aesKey")));


            if (StringUtils.isEmpty(token)) {
                log.error("钱包注册自动登录失败, token为null: 请求ip: {}, 手机号: {}", generateTokenForWallertDTO.getRequestIp(), generateTokenForWallertDTO.getMobileNumber());
                return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
            }

            if (StringUtils.isEmpty(refreshToken)) {
                log.error("钱包注册自动登录失败, refreshToken为null: 请求ip: {}, 手机号: {}", generateTokenForWallertDTO.getRequestIp(), generateTokenForWallertDTO.getMobileNumber());
                return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
            }

            PhoneSignUpVo phoneSignUpVo = new PhoneSignUpVo();

            //会员id
            phoneSignUpVo.setMemberId(generateTokenForWallertDTO.getMemberId());

            //钱包地址
            phoneSignUpVo.setWalletAccessUrl(arProperty.getWalletAccessUrl());

            //token
            phoneSignUpVo.setToken(token);

            //refreshToken
            phoneSignUpVo.setRefreshToken(refreshToken);


            //返回数据
            return RestResult.ok(phoneSignUpVo);
        } catch (Exception e) {
            return RestResult.ok();
        }
    }

    /**
     * 将字符串转为 AES密钥
     *
     * @param strKey
     * @return {@link SecretKey}
     */
    public SecretKey convertStringToAESKey(String strKey) {
        byte[] decodedKey = Base64.getDecoder().decode(strKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    /**
     * 从Redis获取 aes密钥和token
     *
     * @param username
     * @return {@link JSONObject}
     */
    public JSONObject retrieveTokenAndKey(String username) {
        // 从Redis获取数据
        String key = "GENERATETOKEN:" + username;
        return (JSONObject) redisTemplate.opsForValue().get(key);
    }

    /**
     * 检查邀请码是否存在
     *
     * @param invitationCode 邀请码
     * @return boolean 表示邀请码是否存在
     */
    public boolean existsByInvitationCode(String invitationCode) {
        if (StringUtils.isEmpty(invitationCode)) {
            return false;
        }
        InviteLink inviteLink = this.inviteLinkMapper.selectOne(
                new QueryWrapper<InviteLink>()
                        .lambda()
                        .eq(InviteLink::getInviteCode, invitationCode)
        );
        return Objects.nonNull(inviteLink) && invitationCode.equals(inviteLink.getInviteCode());
    }

    /**
     * 邮箱账号注册处理
     *
     * @param emailSignUpReq
     * @param request
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult emailSignUp(EmailSignUpReq emailSignUpReq, HttpServletRequest request) {

        log.info("邮箱账号注册处理 邮箱账号: {} 验证码: {}, 邀请码: {}", emailSignUpReq.getEmailAccount(), emailSignUpReq.getVerificationCode(), emailSignUpReq.getReferrerCode());

        //判断邮箱账号是否被注册
        if (getMemberByEmailAccount(emailSignUpReq.getEmailAccount()) != null) {
            log.error("邮箱账号注册处理失败 该邮箱账号已被注册: {}", emailSignUpReq.getEmailAccount());
            return RestResult.failure(ResultCode.EMAIL_ALREADY_REGISTERED);
        }

        //判断邮箱验证码是否正确
        if (!signUpValidateEmailCode(emailSignUpReq)) {
            log.error("邮箱账号注册处理失败 验证码错误: {}", emailSignUpReq.getEmailAccount());
            return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
        }

        //TODO 判断邀请码是否正确

        try {
            MemberInfo memberInfo = new MemberInfo();
            //设置会员账号
            memberInfo.setMemberAccount(emailSignUpReq.getEmailAccount());
            //设置邮箱号
            memberInfo.setEmailAccount(emailSignUpReq.getEmailAccount());
            //设置密码
            memberInfo.setPassword(passwordEncoder.encode(emailSignUpReq.getPassword()));
            //设置邀请码
            memberInfo.setReferrerCode(emailSignUpReq.getReferrerCode());
            //设置会员类型 (钱包会员)
//            memberInfo.setMemberType(MemberTypeEnum.WALLET_MEMBER.getCode());
            //设置注册设备
            memberInfo.setRegisterDevice(UserAgentUtil.getDeviceType(request.getHeader("user-agent")));
            //设置注册ip
            memberInfo.setRegisterIp(IpUtil.getRealIP(request));
            //设置钱包地址
            memberInfo.setWalletAddress(CryptoWalletGenerator.generateWalletAddress());
            //设置随机昵称
            memberInfo.setNickname(NicknameGeneratorUtil.generateNickname());

            boolean save = save(memberInfo);
            memberInfo.setMemberId(String.valueOf(memberInfo.getId()));
            boolean update = updateById(memberInfo);

            log.info("邮箱账号注册成功 会员信息: {}, sql执行结果: {}", memberInfo, update);

            return RestResult.ok();
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            log.error("邮箱账号注册失败 emailSignUpReq: {}, e: {}", emailSignUpReq, e);

            return RestResult.failure(ResultCode.REGISTRATION_FAILED);
        }
    }

    /**
     * 忘记密码处理
     *
     * @param resetPasswordReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult resetPasswordProcess(ResetPasswordReq resetPasswordReq) {

        log.info("忘记密码处理 会员账号: {}, 验证码: {}", resetPasswordReq.getMemberAccount(), resetPasswordReq.getVerificationCode());

        MemberAuthDTO memberAuthDTO = null;

        //判断会员输入的账号是邮箱还是手机号
        if (RegexUtil.validateEmail(resetPasswordReq.getMemberAccount())) {

            //判断是否存在该会员
            memberAuthDTO = getByUsername(resetPasswordReq.getMemberAccount());

            if (memberAuthDTO == null) {
                log.error("忘记密码处理失败: 账号输入有误: {}", resetPasswordReq);
                return RestResult.failure(ResultCode.ACCOUNT_INPUT_ERROR);
            }

            //邮箱
            EmailSignUpReq emailSignUpReq = new EmailSignUpReq();
            emailSignUpReq.setEmailAccount(resetPasswordReq.getMemberAccount());
            emailSignUpReq.setVerificationCode(resetPasswordReq.getVerificationCode());

            //判断邮箱验证码
            if (!signUpValidateEmailCode(emailSignUpReq)) {
                log.error("忘记密码处理失败: 验证码错误: {}, 会员信息: {}", resetPasswordReq, memberAuthDTO);
                return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
            }
        } else if (RegexUtil.validatePhoneNumber(resetPasswordReq.getMemberAccount())) {

            //判断是否存在该会员
            memberAuthDTO = getByUsername(resetPasswordReq.getMemberAccount());

            if (memberAuthDTO == null) {
                log.error("忘记密码处理失败: 账号输入有误: {}", resetPasswordReq);
                return RestResult.failure(ResultCode.ACCOUNT_INPUT_ERROR);
            }

            //校验手机验证码
            ValidateSmsCodeReq validateSmsCodeReq = new ValidateSmsCodeReq();
            validateSmsCodeReq.setMobileNumber(resetPasswordReq.getMemberAccount());
            validateSmsCodeReq.setVerificationCode(resetPasswordReq.getVerificationCode());

            if (!signUpValidateSmsCode(validateSmsCodeReq)) {
                log.error("忘记密码处理失败: 验证码错误: {}", resetPasswordReq);
                return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
            }

        } else {
            log.error("忘记密码处理失败: 账号输入有误: {}", resetPasswordReq);
            return RestResult.failure(ResultCode.ACCOUNT_INPUT_ERROR);
        }

        //重置会员登录密码
        resetPassword(memberAuthDTO.getUserId(), passwordEncoder.encode(resetPasswordReq.getPassword()));

        log.info("忘记密码处理 重置会员登录密码成功 会员账号: {}, 验证码: {}", resetPasswordReq.getMemberAccount(), resetPasswordReq.getVerificationCode());

        return RestResult.ok();
    }

    /**
     * 邮箱注册 or 忘记密码-验证码校验
     *
     * @param emailSignUpReq
     * @return {@link Boolean}
     */
    @Override
    public Boolean signUpValidateEmailCode(EmailSignUpReq emailSignUpReq) {

        //获取邮箱验证码 redis-key 前缀
        String emailCodePrefix = arProperty.getEmailCodePrefix();

        //只有一个邮箱号(会员之前没绑定邮箱号 或者会员提交的邮箱号和之前绑定的是一致)
        //判断key是否存在
        if (redisTemplate.opsForHash().hasKey(emailCodePrefix + emailSignUpReq.getEmailAccount(), "code")) {

            //获取redis验证码
            String code = (String) redisTemplate.boundHashOps(emailCodePrefix + emailSignUpReq.getEmailAccount()).get("code");

            //校验会员提交过来的验证码是否和redis存储的一致
            if (emailSignUpReq.getVerificationCode().equals(code)) {
                //验证码正确 将状态改为已校验
                BoundHashOperations hashKey = redisTemplate.boundHashOps(emailCodePrefix + emailSignUpReq.getEmailAccount());
                hashKey.put("verified", SignUtil.getMD5(emailSignUpReq.getVerificationCode(), 1, arProperty.getRedismd5key()));

                log.info("邮箱注册 or 忘记密码 验证码校验成功 邮箱账号: {}, 验证码: {}, redis验证码: {}", emailSignUpReq.getEmailAccount(), emailSignUpReq.getVerificationCode(), code);
                return true;
            } else {
                log.error("邮箱注册 or 忘记密码 验证码校验失败 邮箱账号: {}, 验证码: {}, redis验证码: {}", emailSignUpReq.getEmailAccount(), emailSignUpReq.getVerificationCode(), code);
            }
        } else {
            log.error("邮箱注册 or 忘记密码 验证码校验失败 邮箱账号: {}, 验证码: {}, redis验证码不存在", emailSignUpReq.getEmailAccount(), emailSignUpReq.getVerificationCode());
        }

        return false;
    }

    /**
     * 手机号注册 or 忘记密码-验证码校验
     *
     * @param validateSmsCodeReq
     * @return {@link Boolean}
     */
    @Override
    public Boolean signUpValidateSmsCode(ValidateSmsCodeReq validateSmsCodeReq) {

        log.info("一次性验证码校验, 手机号: {}, 验证码: {}", validateSmsCodeReq.getMobileNumber(), validateSmsCodeReq.getVerificationCode());

        //获取短信验证码 redis-key 前缀
        String smsCodePrefix = arProperty.getSmsCodePrefix();

        if (redisTemplate.opsForHash().hasKey(smsCodePrefix + validateSmsCodeReq.getMobileNumber(), "code")) {

            //获取redis验证码
            String code = (String) redisTemplate.boundHashOps(smsCodePrefix + validateSmsCodeReq.getMobileNumber()).get("code");

            //校验会员提交过来的验证码是否和redis存储的一致
            if (validateSmsCodeReq.getVerificationCode().equals(code)) {
                //验证码正确 将状态改为已校验
                BoundHashOperations hashKey = redisTemplate.boundHashOps(smsCodePrefix + validateSmsCodeReq.getMobileNumber());
                hashKey.put("verified", SignUtil.getMD5(validateSmsCodeReq.getVerificationCode(), 1, arProperty.getRedismd5key()));

                log.info("一次性验证码校验成功, 手机号: {}, 验证码: {}, redis验证码: {}", validateSmsCodeReq.getMobileNumber(), validateSmsCodeReq.getVerificationCode(), code.substring(0, 3) + "***");

                return Boolean.TRUE;
            } else {
                log.error("一次性验证码校验失败, 手机号: {}, 验证码: {}, redis验证码: {}", validateSmsCodeReq.getMobileNumber(), validateSmsCodeReq.getVerificationCode(), code.substring(0, 3) + "***");
            }
        } else {
            log.error("一次性验证码校验失败, 手机号: {}, 验证码: {}, redis不存在该手机号的验证码", validateSmsCodeReq.getMobileNumber(), validateSmsCodeReq.getVerificationCode());
        }

        return Boolean.FALSE;
    }

    /**
     * 获取当前会员信息
     *
     * @return {@link MemberInfo}
     */
    @Override
    public MemberInfo getMemberInfo() {

        Long currentUserId = UserContext.getCurrentUserId();

        if (currentUserId != null) {
            return lambdaQuery().eq(MemberInfo::getId, currentUserId).eq(MemberInfo::getDeleted, 0).one();
        }

//        log.error("获取当前会员信息失败: 会员id为null");
        return null;
    }

    /**
     * 设置头像
     *
     * @param updateAvatarReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult updateAvatar(UpdateAvatarReq updateAvatarReq) {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("设置头像失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        boolean update = lambdaUpdate().eq(MemberInfo::getId, memberInfo.getId()).set(MemberInfo::getAvatar, updateAvatarReq.getAvatar()).update();

        if (update) {
            log.info("设置头像成功 会员账号: {}, sql执行结果: {}", memberInfo.getMemberAccount(), update);
            return RestResult.ok();
        }

        log.error("设置头像失败 会员信息: {}, sql执行结果: {}", memberInfo, update);

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 设置昵称
     *
     * @param updateNicknameReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult updateNickname(UpdateNicknameReq updateNicknameReq) {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("设置昵称失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //对用户昵称进行HTML清洗
        updateNicknameReq.setNickname(JsoupUtil.clean(updateNicknameReq.getNickname()));

        boolean update = lambdaUpdate().eq(MemberInfo::getId, memberInfo.getId()).set(MemberInfo::getNickname, updateNicknameReq.getNickname()).update();

        if (update) {
            log.info("设置昵称成功 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), updateNicknameReq, update);
            return RestResult.ok();
        }

        log.error("设置昵称失败 会员信息: {}, req: {}, sql执行结果: {}", memberInfo, updateNicknameReq, update);

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 设置新支付密码
     *
     * @param newPaymentPasswordReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult setNewPaymentPassword(NewPaymentPasswordReq newPaymentPasswordReq) {

        Long currentUserId = UserContext.getCurrentUserId();

        //分布式锁key ar-wallet-buy+会员id
        String key = "ar-wallet-setNewPaymentPassword" + currentUserId;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取当前会员信息
                MemberInfo memberInfo = getById(currentUserId);

                if (memberInfo == null) {
                    log.error("设置新支付密码失败: 获取会员信息失败");
                    return RestResult.failure(ResultCode.RELOGIN);
                }

                if (StringUtils.isNotEmpty(memberInfo.getPaymentPassword())) {
                    //会员已经存在支付密码了 支付返回成功
                    log.error("设置新支付密码失败: 会员已经设置过支付密码了, 会员信息: {}", memberInfo);
                    return RestResult.ok();
                }

                // 校验短信验证码
                if (!this.validateCodeService.validate(memberInfo.getMobileNumber(), newPaymentPasswordReq.getVerificationCode())) {
                    log.error("手机号注册处理失败: 验证码错误, 手机号: {}, 验证码: {}", memberInfo.getMobileNumber(), newPaymentPasswordReq.getVerificationCode());
                    return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
                }

                //对支付密码提示语进行HTML清洗
//                newPaymentPasswordReq.setPaymentPasswordHint(JsoupUtil.clean(newPaymentPasswordReq.getPaymentPasswordHint()));

                //对支付密码进行加密存储
                newPaymentPasswordReq.setPaymentPassword(passwordEncoder.encode(newPaymentPasswordReq.getPaymentPassword()));

                boolean update = lambdaUpdate().eq(MemberInfo::getId, memberInfo.getId())
                        .set(MemberInfo::getPaymentPassword, newPaymentPasswordReq.getPaymentPassword())
//                        .set(MemberInfo::getPaymentPasswordHint, newPaymentPasswordReq.getPaymentPasswordHint())
                        .update();

                if (update) {
                    log.info("设置新支付密码成功 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), newPaymentPasswordReq, update);
                    return RestResult.ok();
                }

            }
        } catch (Exception e) {
            log.error("设置新支付密码失败 会员id: {}, req: {}, e: {}", currentUserId, newPaymentPasswordReq, e.getMessage());

            return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        log.error("设置新支付密码失败 会员id: {}, req: {}", currentUserId, newPaymentPasswordReq);

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 修改支付密码
     *
     * @param updatePaymentPasswordReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult updatePaymentPassword(UpdatePaymentPasswordReq updatePaymentPasswordReq) {

        Long currentUserId = UserContext.getCurrentUserId();

        //分布式锁key ar-wallet-buy+会员id
        String key = "ar-wallet-updatePaymentPassword" + currentUserId;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取当前会员信息
                MemberInfo memberInfo = getMemberInfo();

                if (memberInfo == null) {
                    log.error("修改支付密码失败: 获取会员信息失败");
                    return RestResult.failure(ResultCode.RELOGIN);
                }

                if (memberInfo.getPaymentPassword() == null) {
                    log.error("修改支付密码失败: 获取原支付密码失败");
                }

                //校验旧支付密码
                if (!passwordEncoder.matches(updatePaymentPasswordReq.getOldPaymentPassword(), memberInfo.getPaymentPassword())) {
                    log.error("修改支付密码失败: 旧支付密码错误: 会员信息: {}, req: {}", memberInfo, updatePaymentPasswordReq);
                    return RestResult.failure(ResultCode.PASSWORD_VERIFICATION_FAILED);
                }

                log.info("修改支付密码: 旧密码校验成功, 会员账号: {}", memberInfo.getMemberAccount());

                //更换会员支付密码
                boolean update = lambdaUpdate()
                        .eq(MemberInfo::getId, memberInfo.getId())
                        .set(MemberInfo::getPaymentPassword, passwordEncoder.encode(updatePaymentPasswordReq.getNewPaymentPassword()))
//                        .set(MemberInfo::getPaymentPasswordHint, updatePaymentPasswordReq.getPaymentPasswordHint())
                        .update();

                if (update) {
                    log.info("修改支付密码成功, 会员账号: {}", memberInfo.getMemberAccount());
                    return RestResult.ok();
                }
            }
        } catch (Exception e) {
            log.error("修改支付密码失败: 旧支付密码错误: 会员id: {}, req: {}, e: {}", currentUserId, updatePaymentPasswordReq, e);

            return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        log.error("修改支付密码失败: 旧支付密码错误: 会员id: {}, req: {}", currentUserId, updatePaymentPasswordReq);

        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    /**
     * 查看会员是否实名认证
     *
     * @return {@link RestResult}<{@link verificationStatusVo}>
     */
    @Override
    public RestResult<verificationStatusVo> verificationStatus() {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("查看会员是否实名认证失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        verificationStatusVo verificationStatusVo = new verificationStatusVo();
        //校验会员有没有实名认证
        if (MemberAuthenticationStatusEnum.AUTHENTICATED.getCode().equals(memberInfo.getAuthenticationStatus())
                || MemberAuthenticationStatusEnum.MANUAL_AUTHENTICATION.getCode().equals(memberInfo.getAuthenticationStatus())) {
            log.info("查看会员是否实名认证: 会员已实名认证 会员信息: {}", memberInfo);
            verificationStatusVo.setIsVerified(1);
        } else {
            log.info("查看会员是否实名认证: 会员未实名认证 会员信息: {}", memberInfo);
        }

        //会员是否有未完成的买入订单
        //查看当前会员是否有未完成的买入订单
        CollectionOrder collectionOrder = collectionOrderService.countActiveBuyOrders(String.valueOf(memberInfo.getId()));
        if (collectionOrder != null) {
            log.error("买入订单失败, 当前有未完成的订单: {}, 会员账号: {}", collectionOrder, memberInfo.getMemberAccount());

            //会员有未完成的买入订单
            verificationStatusVo.setHasUnfinishedBuyOrders(1);

            PendingOrderVo pendingOrderVo = new PendingOrderVo();
            pendingOrderVo.setPlatformOrder(collectionOrder.getPlatformOrder());
            pendingOrderVo.setOrderStatus(collectionOrder.getOrderStatus());

            //未完成的订单信息
            verificationStatusVo.setPendingOrderVo(pendingOrderVo);
        }

        //校验会员是否有买入权限
        if (!MemberPermissionCheckerUtil.hasPermission(memberGroupService.getAuthListById(memberInfo.getMemberGroup()), MemberPermissionEnum.BUY)) {
            log.error("买入下单失败, 当前会员所在分组没有买入权限, 会员账号: {}", memberInfo.getMemberAccount());

            //会员是否有买入权限
            verificationStatusVo.setHasBuyPermission(0);
        }

        //会员是否被禁止买入
        //检查当前会员是否处于买入冷却期
        if (!redisUtil.canMemberBuy(String.valueOf(memberInfo.getId()))) {

            //获取配置信息
            TradeConfig tradeConfig = tradeConfigService.getById(1);

            //剩余时间
            long memberBuyBlockedExpireTime = redisUtil.getMemberBuyBlockedExpireTime(String.valueOf(memberInfo.getId()));

            //会员处于冷却期 不能购买
            log.error("买入下单失败, 当前会员处于买入冷却期, 会员账号: {}, 禁止买入时间(时): {}, 剩余时间(秒): {}", memberInfo.getMemberAccount(), tradeConfig.getDisabledTime(), memberBuyBlockedExpireTime);

            DisableBuyingVo disableBuyingVo = new DisableBuyingVo();

            //获取会员被禁用的时间
            Integer memberBuyBlockRemainingTime = redisUtil.getMemberBuyBlockRemainingTime(String.valueOf(memberInfo.getId()));

            if (memberBuyBlockRemainingTime == null) {
                memberBuyBlockRemainingTime = tradeConfig.getDisabledTime();
            }

            //禁止买入小时数
            disableBuyingVo.setBuyDisableHours(memberBuyBlockRemainingTime);
            //剩余时间(秒)
            disableBuyingVo.setRemainingSeconds(memberBuyBlockedExpireTime);

            //是否被禁用
            verificationStatusVo.setIsBuyBanned(1);

            //会员禁止买入信息
            verificationStatusVo.setDisableBuyingVo(disableBuyingVo);
        }

        if (controlSwitchService.isSwitchEnabled(SwitchIdEnum.REAL_NAME_VERIFICATION.getSwitchId())) {
            verificationStatusVo.setIsRealNameAuthEnabled(1);
        }

        return RestResult.ok(verificationStatusVo);
    }

    /**
     * 忘记支付密码
     *
     * @param resetPaymentPasswordReq
     * @return {@link RestResult}
     */
    @Override
    public RestResult resetPaymentPassword(ResetPaymentPasswordReq resetPaymentPasswordReq) {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("忘记支付密码处理失败: 获取会员信息失败, req: {}", resetPaymentPasswordReq);
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //对支付密码提示语进行HTML清洗
//        resetPaymentPasswordReq.setPaymentPasswordHint(JsoupUtil.clean(resetPaymentPasswordReq.getPaymentPasswordHint()));

        //校验验证码是否正确
        //校验手机验证码
        if (!this.validateCodeService.validate(memberInfo.getMobileNumber(), resetPaymentPasswordReq.getVerificationCode())) {
            log.error("忘记密码处理失败: 验证码错误: {}, 会员信息: {}", resetPaymentPasswordReq, memberInfo);
            return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
        }
//        ValidateSmsCodeReq validateSmsCodeReq = new ValidateSmsCodeReq();
//        validateSmsCodeReq.setMobileNumber(memberInfo.getMobileNumber());
//        validateSmsCodeReq.setVerificationCode(resetPaymentPasswordReq.getVerificationCode());
//
//        if (!signUpValidateSmsCode(validateSmsCodeReq)) {
//            log.error("忘记密码处理失败: 验证码错误: {}, 会员信息: {}", resetPaymentPasswordReq, memberInfo);
//            return RestResult.failure(ResultCode.VERIFICATION_CODE_ERROR);
//        }

        log.info("忘记支付密码处理 验证码校验成功, 会员账号: {}, req: {}", memberInfo, resetPaymentPasswordReq);

        //重置会员支付密码
        boolean update = lambdaUpdate()
                .eq(MemberInfo::getId, memberInfo.getId())
                .set(MemberInfo::getPaymentPassword, passwordEncoder.encode(resetPaymentPasswordReq.getPaymentPassword()))
                .update();
//                .set(MemberInfo::getPaymentPasswordHint, resetPaymentPasswordReq.getPaymentPasswordHint()).update();

        if (update) {
            log.info("忘记支付密码处理成功 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), resetPaymentPasswordReq, update);
            return RestResult.ok();
        }

        log.error("忘记支付密码处理失败 会员账号: {}, req: {}, sql执行结果: {}", memberInfo.getMemberAccount(), resetPaymentPasswordReq, update);
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 更新会员买入统计信息(添加)
     *
     * @param memberId
     * @return {@link Boolean}
     */
    @Override
    public Boolean updateAddBuyInfo(String memberId) {

        //更新会员累计买入次数 累计买入金额
        return lambdaUpdate()
                .eq(MemberInfo::getId, memberId)
                .setSql("total_buy_count = total_buy_count + 1")
                .update();

//        return lambdaUpdate()
//                .eq(MemberInfo::getId, memberId)
//                .setSql("total_buy_count = total_buy_count + 1")
//                .setSql("total_buy_amount = total_buy_amount + " + amount.toPlainString())
//                .update();
    }

    /**
     * 获取USDT汇率和支付类型
     *
     * @return {@link RestResult}<{@link UsdtCurrencyAndPayTypeVo}>
     */
    @Override
    public RestResult<UsdtCurrencyAndPayTypeVo> getUsdtCurrencyAndPayType() {

        //获取当前会员信息
        MemberInfo memberInfo = getMemberInfo();

        if (memberInfo == null) {
            log.error("获取USDT汇率和支付类型失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //获取配置信息
        TradeConfig tradeConfig = tradeConfigService.getById(1);

        UsdtCurrencyAndPayTypeVo usdtCurrencyAndPayTypeVo = new UsdtCurrencyAndPayTypeVo();

        //支付类型
        PaymentTypeVo paymentTypeVoUPI = new PaymentTypeVo();
        paymentTypeVoUPI.setPayType(PayTypeEnum.INDIAN_UPI.getCode());
        paymentTypeVoUPI.setName("UPI");

        PaymentTypeVo paymentTypeVoBank = new PaymentTypeVo();
        paymentTypeVoBank.setPayType(PayTypeEnum.INDIAN_CARD.getCode());
        paymentTypeVoBank.setName("BANK");

        ArrayList<PaymentTypeVo> paymentTypeVos = new ArrayList<>();
        paymentTypeVos.add(paymentTypeVoUPI);
        paymentTypeVos.add(paymentTypeVoBank);
        usdtCurrencyAndPayTypeVo.setPaymentTypeVo(paymentTypeVos);

        //usdt汇率
        usdtCurrencyAndPayTypeVo.setUsdtCurrency(tradeConfig.getUsdtCurrency());

        log.info("获取USDT汇率和支付类型成功: 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), usdtCurrencyAndPayTypeVo);

        return RestResult.ok(usdtCurrencyAndPayTypeVo);
    }

    /**
     * 更新被申诉人信息 (增加被申诉次数)
     *
     * @param memberId
     * @return {@link Boolean}
     */
    @Override
    public Boolean updateAddAppealCount(String memberId) {

        //更新被申诉人信息 (增加被申诉次数)
        return lambdaUpdate()
                .eq(MemberInfo::getId, memberId)
                .setSql("appeal_count = appeal_count + 1")
                .update();
    }

    /**
     * 查看该会员是否被注册 (会员id 手机号) (商户专用)
     *
     * @param memberId
     * @param mobileNumber
     */
    @Override
    public MemberInfo checkMemberRegistered(String memberId, String mobileNumber) {
        return lambdaQuery()
                .eq(MemberInfo::getMemberId, memberId)
                .or().eq(MemberInfo::getMobileNumber, mobileNumber)
                .or().eq(MemberInfo::getMemberAccount, mobileNumber)
                .last("LIMIT 1").one();
    }

    @Override
    public MemberInfo checkMemberRegistered(String memberId) {
        return lambdaQuery()
                .eq(MemberInfo::getMemberId, memberId)
                .last("LIMIT 1").one();
    }


    /**
     * 根据会员id获取会员信息 (商户专用)
     *
     * @param memberId
     * @return {@link MemberInfo}
     */
    @Override
    public MemberInfo getMemberInfoByMemberId(String memberId) {
        return lambdaQuery().eq(MemberInfo::getMemberId, memberId).one();
    }


    /**
     * 根据会员id获取会员信息 (商户专用)
     *
     * @param id
     * @return {@link MemberInfo}
     */
    @Override
    public MemberInfo getMemberInfoById(String id) {
        return lambdaQuery().eq(MemberInfo::getId, id).one();
    }


    /**
     * 设置首次登录信息
     *
     * @param memberId
     * @param firstLoginIp
     * @param firstLoginTime
     * @return {@link Boolean}
     */
    @Override
    public Boolean setFirstLoginInfo(Long memberId, String firstLoginIp, LocalDateTime firstLoginTime) {
        return lambdaUpdate()
                .eq(MemberInfo::getId, memberId)
                .set(MemberInfo::getFirstLoginIp, firstLoginIp)
                .set(MemberInfo::getFirstLoginTime, firstLoginTime)
                .update();
    }

    @Override
    public Boolean updateLastLoginInfo(Long memberId, String loginIp) {
        return lambdaUpdate()
                .eq(MemberInfo::getId, memberId)
                .set(MemberInfo::getLoginIp, loginIp)
                .update();
    }

    @Override
    public PageReturn<MemberRealNamelistPageDTO> realName(MemberInfoRealNameListReq req) {
        Page<MemberInfo> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<MemberInfo> lambdaQuery = lambdaQuery();
        //--动态查询 会员id
        if (!StringUtils.isEmpty(req.getMemberId())) {
            lambdaQuery.eq(MemberInfo::getId, req.getMemberId());
        }
        if (!StringUtils.isEmpty(req.getIdCardNumber())) {
            lambdaQuery.eq(MemberInfo::getIdCardNumber, req.getIdCardNumber());
        }
        if (ObjectUtils.isNotEmpty(req.getAuthenticationStatus())) {
            lambdaQuery.eq(MemberInfo::getAuthenticationStatus, req.getAuthenticationStatus());
        }
        lambdaQuery.orderByDesc(MemberInfo::getRealNameVerificationTime);
        // 倒序排序

        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MemberInfo> records = page.getRecords();

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<MemberRealNamelistPageDTO> list = new ArrayList<>();
        for (MemberInfo record : records) {
            MemberRealNamelistPageDTO memberInfolistPageDTO = new MemberRealNamelistPageDTO();
            BeanUtils.copyProperties(record, memberInfolistPageDTO);
            list.add(memberInfolistPageDTO);
        }
        return PageUtils.flush(page, list);
    }

    /**
     * 禁用会员，踢出登录
     *
     * @param memberId
     * @param operator
     * @return
     */
    @Override
    @Transactional
    public void disableMember(String memberId, String operator, String remark) {
        log.info("会员设置为禁用状态, memberId:{}", memberId);
        MemberInfo memberInfo = baseMapper.selectMemberInfoForUpdate(Long.valueOf(memberId));
        if (memberInfo == null) {
            log.info("会员设置为禁用状态, 会员不存在, memberId:{}", memberId);
            return;
        }
        if (MemberStatusEnum.DISABLE.getCode().equals(memberInfo.getStatus()) && BuyStatusEnum.DISABLE.getCode().equals(memberInfo.getBuyStatus()) && SellStatusEnum.DISABLE.getCode().equals(memberInfo.getSellStatus())) {
            log.info("会员设置为禁用状态, 会员已为禁用状态无需设置, memberId:{}", memberId);
            return;
        }
        boolean isOk = lambdaUpdate()
                .eq(MemberInfo::getId, memberId)
                .set(MemberInfo::getStatus, MemberStatusEnum.DISABLE.getCode())
                .set(MemberInfo::getBuyStatus, BuyStatusEnum.DISABLE.getCode())
                .set(MemberInfo::getSellStatus, SellStatusEnum.DISABLE.getCode())
                .update();
        log.info("会员设置为禁用状态, memberId:{}, 更新DB结果:{}", memberId, isOk);

        // 会员拉黑
        MemberBlack memberBlack = BeanUtil.toBean(memberInfo, MemberBlack.class);

        memberBlack.setOperator(StringUtils.isNotBlank(operator) ? operator : "系统");
        memberBlack.setOpTime(LocalDateTime.now());
        memberBlack.setMemberId(memberInfo.getId().toString());
        memberBlack.setSellStatus(Integer.parseInt(SellStatusEnum.DISABLE.getCode()));
        memberBlack.setStatus(Integer.parseInt(MemberStatusEnum.DISABLE.getCode()));
        memberBlack.setBuyStatus(Integer.parseInt(BuyStatusEnum.DISABLE.getCode()));
        memberBlack.setRemark(remark);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(memberInfo.getMemberId()) && org.apache.commons.lang3.StringUtils.isNotBlank(memberInfo.getMerchantCode()) &&
                memberInfo.getMemberId().contains(memberInfo.getMerchantCode())) {
            String externalMemberId = memberInfo.getMemberId().substring(memberInfo.getMerchantCode().length());
            memberBlack.setMerchantMemberId(externalMemberId);
        }
        Boolean blackResult = memberBlackService.addBlack(memberBlack);
        log.info("会员设置为禁用状态, memberId:{}, 添加到黑名单结果:{}, 推送踢人消息...", memberId, blackResult);

        NotifyLoginOutWebSocketService notifyLoginOutWebSocketService = SpringContextUtil.getBean(NotifyLoginOutWebSocketService.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", memberId);
        notifyLoginOutWebSocketService.AppointSending(jsonObject.toJSONString());

        String userName = memberInfo.getMemberAccount();
        CommonUtils.deleteToken(userName, redisUtils);
        redisUtils.del(SecurityConstants.REFRESH_TOKEN_PREFIX + userName);

        String jti = (String) redisUtils.get(SecurityConstants.LOGIN_USER_ID + userName);
        log.info("会员设置为禁用状态, memberId:{}, token删除完成, jti:{}", memberId, jti);
        if (StringUtils.isNotEmpty(jti)) {
            redisUtils.set(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti, null, 1000);
        }

    }

    /**
     * 根据登录IP获取会员ID列表
     *
     * @param ip
     * @return
     */
    @Override
    public List<String> getMembersByByLoginIp(String ip) {
        List<MemberInfo> memberList = lambdaQuery().eq(MemberInfo::getLoginIp, ip)
                .select(MemberInfo::getId)
                .list();
        if (CollectionUtils.isEmpty(memberList)) {
            return Collections.emptyList();
        }
        return memberList.stream().map(m -> String.valueOf(m.getId())).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<MemberInfolistPageDTO> updateCreditScore(MemberInfoCreditScoreReq req) {
        long memberId = req.getId();

        String key = "ar-wallet-updateCreditScore" + memberId;
        RLock lock = redissonUtil.getLock(key);
        // 获取交易信息
        TradeConfig tradeConfig = tradeConfigService.getById(1);
        boolean lockStatus = false;

        try {
            lockStatus = lock.tryLock(10, TimeUnit.SECONDS);

            if (lockStatus) {
                Integer eventType = req.getEventType();
                Integer tradeType = req.getTradeType();
                BigDecimal changeScore = req.getChangeScore();
                if (ObjectUtils.isEmpty(changeScore)) {
                    // 查询配置文件获取
                    CreditScoreConfig creditScoreConfig = creditScoreConfigService.getCreditScoreConfig(eventType, tradeType);
                    if (Objects.isNull(creditScoreConfig)) {
                        throw new BizException("获取信用分配置信息失败！memberId:" + memberId + ",eventType:" + eventType + ", tradeType:" + tradeType);
                    }
                    changeScore = creditScoreConfig.getScore();
                }

                // 更新分数信息
                MemberInfo memberInfo = getById(memberId);
                BigDecimal currentScore = memberInfo.getCreditScore();
                BigDecimal afterScore = currentScore.add(changeScore);
                if (afterScore.compareTo(new BigDecimal(240)) > 0) {
                    log.error("信誉分上限为240分！memberId:{},eventType:{}, tradeType:{}", memberId, eventType, tradeType);
                    return RestResult.failure(ResultCode.UPDATE_CREDIT_SCORE_FAILED);
                }
                if (afterScore.compareTo(new BigDecimal(0)) < 0) {
                    log.error("信誉分下限为0分！memberId:{},eventType:{}, tradeType:{}", memberId, eventType, tradeType);
                    return RestResult.failure(ResultCode.UPDATE_CREDIT_SCORE_FAILED);
                }
                memberInfo.setCreditScore(afterScore);
                // 交易开关判断
                BigDecimal tradeCreditScoreLimit = tradeConfig.getTradeCreditScoreLimit();
                // 信用分降低到阈值 并且为减分操作时 关闭交易开关
                if (afterScore.compareTo(tradeCreditScoreLimit) < 0 && currentScore.compareTo(afterScore) > 0) {
                    if (memberInfo.getBuyStatus().equals(BuyStatusEnum.ENABLE.getCode())) {
                        memberInfo.setBuyStatus(BuyStatusEnum.DISABLE.getCode());
                    }
                    if (memberInfo.getSellStatus().equals(SellStatusEnum.ENABLE.getCode())) {
                        memberInfo.setSellStatus(SellStatusEnum.DISABLE.getCode());
                    }
                }
                // 信用分回升到阈值
                if (afterScore.compareTo(tradeCreditScoreLimit) >= 0) {
                    if (memberInfo.getBuyStatus().equals(BuyStatusEnum.DISABLE.getCode())) {
                        memberInfo.setBuyStatus(BuyStatusEnum.ENABLE.getCode());
                    }
                    if (memberInfo.getSellStatus().equals(SellStatusEnum.DISABLE.getCode())) {
                        memberInfo.setSellStatus(SellStatusEnum.ENABLE.getCode());
                    }
                }
                boolean updateCreditScore = updateById(memberInfo);
                if (!updateCreditScore) {
                    throw new BizException("更新用户信誉分失败！memberId:" + memberId + ",eventType:" + eventType + ", tradeType:" + tradeType);
                }
                // 添加信用分变化记录
                boolean addLog = iCreditScoreLogsService.addLog(memberId, changeScore, currentScore, eventType, tradeType);
                if (!addLog) {
                    throw new BizException("添加信用分记录失败！memberId:" + memberId + ",eventType:" + eventType + ", tradeType:" + tradeType);
                }
                MemberInfolistPageDTO memberInfolistPageDTO = new MemberInfolistPageDTO();
                BeanUtils.copyProperties(memberInfo, memberInfolistPageDTO);
                // 发送变更等级的mq
                rabbitMQService.sendMemberUpgradeMessage(String.valueOf(memberId));
                return RestResult.ok(memberInfolistPageDTO);
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new BizException(e);
        } finally {
            if (lockStatus && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return RestResult.failure(ResultCode.UPDATE_CREDIT_SCORE_FAILED);
    }

    @Override
    public RestResult<MemberCreditScoreInfoDTO> getCreditScoreInfo(MemberCreditScoreInfoIdReq req) {
        long memberId = req.getId();
        MemberInfo memberInfo = getById(memberId);
        MemberCreditScoreInfoDTO memberCreditScoreInfoDTO = new MemberCreditScoreInfoDTO();
        memberCreditScoreInfoDTO.setMemberId(memberInfo.getId());
        memberCreditScoreInfoDTO.setCreditScore(memberInfo.getCreditScore());
        PageReturn<CreditScoreLogsDTO> creditScoreLogsDTOPageReturn = iCreditScoreLogsService.listPage(memberId, 3L);
        List<CreditScoreLogsDTO> list = creditScoreLogsDTOPageReturn.getList();
        memberCreditScoreInfoDTO.setLastCreditScoreLogList(list);
        return RestResult.ok(memberCreditScoreInfoDTO);
    }

    @Override
    public RestResult<MemberCreditScoreInfoDTO> getCreditScoreInfo() {
        Long memberId = UserContext.getCurrentUserId();
        MemberCreditScoreInfoIdReq req = new MemberCreditScoreInfoIdReq();
        req.setId(memberId);
        return getCreditScoreInfo(req);
    }


    @Override
    @Transactional
    public RestResult<List<CashBackOrderProcessDTO>> cashBackBatch(MemberInfoBatchIdReq req) {
        LambdaQueryChainWrapper<MemberInfo> queryChainWrapper = lambdaQuery().in(MemberInfo::getId, req.getIds()).last(" for update");
        List<MemberInfo> memberInfoList = baseMapper.selectList(queryChainWrapper.getWrapper());
        boolean flag = true;
        String failedMessage = "";
        List<CashBackOrderProcessDTO> resultList = new ArrayList<>();
        for (MemberInfo memberInfo : memberInfoList) {
            CashBackOrderProcessDTO cashBackProcess = checkCashBackOrder(memberInfo);
            resultList.add(cashBackProcess);
            if (!cashBackProcess.getResult()) {
                flag = false;
                failedMessage += cashBackProcess.getRemark() + ";";
                break;
            }
        }
        // 校验完毕添加账变和发送消息
        if (flag) {
            memberInfoList.forEach(this::cashBackProcess);
        }
        return flag ? RestResult.ok(resultList) : RestResult.result(ResultCode.CASH_BACK_PROCESS_MISTAKE.getCode(), failedMessage, resultList);
    }

    private CashBackOrderProcessDTO checkCashBackOrder(MemberInfo memberInfo) {
        CashBackOrderProcessDTO dto = new CashBackOrderProcessDTO();
        dto.setMemberId(memberInfo.getId());
        // 冻结余额
        BigDecimal currentMemberBalance = memberInfo.getBalance();
        // 检测用户商户类型
        if (ObjectUtils.isEmpty(memberInfo.getMerchantCode())) {
            log.info("批量退回：memberId:{} 商户会员信息不存在不予退回", memberInfo.getId());
            dto.setResult(false);
            dto.setRemark("用户:" + memberInfo.getId() + "商户会员信息不存在不予退回");
            return dto;
        }
        // 余额为零情况
        if (currentMemberBalance.compareTo(BigDecimal.ZERO) == 0) {
            log.info("批量退回：memberId:{} 余额为零不予退回", memberInfo.getId());
            dto.setResult(false);
            dto.setRemark("用户:" + memberInfo.getId() + "的余额为零");
            return dto;
        }
        // 退回金额不为零情况
        if (memberInfo.getCashBackFrozenAmount() != null && memberInfo.getCashBackFrozenAmount().compareTo(BigDecimal.ZERO) > 0) {
            log.info("批量退回：memberId:{} 已经存在处理中的退回订单", memberInfo.getId());
            dto.setResult(false);
            dto.setRemark("用户:" + memberInfo.getId() + "已经存在处理中的退回订单");
            return dto;
        }
        // 查询是否有未完成退回订单
        CashBackOrder cashBackOrderByMemberId = cashBackOrderService.getProcessingCashBackOrderByMemberId(memberInfo.getId());
        if (ObjectUtils.isNotEmpty(cashBackOrderByMemberId)) {
            log.info("批量退回：platformOrder:{} 已经存在处理中的退回订单", cashBackOrderByMemberId.getPlatformOrder());
            dto.setResult(false);
            dto.setRemark("用户:" + memberInfo.getId() + "已经存在处理中的退回订单");
            return dto;
        }
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCodeForUpdate(memberInfo.getMerchantCode());
        BigDecimal merchantBalance = merchantInfo.getBalance();
        if (merchantBalance.compareTo(currentMemberBalance) < 0) {
            log.info("批量退回：商户余额不足, 商户余额:{}, 会员余额:{}。", merchantBalance, currentMemberBalance);
            dto.setResult(false);
            dto.setRemark("用户:" + memberInfo.getId() + "商户余额不足");
            return dto;
        }
        dto.setResult(true);
        return dto;
    }

    @Transactional
    public void cashBackProcess(MemberInfo memberInfo) {
        try {
            BigDecimal currentMemberBalance = memberInfo.getBalance();
            String thOrder = orderNumberGenerator.generateOrderNo("TH");
            // 冻结会员余额
            memberInfo.setBalance(BigDecimal.ZERO);
            memberInfo.setCashBackFrozenAmount(currentMemberBalance);
            updateById(memberInfo);
            // 冻结商户余额
            MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCodeForUpdate(memberInfo.getMerchantCode());
            BigDecimal merchantBalance = merchantInfo.getBalance();
            if (merchantBalance.compareTo(currentMemberBalance) < 0) {
                throw new BizException("批量退回：商户余额不足, 商户余额:" + merchantBalance + ", 会员余额:" + currentMemberBalance);
            }
            BigDecimal afterMerchantBalance = merchantBalance.subtract(currentMemberBalance);
            BigDecimal merchantCashBackFrozenAmount = merchantInfo.getCashBackFrozenAmount() == null ? BigDecimal.ZERO : merchantInfo.getCashBackFrozenAmount();
            BigDecimal afterMerchantCashBackFrozenAmount = merchantCashBackFrozenAmount.add(currentMemberBalance);
            merchantInfo.setBalance(afterMerchantBalance);
            merchantInfo.setCashBackFrozenAmount(afterMerchantCashBackFrozenAmount);
            merchantInfoMapper.updateById(merchantInfo);
            // 生成退回订单
            boolean insertCashBackOrder = cashBackOrderService.generateOrder(thOrder, currentMemberBalance, String.valueOf(memberInfo.getId()), merchantInfo.getUsername(), merchantInfo.getCode(), memberInfo.getMemberId());
            if (!insertCashBackOrder) {
                throw new BizException("批量退回：生成退回订单失败, memberId:" + memberInfo.getId());
            }
            // 当前操作用户
            String currentUserName = UserContext.getCurrentUserName();
            // 加入队列
            rabbitMQService.sendCashBackOrderProcess(thOrder, currentUserName);
        } catch (BizException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error(e.getMessage());
        }
    }

    private void logoutMember(MemberInfo memberInfo) {
        String userName = memberInfo.getMemberAccount();
        CommonUtils.deleteToken(userName, redisUtils);
        redisUtils.del(SecurityConstants.REFRESH_TOKEN_PREFIX + userName);
        String jti = (String) redisUtils.get(SecurityConstants.LOGIN_USER_ID + userName);
        if (StringUtils.isNotEmpty(jti)) {
            redisUtils.set(SecurityConstants.BLACKLIST_TOKEN_PREFIX + jti, null, 1000);
        }
    }

    /**
     * 完成新手引导
     *
     * @param type 1:买入引导 2:卖出引导
     * @return
     */
    @Transactional
    @Override
    public RestResult finishNewUserGuide(Integer type) {
        if (type == null || (type != 1 && type != 2)) {
            return RestResult.failure(INVALID_NEW_USER_GUID_TYPE);
        }
        Long memberId = UserContext.getCurrentUserId();
        MemberInfo memberInfo = this.getBaseMapper().selectMemberInfoForUpdate(memberId);
        if (memberInfo == null) {
            log.error("完成新手引导, 未查询到用户:{}", memberId);
            return RestResult.failure(USER_NOT_EXIST);
        }
        String taskType = null;
        LambdaUpdateChainWrapper<MemberInfo> updateWrapper = lambdaUpdate().eq(MemberInfo::getId, memberId);
        if (type == 1) {
            taskType = STARTER_QUESTS_BUY.getCode();
            updateWrapper.set(MemberInfo::getBuyGuideStatus, 1);
        } else if (type == 2) {
            taskType = STARTER_QUESTS_SELL.getCode();
            updateWrapper.set(MemberInfo::getSellGuideStatus, 1);
        }
        boolean isOk = updateWrapper.update();
        log.info("完成新手引导, memberId:{}, type:{}, 更新DB结果:{}", memberId, type, isOk);
        if (isOk) {
            TaskManager taskManager = taskManagerService.getTaskDetailsByType(taskType);
            if (taskManager == null) {
                log.info("完成新手引导, 新手引导活动未开启:{}, memberId:{}", taskType, memberId);
                return RestResult.ok();
            }
            //完成实名认证任务
            if (!memberTaskStatusService.completeOnceTask(memberInfo, taskManager)) {
                log.info("完成新手引导, memberId: {}, 任务信息: {}", memberId, taskManager);
                //完成实名认证任务失败, 手动抛出异常进行回滚
                throw new RuntimeException();
            } else {
                log.info("完成新手引导, memberId: {}, 任务信息: {}", memberId, taskManager);
            }

        }
        return RestResult.ok();
    }

    @Override
    public List<MemberLevelDTO> getMemberLevelInfo() {

        String key = (String) redisUtils.get(RedisConstants.MEMBER_LEVEL_CONFIG);
        List<MemberLevelConfig> listRe = JSON.parseObject(key, new TypeReference<List<MemberLevelConfig>>() {
        });

        String welfareKey = (String) redisUtils.get(RedisConstants.MEMBER_LEVEL_WELFARE_CONFIG);
        List<MemberLevelWelfareConfig> resultList = JSON.parseObject(welfareKey, new TypeReference<List<MemberLevelWelfareConfig>>() {
        });
        List<MemberLevelDTO> list = new ArrayList<>();

        for (MemberLevelConfig item : listRe) {
            for (MemberLevelWelfareConfig innerItem : resultList) {
                if (item.getLevel().equals(innerItem.getLevel())) {
                    MemberLevelDTO memberLevelDTO = new MemberLevelDTO();
                    BeanUtils.copyProperties(item, memberLevelDTO);
                    BeanUtils.copyProperties(innerItem, memberLevelDTO);
                    list.add(memberLevelDTO);
                }
            }
        }
        return list;
    }

    @Override
    public void memberUpgrade(String memberId) {
        MemberInfo item = this.getMemberInfoById(memberId);
        String key = (String) redisUtils.get(RedisConstants.MEMBER_LEVEL_CONFIG);
        List<MemberLevelConfig> listRe = JSON.parseObject(key, new TypeReference<List<MemberLevelConfig>>() {
        });
        List<MemberInfo> allList = new ArrayList<>();
        Integer level = item.getLevel();
        BigDecimal buySuccessRate = BigDecimal.ZERO;
        if (item.getTotalBuySuccessCount() > 0 && item.getTotalBuyCount() > 0) {
            buySuccessRate = new BigDecimal(item.getTotalBuySuccessCount()).divide(new BigDecimal(item.getTotalBuyCount()), 2, RoundingMode.DOWN);
        }
        item.setBeforeLevel(item.getLevel());
        for (MemberLevelConfig config : listRe) {
            if (config.getLevel() > UserLevelEnum.NORMAL.getCode()) {
                if (item.getCreditScore().longValue() >= config.getCreditScore().longValue() &&
                        item.getTotalSellSuccessCount() >= config.getSellNum() &&
                        item.getTotalBuySuccessCount() >= config.getBuyNum() && buySuccessRate.doubleValue() >= config.getBuySuccessRate().doubleValue()) {
                    level = config.getLevel();
                }
            } else {
                if (item.getCreditScore().longValue() < config.getCreditScore().longValue()) {
                    level = UserLevelEnum.NORMAL.getCode();
                    break;
                }
            }
        }

        if (!level.equals(item.getBeforeLevel())) {
            item.setLevel(level);
            allList.add(item);
            this.baseMapper.updateRechargeInfo(allList);
            log.info("会员升级成功会员ID->{},升级前等级->{},升级后等级->{}", item.getId(), item.getBeforeLevel(), item.getLevel());
        }

    }


    /**
     * 版本号规则：主版本号.子版本号.阶段版本号 例如：1.0.3
     *
     * @param currentVersion
     * @return
     */
    @Override
    public AppVersionManagerDTO getAppVersionInfo(String currentVersion, Integer device) {

        AppVersionManagerDTO vo = new AppVersionManagerDTO();
        try {
            String key = (String) redisUtils.get(RedisConstants.APP_VERSION_CONFIG);
            List<AppVersionDTO> resultList = JSON.parseObject(key, new TypeReference<List<AppVersionDTO>>() {
            });
            AppVersionDTO result = resultList.stream().filter(item -> item.getDevice().equals(device)).findAny().get();
            Integer currentVersionIn = null;
            Integer configLastVersion = null;
            Integer minVersion = null;
            BeanUtils.copyProperties(result, vo);
            if (ObjectUtils.isEmpty(result) || StringUtils.isBlank(currentVersion) ||
                    result.getStatus().equals(0) || StringUtils.isBlank(result.getLatestVersion())) {
                return vo;
            }
            currentVersionIn = Integer.parseInt(currentVersion.replace(".", ""));
            configLastVersion = Integer.parseInt(result.getLatestVersion().replace(".", ""));
            if (StringUtils.isNotBlank(result.getMinVersion())) {
                minVersion = Integer.parseInt(result.getMinVersion().replace(".", ""));
            }
            // 根据是否有特殊需求可指定某个版本必须强制更新
            if (StringUtils.isNotBlank(result.getForceUpdateVersion()) && currentVersion.equals(result.getForceUpdateVersion())) {
                vo.setIsForceUpdate(true);
                vo.setIsUpdate(true);
            } else if (currentVersionIn < configLastVersion) {
                // 如果currentVersion < latestVersion,则isUpdate = true
                vo.setIsUpdate(true);
                if (ObjectUtils.isNotEmpty(minVersion) && currentVersionIn < minVersion) {
                    // 如果currentVersion < minVersion,则forceUpdate = true；
                    vo.setIsForceUpdate(true);
                } else if (ObjectUtils.isNotEmpty(minVersion) && currentVersionIn >= minVersion) {
                    vo.setIsForceUpdate(false);
                }
            } else if (currentVersionIn.equals(configLastVersion)) {
                // 如果currentVersion == latestVersion，则isUpdate = false.
                vo.setIsUpdate(false);
                vo.setIsForceUpdate(false);
            }
        } catch (Exception e) {
            log.error("MemberInfoServiceImpl.getAppVersionInfo" + e.getMessage());
        }

        return vo;
    }


    /**
     * 校验手机号是否被使用
     *
     * @param checkPhoneNumberAvailabilityReq
     * @return {@link RestResult}<{@link CheckPhoneNumberAvailabilityVo}>
     */
    @Override
    public RestResult<CheckPhoneNumberAvailabilityVo> checkPhoneNumberAvailability(CheckPhoneNumberAvailabilityReq checkPhoneNumberAvailabilityReq) {

        CheckPhoneNumberAvailabilityVo checkPhoneNumberAvailabilityVo = new CheckPhoneNumberAvailabilityVo();

        //判断手机号是否被注册
        if (getMemberByPhoneNumber(checkPhoneNumberAvailabilityReq.getMobileNumber()) != null) {
            // 该手机号已被使用
            checkPhoneNumberAvailabilityVo.setIsUsed(true);
        }

        return RestResult.ok(checkPhoneNumberAvailabilityVo);
    }

    @Override
    @Transactional
    public Boolean processHistoryNewUserTask(String taskType) {

        //分布式锁key ar-wallet-handleDailyTask
        String key = "ar-wallet-handleNewUserTask";
        RLock lock = redissonUtil.getLock(key);
        log.info("处理新人任务历史数据 taskType: {}", taskType);
        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                // 查询新人任务配置信息
                TaskManager taskManager = taskManagerService.getTaskDetailsByType(taskType);

                if (taskManager == null) {
                    log.error("处理新人任务历史数据 job处理失败 获取任务信息失败, taskType: {}", taskType);
                    return false;
                }
                // 查询已完成任务的会员
                LambdaQueryChainWrapper<MemberInfo> queryWrap = lambdaQuery();
                if (STARTER_QUESTS_BUY.getCode().equals(taskType)) {
                    queryWrap.eq(MemberInfo::getBuyGuideStatus, 1);
                } else if (STARTER_QUESTS_SELL.getCode().equals(taskType)) {
                    queryWrap.eq(MemberInfo::getSellGuideStatus, 1);
                }
                List<MemberInfo> finishMembers = queryWrap.select(MemberInfo::getId).list();

                // 查询任务记录中已存在的会员
                List<Long> membersWithCompletedTask = memberTaskStatusService.lambdaQuery()
                        .eq(MemberTaskStatus::getTaskType, taskType)
                        .list()
                        .stream()
                        .map(MemberTaskStatus::getMemberId)
                        .collect(Collectors.toList());

                // 过滤掉任务记录中已存在的会员
                finishMembers = finishMembers.stream()
                        .filter(member -> !membersWithCompletedTask.contains(member.getId()))
                        .collect(Collectors.toList());

                // 2. 准备批量插入的任务状态记录
                List<MemberTaskStatus> taskStatusRecords = new ArrayList<>();

                for (MemberInfo member : finishMembers) {
                    MemberTaskStatus taskStatus = new MemberTaskStatus();
                    taskStatus.setMemberId(member.getId());//会员id
                    taskStatus.setTaskType(Integer.valueOf(taskManager.getTaskType()));/* 实名认证任务类型 */
                    taskStatus.setTaskId(taskManager.getId());//实名认证任务的ID
                    taskStatus.setCompletionStatus(1); // 任务完成
                    taskStatus.setRewardClaimed(0); // 奖励未领取
                    taskStatus.setCompletionDate(LocalDate.now());//任务完成日期 今天
                    taskStatus.setOrderNo(orderNumberGenerator.generateOrderNo("RW"));//任务订单号
                    taskStatus.setCreateTime(LocalDateTime.now());//任务完成时间
                    taskStatus.setTaskCycle(1);//任务周期 1:一次性任务 2:周期性-每天

                    taskStatusRecords.add(taskStatus);
                }

                // 3. 批量插入任务完成记录
                if (!taskStatusRecords.isEmpty()) {
                    //批量插入 批次大小限制: 1000条记录
                    boolean saveBatch = memberTaskStatusService.saveBatch(taskStatusRecords, 1000);
                    if (!saveBatch) {
                        //执行失败了 手动抛出异常回滚
                        log.error("处理新人任务历史数据 job处理失败, 处理行数:{}, taskType:{}", taskStatusRecords.size(), taskType);
                        throw new RuntimeException();
                    } else {
                        log.info("处理新人任务历史数据 job处理成功, 处理行数:{}, taskType:{}", taskStatusRecords.size(), taskType);
                        return true;
                    }
                }

                return true;
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("处理新人任务历史数据, taskType:{}", taskType, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }
}