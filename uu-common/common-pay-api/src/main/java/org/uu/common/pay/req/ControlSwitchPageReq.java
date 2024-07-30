package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.uu.common.core.page.PageReturn;

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
public class ControlSwitchPageReq extends PageReturn implements Serializable {


}
