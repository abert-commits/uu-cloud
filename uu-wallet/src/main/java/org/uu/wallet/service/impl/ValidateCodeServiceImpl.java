package org.uu.wallet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.uu.wallet.Enum.SwitchIdEnum;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.req.ValidateSmsCodeReq;
import org.uu.wallet.service.IControlSwitchService;
import org.uu.wallet.service.ValidateCodeService;
import org.uu.wallet.util.SignUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateCodeServiceImpl implements ValidateCodeService {
    private final ArProperty arProperty;

    private final RedisTemplate redisTemplate;

    private final IControlSwitchService controlSwitchService;

    @Override
    public Boolean validate(String phone, String code) {
        log.info("ValidateCodeServiceImpl.validate(), phone: {}, code: {}", phone, code);
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            return Boolean.FALSE;
        }

        String appEnv = arProperty.getAppEnv();
        boolean isTestEnv = "sit".equals(appEnv) || "dev".equals(appEnv);
        if (isTestEnv) {
            return "123456".equals(code);
        }

        //查看是否开启验证码开关
        if (controlSwitchService.isSwitchEnabled(SwitchIdEnum.REGISTRATION_CAPTCHA.getSwitchId())) {
            //开启了验证码开关 校验验证码

            ValidateSmsCodeReq validateSmsCodeReq = ValidateSmsCodeReq.builder()
                    .mobileNumber(phone)
                    .verificationCode(code)
                    .build();

            //判断手机验证码是否正确
            if (!signUpValidateSmsCode(validateSmsCodeReq)) {
                log.error("手机号注册处理失败: 验证码错误, 手机号: {}, 验证码: {}", validateSmsCodeReq.getMobileNumber(), validateSmsCodeReq.getVerificationCode());
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

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

}
