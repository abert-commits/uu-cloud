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
 * <p>
 * 任务规则内容
 * </p>
 *
 * @author 
 * @since 2024-03-20
 */
@Data
@ApiModel(description = "任务规则内容")
public class TaskRulesContentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    /**
     * 任务规则
     */
    @ApiModelProperty("任务规则内容")
    private String taskRulesContent;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private String updateBy;


}
