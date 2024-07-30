package org.uu.job.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.uu.job.Enum.DataSourceKey;
import org.springframework.stereotype.Component;

/**
 * @author Admin
 */
@Aspect
@Component
public class DataSourceAdvice {

    @Pointcut("execution(* org.uu.wallet..*.*(..))")
    public void walletPointcut() {}

    @Pointcut("execution(* org.uu.manager..*.*(..))")
    public void managerPointcut() {}


    @Around("walletPointcut()")
    public Object order(ProceedingJoinPoint pjp) throws Throwable {
        DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.WALLET);
        Object retVal = pjp.proceed();
        DynamicDataSourceContextHolder.clearDataSourceKey();
        return retVal;
    }
    @Around("managerPointcut()")
    public Object account(ProceedingJoinPoint pjp) throws Throwable {
        DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.MANAGER);
        Object retVal = pjp.proceed();
        DynamicDataSourceContextHolder.clearDataSourceKey();
        return retVal;
    }
}
