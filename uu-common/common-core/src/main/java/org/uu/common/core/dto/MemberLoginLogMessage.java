package org.uu.common.core.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员登录日志记录 MQ消息体
 *
 * @author Simon
 * @date 2024/01/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginLogMessage implements Serializable {


    /**
     * 会员id
     */
    private Long memberId;


    /**
     * 会员账号
     */
    private String username;


    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;


    /**
     * 登录ip
     */
    private String ipAddress;


    /**
     * 登录设备
     */
    private String device;


    /**
     * 用户代理（浏览器或设备信息）
     */
    private String userAgent;


    /**
     * 登录模式 前台登录  商户登录
     */
    private String authenticationMode;


    /**
     * 登录状态  1 成功  0 失败
     */
    private String loginStatus;


    /**
     * 会员类型
     */
    private String memberType;


    /**
     * 首次登录ip
     */
    private String firstLoginIp;
}