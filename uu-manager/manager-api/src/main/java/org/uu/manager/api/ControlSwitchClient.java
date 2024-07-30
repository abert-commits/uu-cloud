package org.uu.manager.api;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.ControlSwitchDTO;
import org.uu.common.pay.req.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "control-switch")
public interface ControlSwitchClient {
    /**
     * 开关列表
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/controlSwitch/listPage")
    RestResult listPage(@RequestBody ControlSwitchPageReq req);

    /**
     * 详情
     *
     * @param
     * @param
     * @return
     */
    @PostMapping("/api/v1/controlSwitch/createControlSwitch")
    RestResult<ControlSwitchDTO> createControlSwitch(@RequestBody ControlSwitchReq req);

    @PostMapping("/api/v1/controlSwitch/updateControlSwitchInfo")
    RestResult<ControlSwitchDTO> updateControlSwitchInfo(@RequestBody ControlSwitchUpdateReq req);

    @PostMapping("/api/v1/controlSwitch/updateControlSwitchStatus")
    RestResult<ControlSwitchDTO> updateControlSwitchStatus(@RequestBody ControlSwitchStatusReq req);


    @PostMapping("/api/v1/controlSwitch/detail")
    RestResult<ControlSwitchDTO> detail(ControlSwitchIdReq req);
}
