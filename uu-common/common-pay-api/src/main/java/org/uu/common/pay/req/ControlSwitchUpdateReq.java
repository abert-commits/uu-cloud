package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

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
@ApiModel(description = "后台开关")
public class ControlSwitchUpdateReq implements Serializable {

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


}
