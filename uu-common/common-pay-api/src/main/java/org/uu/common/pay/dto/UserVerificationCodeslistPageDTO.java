package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员短信
 *
 * @author
 */
@Data
@ApiModel(description = "会员列表返回")
public class UserVerificationCodeslistPageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;


    /**
     * 用户标识
     */
    @ApiModelProperty("用户ID")
    private String userId;

    /**
     * 验证码
     */
    @ApiModelProperty("验证码")
    private String verificationCode;

    /**
     * 发送时间
     */
    @ApiModelProperty("发送时间")
    private LocalDateTime sendTime;

    /**
     * 过期时间
     */
    @ApiModelProperty("过期时间")
    private LocalDateTime expirationTime;

    /**
     * 验证码类型（短信SMS、邮箱EMAIL）
     */
    @ApiModelProperty("验证码类型")
    private String codeType;

    /**
     * 接收验证码账号
     */
    @ApiModelProperty("接收验证码账号")
    private String receiver;

    /**
     * IP地址
     */
    @ApiModelProperty("发送IP地址")
    private String ipAddress;


    /**
     * 操作设备
     */
    @ApiModelProperty("操作设备")
    private String device;
}