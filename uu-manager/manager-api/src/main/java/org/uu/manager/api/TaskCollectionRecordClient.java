package org.uu.manager.api;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskCollectionRecordDTO;
import org.uu.common.pay.dto.TaskManagerDTO;
import org.uu.common.pay.req.TaskCollectionRecordReq;
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
@FeignClient(value = "uu-wallet", contextId = "task-collection-record")
public interface TaskCollectionRecordClient {

    @PostMapping("/api/v1/taskCollectionRecord/listPage")
    PageReturn<TaskCollectionRecordDTO> listPage(@RequestBody TaskCollectionRecordReq req);
    @PostMapping("/api/v1/taskCollectionRecord/getStatisticsData")
    TaskCollectionRecordDTO getStatisticsData();
}
