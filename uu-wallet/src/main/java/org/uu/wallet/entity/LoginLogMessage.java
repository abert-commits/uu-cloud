package org.uu.wallet.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Date;

/**
 * 会员登录日志记录 MQ消息体
 *
 * @author Simon
 * @date 2024/01/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginLogMessage implements Serializable {


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
    private Date loginTime;


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
}