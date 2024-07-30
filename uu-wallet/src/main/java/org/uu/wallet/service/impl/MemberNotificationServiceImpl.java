package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.vo.request.MemberNotificationListRequestVO;
import org.uu.common.pay.vo.response.MemberNotificationResponseVO;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.NotificationReadFlagEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberNotification;
import org.uu.wallet.mapper.MemberNotificationMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.req.BindEmailReq;
import org.uu.wallet.req.TradeNotificationReq;
import org.uu.wallet.req.VerifySmsCodeReq;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.service.IMemberNotificationService;
import org.uu.wallet.util.SignUtil;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberNotificationServiceImpl extends ServiceImpl<MemberNotificationMapper, MemberNotification> implements IMemberNotificationService {
    private final RedisTemplate redisTemplate;
    private final ArProperty arProperty;
    private final IMemberInfoService memberInfoService;

    /**
     * 根据会员id获取通知数量
     *
     * @param memberId
     * @return {@link Integer}
     */
    @Override
    public Integer getNotificationCountByMemberId(String memberId) {
        return lambdaQuery().eq(MemberNotification::getMemberId, memberId).eq(MemberNotification::getReadFlag, NotificationReadFlagEnum.READ_N).count().intValue();
    }

    /**
     * 校验短信验证码
     *
     * @param verifySmsCodeReq
     * @return {@link Boolean}
     */
    public Boolean validateSmsCode(VerifySmsCodeReq verifySmsCodeReq) {

        //获取短信验证码 redis-key 前缀
        String smsCodePrefix = arProperty.getSmsCodePrefix();

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("校验短信验证码失败: 获取会员信息失败");
            return Boolean.FALSE;
        }

        //获取会员原有手机号
        String previousPhoneNumber = memberInfo.getMobileNumber();

        //判断会员之前是否绑定过手机号 如果绑定了 那么判断提交过来的手机号是否和绑定的一致
        if (previousPhoneNumber != null && !previousPhoneNumber.equals(verifySmsCodeReq.getMobileNumber())) {
            //会员之前绑定了手机号 并且提交过来的手机和绑定的不一致  同时判断原手机号和新手机号验证码

            log.info("校验短信验证码 会员之前绑定了手机号 并且提交过来的手机和绑定的不一致  同时判断原手机号和新手机号验证码, 会员信息: {}, req: {}", memberInfo, verifySmsCodeReq);

            //判断原手机号是否已校验
            //判断key是否存在
            if (redisTemplate.opsForHash().hasKey(smsCodePrefix + previousPhoneNumber, "verified")) {
                String previousVerified = (String) redisTemplate.boundHashOps(smsCodePrefix + previousPhoneNumber).get("verified");

                // 原手机号验证码
                String previousCode = (String) redisTemplate.boundHashOps(smsCodePrefix + previousPhoneNumber).get("code");

                String sign = null;

                if (StringUtils.isNotEmpty(previousCode)){
                    sign = SignUtil.getMD5(previousCode, 1, arProperty.getRedismd5key());
                }

                log.info("验证码正确 将状态改为已校验(两个手机号), smsCodePrefix: {}, VerificationCode(): {}, getRedismd5key: {}, verified: {}", smsCodePrefix + verifySmsCodeReq.getMobileNumber(), verifySmsCodeReq.getVerificationCode(), arProperty.getRedismd5key(), SignUtil.getMD5(verifySmsCodeReq.getVerificationCode(), 1, arProperty.getRedismd5key()));

                log.info("校验短信验证码 会员之前绑定了手机号 并且提交过来的手机和绑定的不一致  同时判断原手机号和新手机号验证码, 会员信息: {}, req: {}, previousVerified: {}, sign: {}", memberInfo, verifySmsCodeReq, previousVerified, sign);

                if (sign != null && sign.equals(previousVerified)) {
                    log.info("原有手机号校验成功 req: {}, sign: {}, previousVerified: {}, 会员信息: {}", verifySmsCodeReq, sign, previousVerified, memberInfo);
                    //原有手机号校验成功
                    //校验新手机号验证码 是否正确
                    //判断key是否存在
                    if (redisTemplate.opsForHash().hasKey(smsCodePrefix + verifySmsCodeReq.getMobileNumber(), "code")) {

                        //获取redis验证码
                        String redisCode = (String) redisTemplate.boundHashOps(smsCodePrefix + verifySmsCodeReq.getMobileNumber()).get("code");

                        //校验会员提交过来的验证码是否和redis存储的一致
                        if (verifySmsCodeReq.getVerificationCode().equals(redisCode)) {
                            log.info("新手机号码校验成功 req: {}, redisCode: {}, 会员账号: {}", verifySmsCodeReq, redisCode, memberInfo.getMemberAccount());
                            //验证码正确 将状态改为已校验
                            BoundHashOperations hashKey = redisTemplate.boundHashOps(smsCodePrefix + verifySmsCodeReq.getMobileNumber());
                            hashKey.put("verified", SignUtil.getMD5(verifySmsCodeReq.getVerificationCode(), 1, arProperty.getRedismd5key()));

                            log.info("验证码正确 将状态改为已校验(两个手机号), smsCodePrefix: {}, VerificationCode(): {}, getRedismd5key: {}, verified: {}", smsCodePrefix + verifySmsCodeReq.getMobileNumber(), verifySmsCodeReq.getVerificationCode(), arProperty.getRedismd5key(), SignUtil.getMD5(verifySmsCodeReq.getVerificationCode(), 1, arProperty.getRedismd5key()));

                            return true;
                        }
                    }
                }
            }
        } else {
            //只有一个手机号(会员之前没绑定手机号 或者会员提交的手机号和之前绑定的是一致)
            //判断key是否存在
            if (redisTemplate.opsForHash().hasKey(smsCodePrefix + verifySmsCodeReq.getMobileNumber(), "code")) {


                log.info("只有一个手机号(会员之前没绑定手机号 或者会员提交的手机号和之前绑定的是一致: {}, 会员账号: {}", verifySmsCodeReq, memberInfo.getMemberAccount());

                //获取redis验证码
                String code = (String) redisTemplate.boundHashOps(smsCodePrefix + verifySmsCodeReq.getMobileNumber()).get("code");

                //校验会员提交过来的验证码是否和redis存储的一致
                if (verifySmsCodeReq.getVerificationCode().equals(code)) {

                    log.info("短信验证码验证成功 req: {}, redisCode: {}, 会员账号: {}", verifySmsCodeReq, code, memberInfo.getMemberAccount());

                    //验证码正确 将状态改为已校验
                    BoundHashOperations hashKey = redisTemplate.boundHashOps(smsCodePrefix + verifySmsCodeReq.getMobileNumber());
                    hashKey.put("verified", SignUtil.getMD5(verifySmsCodeReq.getVerificationCode(), 1, arProperty.getRedismd5key()));

                    log.info("验证码正确 将状态改为已校验(只有一个手机号), smsCodePrefix: {}, VerificationCode(): {}, getRedismd5key: {}, verified: {}", smsCodePrefix + verifySmsCodeReq.getMobileNumber(), verifySmsCodeReq.getVerificationCode(), arProperty.getRedismd5key(), SignUtil.getMD5(verifySmsCodeReq.getVerificationCode(), 1, arProperty.getRedismd5key()));
                    return true;
                }
            }
        }

        log.error("短信验证码校验失败 req: {}, 会员信息: {}", verifySmsCodeReq, memberInfo);
        return false;
    }

    /**
     * 校验邮箱验证码
     *
     * @param bindEmailReq
     * @param memberInfo
     * @return {@link Boolean}
     */
    @Override
    public Boolean validateEmailCode(BindEmailReq bindEmailReq, MemberInfo memberInfo) {

        //获取邮箱验证码 redis-key 前缀
        String emailCodePrefix = arProperty.getEmailCodePrefix();

        //获取会员原有邮箱号
        String previousEmail = memberInfo.getEmailAccount();

        //判断会员之前是否绑定过邮箱号 如果绑定了 那么判断提交过来的邮箱号是否和绑定的一致
        if (previousEmail != null && !previousEmail.equals(bindEmailReq.getEmailAccount())) {
            //会员之前绑定了邮箱号 并且提交过来的邮箱和绑定的不一致  同时判断原邮箱号和新邮箱号验证码

            log.info("校验邮箱验证码: 会员之前绑定了邮箱号 并且提交过来的邮箱和绑定的不一致  同时判断原邮箱号和新邮箱号验证码 req: {}, 会员账号: {}", bindEmailReq, memberInfo.getMemberAccount());

            //判断原邮箱号是否已校验
            //判断key是否存在
            if (redisTemplate.opsForHash().hasKey(emailCodePrefix + previousEmail, "verified")) {
                String previousVerified = (String) redisTemplate.boundHashOps(emailCodePrefix + previousEmail).get("verified");

                String sign = SignUtil.getMD5(bindEmailReq.getVerificationCode(), 1, arProperty.getRedismd5key());

                if (sign.equals(previousVerified)) {
                    //原有邮箱号校验成功
                    log.info("原有邮箱号验证码校验成功 req: {}, sign: {}, previousVerified: {}, 会员账号: {}", bindEmailReq, sign, previousVerified, memberInfo.getMemberAccount());
                    //校验新邮箱号验证码 是否正确
                    //判断key是否存在
                    if (redisTemplate.opsForHash().hasKey(emailCodePrefix + bindEmailReq.getEmailAccount(), "code")) {

                        //获取redis验证码
                        String redisCode = (String) redisTemplate.boundHashOps(emailCodePrefix + bindEmailReq.getEmailAccount()).get("code");

                        //校验会员提交过来的验证码是否和redis存储的一致
                        if (bindEmailReq.getVerificationCode().equals(redisCode)) {

                            log.info("新邮箱号验证码校验成功 req: {}, redisCode: {}, 会员账号: {}", bindEmailReq, redisCode, memberInfo.getMemberAccount());

                            //验证码正确 将状态改为已校验
                            BoundHashOperations hashKey = redisTemplate.boundHashOps(emailCodePrefix + bindEmailReq.getEmailAccount());
                            hashKey.put("verified", SignUtil.getMD5(bindEmailReq.getVerificationCode(), 1, arProperty.getRedismd5key()));
                            return true;
                        }
                    }
                }
            }
        } else {
            //只有一个邮箱号(会员之前没绑定邮箱号 或者会员提交的邮箱号和之前绑定的是一致)
            //判断key是否存在
            if (redisTemplate.opsForHash().hasKey(emailCodePrefix + bindEmailReq.getEmailAccount(), "code")) {

                log.info("只有一个邮箱号(会员之前没绑定邮箱号 或者会员提交的邮箱号和之前绑定的是一致) req: {}, 会员账号: {}", bindEmailReq, memberInfo.getMemberAccount());

                //获取redis验证码
                String code = (String) redisTemplate.boundHashOps(emailCodePrefix + bindEmailReq.getEmailAccount()).get("code");

                //校验会员提交过来的验证码是否和redis存储的一致
                if (bindEmailReq.getVerificationCode().equals(code)) {

                    log.info("邮箱验证码校验成功: {}, redisCode: {}, 会员id: {}", bindEmailReq, code, memberInfo.getMemberAccount());

                    //验证码正确 将状态改为已校验
                    BoundHashOperations hashKey = redisTemplate.boundHashOps(emailCodePrefix + bindEmailReq.getEmailAccount());
                    hashKey.put("verified", SignUtil.getMD5(bindEmailReq.getVerificationCode(), 1, arProperty.getRedismd5key()));
                    return true;
                }
            }
        }
        log.error("邮箱验证码校验失败 req: {}, 会员信息: {}", bindEmailReq, memberInfo);
        return false;
    }

    /**
     * 获取消息通知列表
     * @param requestVO 请求参数
     */
    @Override
    public RestResult<PageReturn<MemberNotificationResponseVO>> memberNotificationList(MemberNotificationListRequestVO requestVO) {
        // 获取当前用户ID
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }
        LambdaQueryChainWrapper<MemberNotification> wrapper = this.lambdaQuery()
                .eq(MemberNotification::getMemberId, currentUserId)
                .eq(MemberNotification::getNotificationType, requestVO.getNotificationType())
                .orderByDesc(MemberNotification::getCreateTime);

        if (Arrays.asList(NotificationReadFlagEnum.READ_Y.getReadFlag(), NotificationReadFlagEnum.READ_N.getReadFlag()).contains(requestVO.getReadFlag())) {
            wrapper.eq(MemberNotification::getReadFlag, requestVO.getReadFlag());
        }
        Page<MemberNotification> page = new Page<>();
        page.setCurrent(requestVO.getPageNo())
                .setSize(requestVO.getPageSize());
        Page<MemberNotification> notificationPage = wrapper.page(page);
        List<MemberNotification> notificationList = notificationPage.getRecords();
        List<MemberNotificationResponseVO> responseVOList = new ArrayList<>();
        if (! CollectionUtils.isEmpty(notificationList)) {
            responseVOList = notificationList.stream()
                    .filter(Objects::nonNull)
                    .map(item -> MemberNotificationResponseVO.builder()
                            .id(item.getId())
                            .memberId(item.getMemberId())
                            .content(item.getContent())
                            .readFlag(item.getReadFlag())
                            .orderType(item.getOrderType())
                            .orderStatus(item.getOrderStatus())
                            .createTime(item.getCreateTime())
                            .build())
                    .collect(Collectors.toList());
        }
        return RestResult.ok(PageUtils.flush(page, responseVOList));
    }

    @Override
    public RestResult<Void> allRead() {
        // 获取当前用户ID
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }
        boolean update = this.lambdaUpdate()
                .eq(MemberNotification::getMemberId, currentUserId)
                .eq(MemberNotification::getReadFlag, NotificationReadFlagEnum.READ_N.getReadFlag())
                .set(MemberNotification::getReadFlag, NotificationReadFlagEnum.READ_Y.getReadFlag())
                .set(MemberNotification::getUpdateTime, LocalDateTime.now())
                .update();
        if (update) {
            return RestResult.ok();
        }
        return RestResult.failed("All read notification failed");
    }

    @Override
    public RestResult<Void> readById(Long id) {
        // 获取当前用户ID
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }
        MemberNotification notification = this.getById(id);
        if (Objects.isNull(notification)) {
            return RestResult.failed("Notification not exist");
        }

        if (! currentUserId.equals(notification.getMemberId())) {
            return RestResult.failed("You can't read other people's messages");
        }

        if (NotificationReadFlagEnum.READ_Y.getReadFlag().equals(notification.getReadFlag())) {
            return RestResult.failed("Notification have read");
        }

        boolean update = this.lambdaUpdate()
                .eq(MemberNotification::getMemberId, currentUserId)
                .eq(MemberNotification::getId, id)
                .set(MemberNotification::getReadFlag, NotificationReadFlagEnum.READ_Y.getReadFlag())
                .set(MemberNotification::getUpdateTime, LocalDateTime.now())
                .update();
        if (update) {
            return RestResult.ok();
        }
        return RestResult.failed("Read notification failed");
    }

    @Override
    public Boolean insertPayNotification(TradeNotificationReq requestVO) {
        // 暂时仅支持已结束(成功/失败)的(买入/卖出/USDT充值)订单
        if (!Arrays.asList(OrderStatusEnum.SUCCESS,OrderStatusEnum.WAS_CANCELED).contains(requestVO.getOrderStatusEnum())) {
            log.info("仅支持订单状态为成功(7)或失败(8)的订单");
            return false;
        }
        if (!Arrays.asList(MemberAccountChangeEnum.RECHARGE, MemberAccountChangeEnum.WITHDRAW, MemberAccountChangeEnum.USDT_RECHARGE).contains(requestVO.getOrderType())) {
            log.info("仅支持订单状态为买入(1)/卖出(2)/USDT充值(3)的订单");
            return false;
        }
        return this.save(
                MemberNotification.builder()
                        .memberId(requestVO.getMemberId())
                        .notificationType(requestVO.getNotificationType().getNotificationType())
                        .content(requestVO.getOrderNo())
                        .orderType(Integer.valueOf(requestVO.getOrderType().getCode()))
                        .orderStatus(Integer.valueOf(requestVO.getOrderStatusEnum().getCode()))
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build()
        );
    }
}


