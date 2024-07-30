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
 * 后台控制开关表
 * </p>
 *
 * @author 
 * @since 2024-03-21
 */
@Data
@ApiModel(description = "后台开关")
public class ControlSwitchReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开关id
     */
    private Long switchId;

    /**
     * 开关名称
     */
    private String switchName;

    /**
     * 开关描述
     */
    private String switchDescription;

    /**
     * 状态（1为启用，0为禁用）
     */
    private Integer status;

}
