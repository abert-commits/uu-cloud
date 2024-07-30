package org.uu.wallet.aop;

import com.alibaba.cloud.commons.lang.StringUtils;
import org.uu.common.core.utils.UserAgentUtil;
import org.uu.wallet.Enum.LogModuleEnum;
import org.uu.wallet.Enum.MemberOperationModuleEnum;
import org.uu.wallet.Enum.MemberTypeEnum;
import org.uu.wallet.annotation.LogMemberOperation;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberOperationLogMessage;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.util.IpUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class LogMemberAspect {

    @Autowired
    private HttpServletRequest request; // 注入HttpServletRequest

    @Autowired
    private IMemberInfoService memberInfoService;

    @Autowired
    private RabbitMQService rabbitMQService;

    @Pointcut("@annotation(logMemberOperation)")
    public void memberOperationLogPointcut(LogMemberOperation logMemberOperation) {
    }

    @Around("memberOperationLogPointcut(logMemberOperation)")
    public Object logMemberOperation(ProceedingJoinPoint joinPoint, LogMemberOperation logMemberOperation) throws Throwable {

        // 获取注解中的枚举值
        MemberOperationModuleEnum operationModule = logMemberOperation.value();

        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        Object result = null;
        try {
            result = joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String response = (result != null) ? result.toString() : "null";

            // 从 HttpServletRequest 中获取请求相关信息
            String requestPath = request.getRequestURI();
            String httpMethod = request.getMethod();
            String requestIP = IpUtil.getRealIP(request);
            String userAgent = request.getHeader("User-Agent");

            // 构建操作日志对象
            MemberOperationLogMessage memberOperationLogMessage = new MemberOperationLogMessage();
            // 假设 memberId 和 username 通过某种方式获取，可能是从 session、security context 或其他方式


            Long memberId = null;
            String username = null;
            String memberType = null;

            //获取会员信息
            MemberInfo memberInfo = memberInfoService.getMemberInfo();
            if (memberInfo != null) {
                memberId = memberInfo.getId();
                username = memberInfo.getMemberAccount();
                memberType = memberInfo.getMemberType();

                //判断如果是商户会员首次登录  就记录首次登录信息
                if (!MemberTypeEnum.SIX_NINE_MEMBER.getCode().equals(memberInfo.getMemberType()) && StringUtils.isEmpty(memberInfo.getFirstLoginIp())){
                    //首次登录 记录首次登录信息
                    memberInfoService.setFirstLoginInfo(memberInfo.getId(), requestIP, LocalDateTime.now());
                }

            }

            memberOperationLogMessage.setMemberId(memberId);
            memberOperationLogMessage.setUsername(username);
            memberOperationLogMessage.setMemberType(memberType);
            memberOperationLogMessage.setOperationTime(LocalDateTime.now());
            memberOperationLogMessage.setIpAddress(IpUtil.getRealIP(request));
            memberOperationLogMessage.setDevice(UserAgentUtil.getDeviceType(request.getHeader("user-agent")));
            memberOperationLogMessage.setUserAgent(userAgent);
            memberOperationLogMessage.setModule(LogModuleEnum.FRONTEND.getCode());//操作模块 3: 前台
            memberOperationLogMessage.setOperationPath(request.getRequestURL().toString());//获取操作路径
            memberOperationLogMessage.setRequestPath(requestPath);
            memberOperationLogMessage.setMethodName(className + "." + methodName);
            memberOperationLogMessage.setHttpMethod(httpMethod);
            memberOperationLogMessage.setParameters(Arrays.toString(args));
            memberOperationLogMessage.setResponse(response);
            memberOperationLogMessage.setRequestIp(requestIP);
            memberOperationLogMessage.setDuration(duration);
            memberOperationLogMessage.setMethodComment(operationModule.getDescription());
            //操作模块 枚举code
            memberOperationLogMessage.setModuleCode(operationModule.getCode());

            // 发送日志消息到MQ
            rabbitMQService.sendMemberOperationLogMessage(memberOperationLogMessage);
        }
        return result;
    }
}