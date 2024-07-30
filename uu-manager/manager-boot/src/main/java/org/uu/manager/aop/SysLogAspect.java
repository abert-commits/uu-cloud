package org.uu.manager.aop;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.uu.common.web.utils.UserContext;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.service.ISysLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class SysLogAspect {

    private final ISysLogService sysLogService;

    ThreadLocal<Long> startTime = new ThreadLocal<>();


    @Pointcut("@annotation(org.uu.manager.annotation.SysLog)")//在注解的位置切入代码

    public void sysLogPoinCut() {
    }

    @Before("sysLogPoinCut()")
    public void beforMethod(JoinPoint point){
        startTime.set(System.currentTimeMillis());
    }

    /**
     * 设置操作异常切入点记录异常日志 扫描所有controller包下操作
     */
    @Pointcut("execution(* org.uu.manager.controller..*.*(..))")
    public void operExceptionLogPoinCut() {
    }
    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     *
     * @param joinPoint 切入点
     * @param result      返回结果
     */
    @AfterReturning(value = "sysLogPoinCut()", returning = "result")
    public void saveOperLog(JoinPoint joinPoint, Object result) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            // 获取操作
            SysLog myLog = method.getAnnotation(SysLog.class);

            org.uu.manager.entity.SysLog log = new org.uu.manager.entity.SysLog();
            if (myLog != null) {
                log.setModule(myLog.title());//设置模块名称
               // log.setContent(myLog.content());//设置日志内容
            }
            // 将入参转换成json
            String params = argsArrayToString(joinPoint.getArgs());
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName + "()";
            log.setMethod(methodName); //设置请求方法
            log.setMethod(request.getMethod());//设置请求方式
            log.setRequestParam(params); // 请求参数
            //operlog.setResponseResult(JSON.toJSONString(result)); // 返回结果
            log.setResponseResult(JSON.toJSONString(result)); // 请求参数
            log.setCreateBy(UserContext.getCurrentUserName()); // 获取用户名（真实环境中，肯定有工具类获取当前登录者的账号或ID的，或者从token中解析而来）
            log.setIp(getIp(request)); // IP地址
            log.setContent(myLog.content());
            log.setPath(request.getRequestURI()); // 请求URI
            //log.setCreateTime(new Date()); // 时间
            //log.setStatus(0);//操作状态（0正常 1异常）
            Long takeTime = System.currentTimeMillis() - startTime.get();//记录方法执行耗时时间（单位：毫秒）


            //operlog.setTakeTime(takeTime);
            //插入数据库
            sysLogService.save(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterThrowing(pointcut = "operExceptionLogPoinCut()", throwing = "e")
    public void saveExceptionLog(JoinPoint joinPoint, Throwable e) {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        org.uu.manager.entity.SysLog sysLog = new  org.uu.manager.entity.SysLog();
        try {
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName + "()";
            // 获取操作
            SysLog myLog = method.getAnnotation(SysLog.class);
            if (sysLog != null) {
                sysLog.setModule(myLog.title());//设置模块名称
                sysLog.setContent(myLog.content());//设置日志内容
            }
            // 将入参转换成json
            String params = argsArrayToString(joinPoint.getArgs());
            sysLog.setMethod(methodName); //设置请求方法
            sysLog.setMethod(request.getMethod());//设置请求方式
            sysLog.setRequestParam(params); // 请求参数
            sysLog.setResponseResult(JSON.toJSONString(e.getMessage())); // 请求参数
           // operlog.setOperName("张三"); // 获取用户名（真实环境中，肯定有工具类获取当前登录者的账号或ID的，或者从token中解析而来）
            sysLog.setIp(getIp(request)); // IP地址
            //operlog.setIpLocation("湖北武汉"); // IP归属地（真是环境中可以调用第三方API根据IP地址，查询归属地）
            sysLog.setPath(request.getRequestURI()); // 请求URI
          //  sysLog.setCreateTime(new Date()); // 时间
            //sysLog.setStatus(1);//操作状态（0正常 1异常）
            //operlog.setErrorMsg(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace()));//记录异常信息
            //插入数据库
            sysLogService.save(sysLog);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    /**
     * 转换异常信息为字符串
     */
    public String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuffer strbuff = new StringBuffer();
        for (StackTraceElement stet : elements) {
            strbuff.append(stet + "\n");
        }
        String message = exceptionName + ":" + exceptionMessage + "\n\t" + strbuff.toString();
        message = substring(message,0 ,2000);
        return message;
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray)
    {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0)
        {
            for (Object o : paramsArray)
            {
                if (o != null)
                {
                    try
                    {
                        Object jsonObj = JSON.toJSON(o);
                        params += jsonObj.toString() + " ";
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return params.trim();
    }

    //字符串截取
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        } else {
            if (end < 0) {
                end += str.length();
            }

            if (start < 0) {
                start += str.length();


            }

            if (end > str.length()) {
                end = str.length();
            }

            if (start > end) {
                return "";
            } else {
                if (start < 0) {
                    start = 0;
                }

                if (end < 0) {
                    end = 0;
                }
                return str.substring(start, end);
            }
        }
    }

    /**
     * 转换request 请求参数
     * @param paramMap request获取的参数数组
     */
    public Map<String, String> converMap(Map<String, String[]> paramMap) {
        Map<String, String> returnMap = new HashMap<>();
        for (String key : paramMap.keySet()) {
            returnMap.put(key, paramMap.get(key)[0]);
        }
        return returnMap;
    }

    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}