package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员操作日志表
 *
 * @author
 * @since 2024-01-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_operation_logs")
public class MemberOperationLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private Long memberId;


    /**
     * 会员账号
     */
    private String username;


    /**
     * 会员类型
     */
    private String memberType;


    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;


    /**
     * 操作ip
     */
    private String ipAddress;


    /**
     * 操作设备
     */
    private String device;


    /**
     * 用户代理（浏览器或设备信息）
     */
    private String userAgent;


    /**
     * 操作模块
     */
    private String module;


    /**
     * 操作路径
     */
    private String operationPath;


    /**
     * 请求路径
     */
    private String requestPath;


    /**
     * 方法名
     */
    private String methodName;


    /**
     * 请求方式
     */
    private String httpMethod;


    /**
     * 请求参数
     */
    private String parameters;


    /**
     * 返回数据
     */
    private String response;


    /**
     * 请求ip
     */
    private String requestIp;


    /**
     * 执行时长
     */
    private Long duration;


    /**
     * 方法注释
     */
    private String methodComment;


    /**
     * 操作模块code
     */
    private String moduleCode;

}
