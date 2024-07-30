package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 充值取消原因
*
* @author 
*/
    @Data
    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    @ApiModel(description = "取消原因返回")
    public class CancellationRechargeDTO implements Serializable {
    @ApiModelProperty("主键")
    private long id;

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
    private LocalDateTime createTime;

            /**
            * 创建人
            */
            @ApiModelProperty("创建人")
    private String createBy;

            /**
            * 更新时间
            */
            @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

            /**
            * 修改人
            */
            @ApiModelProperty("修改人")
    private String updateBy;


}