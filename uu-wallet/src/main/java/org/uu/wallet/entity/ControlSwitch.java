package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@TableName("control_switch")
public class ControlSwitch extends BaseEntityOrder implements Serializable {

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
