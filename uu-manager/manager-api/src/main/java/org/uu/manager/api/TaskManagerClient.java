package org.uu.manager.api;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskManagerDTO;
import org.uu.common.pay.req.TaskManagerIdReq;
import org.uu.common.pay.req.TaskManagerListReq;
import org.uu.common.pay.req.TaskManagerReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author admin
 * @date 2024/3/19 9:52
 */
@FeignClient(value = "uu-wallet", contextId = "task-manager")
public interface TaskManagerClient {

    @PostMapping("/api/v1/taskManager/listPage")
    PageReturn<TaskManagerDTO> listPage(@RequestBody TaskManagerListReq req);

    @PostMapping("/api/v1/taskManager/taskDetail")
    RestResult<TaskManagerDTO>  taskDetail(@RequestBody TaskManagerIdReq req);

    @PostMapping("/api/v1/taskManager/createTask")
    RestResult<?>  createTask(@RequestBody TaskManagerReq req);

    @PostMapping("/api/v1/taskManager/deleteTask")
    RestResult<?> deleteTask(@RequestBody TaskManagerIdReq req);
    @PostMapping("/api/v1/taskManager/updateTask")
    RestResult<?> updateTask(@RequestBody TaskManagerReq req);
}
