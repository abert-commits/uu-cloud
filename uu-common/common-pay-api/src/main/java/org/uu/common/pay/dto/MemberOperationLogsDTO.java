package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
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
public class MemberOperationLogsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty(value = "会员ID")
    private Long memberId;


    /**
     * 会员账号
     */
    @ApiModelProperty(value = "会员账号")
    private String username;


    /**
     * 会员类型
     */
    @ApiModelProperty(value = "会员类型")
    private String memberType;


    /**
     * 操作时间
     */
    @ApiModelProperty(value = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;


    /**
     * 操作ip
     */
    @ApiModelProperty(value = "操作ip")
    private String ipAddress;


    /**
     * 操作设备
     */
    @ApiModelProperty(value = "操作设备")
    private String device;



    /**
     * 操作模块
     */
    @ApiModelProperty(value = "操作模块")
    private String module;


    /**
     * 操作路径
     */
    @ApiModelProperty(value = "操作路径")
    private String operationPath;

    /**
     * moduleCode
     */
    @ApiModelProperty(value = "模块code")
    private String moduleCode;


}
