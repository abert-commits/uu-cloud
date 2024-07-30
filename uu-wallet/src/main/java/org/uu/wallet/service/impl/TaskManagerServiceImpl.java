

package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.TaskManagerDTO;
import org.uu.common.pay.req.TaskManagerIdReq;
import org.uu.common.pay.req.TaskManagerListReq;
import org.uu.common.pay.req.TaskManagerReq;
import org.uu.wallet.Enum.RewardTaskCycleEnum;
import org.uu.wallet.Enum.RewardTaskTypeEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.TaskManager;
import org.uu.wallet.mapper.TaskManagerMapper;
import org.uu.wallet.service.ITaskManagerService;
import org.uu.wallet.vo.PrizeWinnersVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务管理表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskManagerServiceImpl extends ServiceImpl<TaskManagerMapper, TaskManager> implements ITaskManagerService {

    private final WalletMapStruct walletMapStruct;

    @Value("${oss.baseUrl}")
    private String baseUrl;

    @Override
    public PageReturn<TaskManagerDTO> listPage(TaskManagerListReq req) {
        LambdaQueryChainWrapper<TaskManager> lambdaQuery = lambdaQuery();
        Page<TaskManager> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        lambdaQuery.eq(TaskManager::getDeleted, 0);
        lambdaQuery.orderByAsc(TaskManager::getTaskSort);

        if (Objects.nonNull(req.getTaskName()) && StringUtils.isNotBlank(req.getTaskName())) {
            lambdaQuery.like(TaskManager::getTaskTitle, req.getTaskName());
        }

        if (Objects.nonNull(req.getTaskType()) && StringUtils.isNotBlank(req.getTaskType())) {
            lambdaQuery.eq(TaskManager::getTaskType, req.getTaskType());
        }

        if (Objects.nonNull(req.getTaskCycle()) && StringUtils.isNotBlank(req.getTaskCycle())) {
            lambdaQuery.eq(TaskManager::getTaskCycle, req.getTaskCycle());
        }

        if (Objects.nonNull(req.getTaskTarget()) && StringUtils.isNotBlank(req.getTaskTarget())) {
            lambdaQuery.eq(TaskManager::getTaskTarget, req.getTaskTarget());
        }

        if (Objects.nonNull(req.getTaskStatus()) && StringUtils.isNotBlank(req.getTaskStatus())) {
            lambdaQuery.eq(TaskManager::getTaskStatus, req.getTaskStatus());
        }
        baseMapper.selectPage(page, lambdaQuery.getWrapper());

        List<TaskManager> records = page.getRecords();
        List<TaskManagerDTO> taskManagerDtoList = walletMapStruct.taskListToDto(records);

        Page<TaskManagerDTO> pageResult = new Page<>();
        pageResult.setCurrent(req.getPageNo());
        pageResult.setSize(req.getPageSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(taskManagerDtoList);
        return PageUtils.flush(pageResult, taskManagerDtoList);
    }

    @Override
    public RestResult<?> createTask(TaskManagerReq req) {
        // 参数检查
        if (!paramsCheck(req)) {
            return RestResult.failure(ResultCode.PARAM_IS_EMPTY_OR_ERROR);
        }
        // 是否已存在该类型的未删除的任务
        if (!checkIsExistenceSamOpenTask(req.getTaskType(), null, req.getTaskTarget())) {
            return RestResult.failure(ResultCode.TASK_IS_ACTIVATED);
        }
        int count = lambdaQuery()
                .eq(TaskManager::getTaskSort, req.getTaskSort())
                .eq(TaskManager::getDeleted, 0)
                .count();
        if (count > 0) {
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }
        TaskManager taskManager = new TaskManager();
        BeanUtils.copyProperties(req, taskManager);
        String iconUrl = getIconUrl(taskManager.getTaskIcon());
        taskManager.setTaskIcon(iconUrl);
        if (save(taskManager)) {
            return RestResult.ok();
        }
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    @Override
    public boolean deleteTask(TaskManagerIdReq req) {
        return lambdaUpdate().eq(TaskManager::getId, req.getId()).set(TaskManager::getDeleted, 1).set(TaskManager::getTaskStatus, 0).update();
    }

    @Override
    public RestResult updateTask(TaskManagerReq req) {
        // 参数检查
        if (!paramsCheck(req)) {
            return RestResult.failure(ResultCode.PARAM_IS_EMPTY_OR_ERROR);
        }
        int count = lambdaQuery()
                .eq(TaskManager::getTaskSort, req.getTaskSort())
                .ne(TaskManager::getId, req.getId())
                .eq(TaskManager::getDeleted, 0)
                .count();
        if (count > 0) {
            return RestResult.failure(ResultCode.SORT_ORDER_DUPLICATED);
        }
        if (!checkIsExistenceSamOpenTask(req.getTaskType(), req.getId(), req.getTaskTarget())) {
            return RestResult.failure(ResultCode.TASK_IS_ACTIVATED);
        }
        TaskManager task = new TaskManager();
        BeanUtils.copyProperties(req, task);
        String iconUrl = getIconUrl(task.getTaskIcon());
        task.setTaskIcon(iconUrl);
        boolean update = updateById(task);
        return update ? RestResult.ok() : RestResult.failed();
    }

    @Override
    public RestResult<TaskManagerDTO> taskDetail(TaskManagerIdReq req) {
        if (Objects.isNull(req.getId())) {
            return RestResult.failure(ResultCode.MERCHANT_WRONG_ID);
        }
        TaskManager taskManager = baseMapper.selectById(req.getId());
        return RestResult.ok(change(taskManager));
    }

    private TaskManagerDTO change(TaskManager taskManager) {
        if (taskManager == null) {
            return null;
        }
        TaskManagerDTO taskManagerDTO = new TaskManagerDTO();
        BeanUtils.copyProperties(taskManager, taskManagerDTO);
        return taskManagerDTO;
    }

    private String getIconUrl(String icon) {
        if (icon != null && !icon.startsWith("https://")) {
            // 如果不是以"http"开头，则进行拼接
            icon = baseUrl + icon;
        }
        return icon;
    }

    private boolean paramsCheck(TaskManagerReq req) {
        if (
                Objects.isNull(req.getTaskType()) || StringUtils.isBlank(req.getTaskType()) ||
                        Objects.isNull(req.getTaskCycle()) || StringUtils.isBlank(req.getTaskCycle()) ||
                        Objects.isNull(req.getTaskReward()) ||
                        Objects.isNull(req.getTaskStatus()) || StringUtils.isBlank(req.getTaskStatus()) ||
                        Objects.isNull(req.getTaskTitle()) || StringUtils.isBlank(req.getTaskTitle()) ||
                        Objects.isNull(req.getTaskSort()) ||
                        Objects.isNull(req.getTaskIcon()) || StringUtils.isBlank(req.getTaskIcon()) ||
                        Objects.isNull(req.getTaskJumpLink()) || StringUtils.isBlank(req.getTaskJumpLink())
        ) {
            return false;
        }
        // 周期和类型检查
        List<String> cycleTaskCode = RewardTaskTypeEnum.getCycleTaskCode();
        List<String> onceTaskCode = RewardTaskTypeEnum.getOnceTaskCode();
        if (onceTaskCode.contains(req.getTaskType())) {
            if (!Objects.equals(req.getTaskCycle(), RewardTaskCycleEnum.ONCE.getCode())) {
                return false;
            }
        }
        // 周期任务检查
        if (cycleTaskCode.contains(req.getTaskType())) {
            // 周期性和类型判断
            if (!Objects.equals(req.getTaskCycle(), RewardTaskCycleEnum.DAY_CYCLE.getCode())) {
                return false;
            }
            // 周期性任务必填参数
            return !Objects.isNull(req.getTaskTarget()) && !StringUtils.isBlank(req.getTaskTarget()) &&
                    !Objects.isNull(req.getTaskTargetNum());
        }
        return true;
    }


    /**
     * 获取任务列表
     *
     * @return {@link List}<{@link PrizeWinnersVo}>
     */
    @Override
    public Map<String, List<TaskManager>> getTasks() {


        // 查询所有启用状态的任务
        List<TaskManager> tasks = lambdaQuery()
                .eq(TaskManager::getTaskStatus, "1") // 任务状态 1-启用
                .orderByAsc(TaskManager::getTaskSort) // 根据排序权重进行排序 数字小排在前
                .select(TaskManager::getId, TaskManager::getTaskType, TaskManager::getTaskCycle, TaskManager::getTaskSort, TaskManager::getTaskTitle,
                        TaskManager::getTaskSubTitle, TaskManager::getTaskIcon, TaskManager::getTaskJumpLink,
                        TaskManager::getTaskReward, TaskManager::getTaskTarget, TaskManager::getTaskTargetNum)
                .list();


        // 使用Java 8的Stream API分离新人任务列表和每日任务列表
        List<TaskManager> newUserTasks = tasks.stream()
                .filter(task -> "1".equals(task.getTaskCycle())) //任务周期：1-一次性
                .collect(Collectors.toList());

        List<TaskManager> dailyTasks = tasks.stream()
                .filter(task -> "2".equals(task.getTaskCycle())) //任务周期：2-每天
                .collect(Collectors.toList());


        // 将两个列表放入Map中返回
        Map<String, List<TaskManager>> taskLists = new HashMap<>();
        taskLists.put("newUserTasks", newUserTasks);
        taskLists.put("dailyTasks", dailyTasks);

        return taskLists;
    }


    private boolean checkIsExistenceSamOpenTask(String taskType, Long id, String taskTarget) {
        // 周期性任务不做重复限制
        List<String> cycleTaskCode = RewardTaskTypeEnum.getCycleTaskCode();
        if(cycleTaskCode.contains(taskType)){
            return true;
        }
        LambdaQueryChainWrapper<TaskManager> wrapper = lambdaQuery().eq(TaskManager::getTaskType, taskType)
                .eq(TaskManager::getDeleted, 0);
//        // 周期性任务可以根据任务目标类型判断
//        if (!Objects.isNull(taskTarget)) {
//            wrapper.eq(TaskManager::getTaskTarget, taskTarget);
//        }
        TaskManager one = wrapper.one();
        // 不存在未删除已启用的任务直接通过
        if (one == null) {
            return true;
        } else {
            if (id == null) {
                return false;
            } else {
                return one.getId().equals(id);
            }
        }
    }


    /**
     * 根据任务id 获取任务信息
     *
     * @param taskId
     * @return {@link TaskManager}
     */
    @Override
    public TaskManager getTaskDetailsById(Long taskId) {
        return lambdaQuery()
                .eq(TaskManager::getId, taskId)
                .eq(TaskManager::getDeleted, 0)
                .eq(TaskManager::getTaskStatus, 1)
                .one();
    }


    /**
     * 根据任务类型 获取任务信息
     *
     * @param taskType
     * @return {@link TaskManager}
     */
    @Override
    public TaskManager getTaskDetailsByType(String taskType) {
        return lambdaQuery()
                .eq(TaskManager::getTaskType, taskType)
                .eq(TaskManager::getDeleted, 0)//只要存在任务 不管有没有开启任务 都要完成实名认证任务 只是不能领取奖励
                .one();
    }


    /**
     * 获取买入卖出任务列表
     *
     * @return {@link Map}<{@link String}, {@link List}<{@link TaskManager}>>
     */
    public Map<String, List<TaskManager>> fetchBuyAndSellTaskList() {
        // 获取所有未删除、状态开启的每天任务
        List<TaskManager> tasks = lambdaQuery()
                .eq(TaskManager::getDeleted, 0)
                .eq(TaskManager::getTaskStatus, 1)
                .eq(TaskManager::getTaskCycle, 2)
                .list();

        // 防止空指针异常，确保tasks不为null
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        // 根据任务类型分组
        Map<String, List<TaskManager>> groupedTasks = tasks.stream()
                .collect(Collectors.groupingBy(task -> RewardTaskTypeEnum.BUY.getCode().equals(task.getTaskType()) ? "buy" : "sell"));

        // 确保每个键都有对应的列表
        groupedTasks.putIfAbsent("buy", new ArrayList<>());
        groupedTasks.putIfAbsent("sell", new ArrayList<>());

        return groupedTasks;
    }

    /**
     * 获取激活的任务类型
     *
     * @return
     */
    public List<TaskManager> getActiveTaskTypes() {
        return lambdaQuery()
                .eq(TaskManager::getTaskStatus, "1") // 任务状态 1-启用
                .select(TaskManager::getTaskType, TaskManager::getTaskReward)
                .list();
    }
}
