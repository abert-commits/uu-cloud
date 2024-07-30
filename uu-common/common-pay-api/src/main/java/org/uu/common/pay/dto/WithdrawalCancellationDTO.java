package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 提现取消原因配置表
*
* @author 
*/
    @Data
    @ApiModel(description = "提现取消原因返回")
    public class WithdrawalCancellationDTO implements Serializable {
    @ApiModelProperty("主键")
    private Long id;

            /**
            * 原因
            */
            @ApiModelProperty("原因")
    private String reason;

            /**
            * 排序
            */
            @ApiModelProperty("排序")
    private Integer sort;

            /**
            * 创建时间
            */
            @ApiModelProperty("创建时间")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

            /**
            * 创建人
            */
            @ApiModelProperty("创建人")
    private String createBy;

            /**
            * 修改时间
            */
            @ApiModelProperty("修改时间")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

            /**
            * 修改人
            */
            @ApiModelProperty("修改人")
    private String updateBy;


}