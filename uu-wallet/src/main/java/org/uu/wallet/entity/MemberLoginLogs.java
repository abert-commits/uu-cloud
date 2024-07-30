package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员登录日志表
 *
 * @author
 * @since 2024-01-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_login_logs")
public class MemberLoginLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员ID
     */
    @ApiModelProperty(value = "会员ID")
    private Long memberId;


    /**
     * 会员账号
     */
    @ApiModelProperty(value = "会员账号")
    private String username;


    /**
     * 登录时间
     */
    @ApiModelProperty(value = "登录时间")
    private LocalDateTime loginTime;


    /**
     * 登录IP地址
     */
    @ApiModelProperty(value = "登录IP地址")
    private String ipAddress;


    /**
     * 登录设备
     */
    @ApiModelProperty(value = "登录设备")
    private String device;


    /**
     * 用户代理（浏览器或设备信息）
     */
    @ApiModelProperty(value = "用户代理（浏览器或设备信息）")
    private String userAgent;


    /**
     * 登录模式 前台登录  商户登录
     */
    @ApiModelProperty(value = "登录模式 前台登录  商户登录")
    private String authenticationMode;


    /**
     * 登录状态  1 成功  0 失败
     */
    @ApiModelProperty(value = "登录状态  1 成功  0 失败")
    private String loginStatus;


    /**
     * 会员类型
     */
    @ApiModelProperty(value = "会员类型")
    private String memberType;


    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private String merchantCode;

    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;


}
