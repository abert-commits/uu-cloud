package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TaskManagerDTO;
import org.uu.common.pay.req.TaskManagerIdReq;
import org.uu.common.pay.req.TaskManagerListReq;
import org.uu.common.pay.req.TaskManagerReq;
import org.uu.wallet.entity.TaskManager;
import org.uu.wallet.vo.PrizeWinnersVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务管理表 服务类
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
public interface ITaskManagerService extends IService<TaskManager> {
    PageReturn<TaskManagerDTO> listPage(TaskManagerListReq req);

    RestResult<?> createTask(TaskManagerReq req);

    boolean deleteTask(TaskManagerIdReq req);

    RestResult updateTask(TaskManagerReq req);

    RestResult<TaskManagerDTO> taskDetail(TaskManagerIdReq req);

    /**
     * 获取任务列表
     *
     * @return {@link List}<{@link PrizeWinnersVo}>
     */
    Map<String, List<TaskManager>> getTasks();


    /**
     * 根据任务id 获取任务信息
     *
     * @param taskId
     * @return {@link TaskManager}
     */
    TaskManager getTaskDetailsById(Long taskId);

    /**
     * 根据任务类型 获取任务信息
     *
     * @param taskType
     * @return {@link TaskManager}
     */
    TaskManager getTaskDetailsByType(String taskType);


    /**
     * 获取买入卖出任务列表
     *
     * @return {@link Map}<{@link String}, {@link List}<{@link TaskManager}>>
     */
    Map<String, List<TaskManager>> fetchBuyAndSellTaskList();

    /**
     * 获取激活的任务类型
     *
     * @return
     */
    List<TaskManager> getActiveTaskTypes();

}
