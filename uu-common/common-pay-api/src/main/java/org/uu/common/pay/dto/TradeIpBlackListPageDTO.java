package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "交易黑名单列表返回")
public class TradeIpBlackListPageDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    /**
     * 黑名单中的IP地址，不允许重复
     */
    @ApiModelProperty("IP地址")
    private String ipAddress;

    /**
     * 加入黑名单的原因
     */
    @ApiModelProperty("备注")
    private String reason;

    /**
     * 是否被删除（软删除），1表示已删除，0表示未删除
     */
    @ApiModelProperty("是否被删除（软删除），1表示已删除，0表示未删除")
    private Integer deleted;


    /**
     * 状态 默认启用
     */
    @ApiModelProperty(value = "状态 0禁用 1启用")
    private String status;


    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "更新人")
    private String updateBy;


}