package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 后台控制开关表
 * </p>
 *
 * @author 
 * @since 2024-03-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "开关信息")
public class ControlSwitchDTO implements Serializable {

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
