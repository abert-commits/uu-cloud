package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取任务规则详情返回数据")
public class TaskRuleDetailsVo implements Serializable {

    /**
     * 任务规则内容
     */
    @ApiModelProperty(value = "任务规则内容")
    private String taskRulesContent;
}