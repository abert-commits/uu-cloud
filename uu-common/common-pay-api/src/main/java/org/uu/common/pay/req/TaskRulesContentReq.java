package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
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
public class TaskRulesContentReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 任务规则
     */
    private String taskRulesContent;

}
