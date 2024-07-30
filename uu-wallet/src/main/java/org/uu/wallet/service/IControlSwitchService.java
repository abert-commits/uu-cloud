package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.ControlSwitchDTO;
import org.uu.common.pay.req.*;
import org.uu.wallet.entity.ControlSwitch;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 后台控制开关表 服务类
 * </p>
 *
 * @author 
 * @since 2024-03-21
 */
public interface IControlSwitchService extends IService<ControlSwitch> {
    RestResult<ControlSwitchDTO>  createControlSwitch(ControlSwitchReq req);
    RestResult<ControlSwitchDTO>  updateControlSwitchInfo(ControlSwitchUpdateReq req);
    RestResult<ControlSwitchDTO>  updateControlSwitchStatus(ControlSwitchStatusReq req);
    RestResult<ControlSwitchDTO>  detail(ControlSwitchIdReq req);
    PageReturn<ControlSwitchDTO> listPage(ControlSwitchPageReq req);


    /**
     * 检查指定开关是否开启
     *
     * @param switchId
     * @return boolean
     */
    boolean isSwitchEnabled(Long switchId);
}
