package org.uu.wallet.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberTaskStatus;
import org.uu.wallet.entity.TaskCollectionRecord;
import org.uu.wallet.entity.TaskManager;
import org.uu.wallet.service.*;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.vo.MemberTaskVo;
import org.uu.wallet.vo.TaskCenterVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 任务中心 服务类
 *
 * @author Simon
 * @date 2024/03/20
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ITaskCenterServiceImpl implements ITaskCenterService {


    @Autowired
    private ITaskCollectionRecordService taskCollectionRecordService;


    @Autowired
    private ITaskManagerService taskManagerService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;


    @Autowired
    private RedissonUtil redissonUtil;

    @Autowired
    private IMemberInfoService memberInfoService;

    @Autowired
    private IMemberTaskStatusService memberTaskStatusService;

    private static final String TASK_COMPLETE_KEY_PREFIX = RedisKeys.TASK_COMPLETED; // Redis中存储任务完成状态的key前缀


    /**
     * 获取任务中心页面数据
     *
     * @return {@link RestResult}<{@link TaskCenterVo}>
     */
    @Override
    public RestResult<TaskCenterVo> fetchTaskCenterDetails() {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取获取任务中心页面数据失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        TaskCenterVo taskCenterVo = new TaskCenterVo();

        //累计任务奖励金额 领取任务的时候 将该值统计在会员信息里面
        taskCenterVo.setTotalTaskRewards(memberInfo.getTotalTaskRewards());

        //获取任务列表
        Map<String, List<TaskManager>> tasks = taskManagerService.getTasks();

        //新人任务列表 查询任务列表 需要区分是否完成
        List<TaskManager> newUserTaskList = tasks.get("newUserTasks");

        ArrayList<MemberTaskVo> newUserTasksVoList = new ArrayList<>();

        if (newUserTaskList != null && newUserTaskList.size() > 0) {
            for (TaskManager newUserTask : newUserTaskList) {
                // 实名认证类型：未认证任务状态未完成；领取记录存在时状态已完成
                if (RewardTaskTypeEnum.REAL_AUTH.getCode().equals(newUserTask.getTaskType())) {
                    MemberTaskVo beginnerTasksVo = new MemberTaskVo();
                    BeanUtils.copyProperties(newUserTask, beginnerTasksVo);
                    beginnerTasksVo.setTaskId(newUserTask.getId());
                    if (MemberAuthenticationStatusEnum.UNAUTHENTICATED.getCode().equals(memberInfo.getAuthenticationStatus())) {
                        beginnerTasksVo.setTaskStatus(MemberTaskStatusEnum.NOT_FINISH.getCode());
                    } else {
                        boolean collectedReward = taskCollectionRecordService.checkTaskCompletedByMember(memberInfo.getId(), newUserTask.getId());
                        beginnerTasksVo.setTaskStatus(collectedReward ? MemberTaskStatusEnum.FINISHED.getCode() : MemberTaskStatusEnum.TO_COLLECT.getCode());
                    }
                    newUserTasksVoList.add(beginnerTasksVo);
                } else if (RewardTaskTypeEnum.STARTER_QUESTS_BUY.getCode().equals(newUserTask.getTaskType())) {
                    newUserTasksVoList.add(buildNewUserGuideTask(newUserTask, memberInfo.getId(), memberInfo.getBuyGuideStatus()));
                } else if (RewardTaskTypeEnum.STARTER_QUESTS_SELL.getCode().equals(newUserTask.getTaskType())) {
                    newUserTasksVoList.add(buildNewUserGuideTask(newUserTask, memberInfo.getId(), memberInfo.getSellGuideStatus()));
                } else {
                    log.info("获取获取任务中心页面数据, 不支持的任务类型:{}", newUserTask.getTaskType());
                }
            }
        }

        //新人任务列表
        taskCenterVo.setBeginnerTasks(sort(newUserTasksVoList));

        //每日任务列表 查询任务列表 需要区分是否完成
        List<TaskManager> dailyTaskList = tasks.get("dailyTasks");

        ArrayList<MemberTaskVo> dailyTaskVoList = new ArrayList<>();

        if (dailyTaskList != null && dailyTaskList.size() > 0) {
            // 查询会员当天的任务状态, 计算每日任务完成情况
            List<MemberTaskStatus> memberTaskStatuses = memberTaskStatusService.queryMemberTodayTaskStatus(memberInfo.getId());
            Map<Long, MemberTaskStatus> memberTaskMap = Maps.newHashMap();
            if (!Collections.isEmpty(memberTaskStatuses)) {
                memberTaskMap = memberTaskStatuses.stream().collect(Collectors.toMap(MemberTaskStatus::getTaskId, v -> v, (k1, k2) -> k1));
            }
            for (TaskManager dailyTask : dailyTaskList) {
                MemberTaskVo dailyTaskVo = new MemberTaskVo();
                BeanUtils.copyProperties(dailyTask, dailyTaskVo);
                dailyTaskVo.setTaskId(dailyTask.getId());
                dailyTaskVo.setTaskCurrentNum(getTaskCurrentValue(memberInfo, dailyTaskVo));
                MemberTaskStatus memberTaskStatus = memberTaskMap.get(dailyTask.getId());
                // 当日没有该类型的任务状态记录，说明此项任务玩家未完成
                if (memberTaskStatus == null) {
                    dailyTaskVo.setTaskStatus(MemberTaskStatusEnum.NOT_FINISH.getCode());
                } else if (memberTaskStatus.getRewardClaimed() != null && memberTaskStatus.getRewardClaimed() == RewardCollectStatusEnum.COLLECTED.getCode()) {
                    dailyTaskVo.setTaskStatus(MemberTaskStatusEnum.FINISHED.getCode());
                } else {
                    dailyTaskVo.setTaskStatus(MemberTaskStatusEnum.TO_COLLECT.getCode());
                }
                dailyTaskVoList.add(dailyTaskVo);
            }
        }

        //每日任务列表
        taskCenterVo.setDailyTaskList(sort(dailyTaskVoList));

        //领奖会员列表
        taskCenterVo.setPrizeWinners(taskCollectionRecordService.getPrizeWinnersList());

        return RestResult.ok(taskCenterVo);

    }


    /**
     * 记录任务的两个关键状态到Redis：是否达到领取条件和奖励是否已经被领取
     *
     * @param memberId      会员ID
     * @param taskId        任务ID，例如"realNameAuth"表示实名认证任务
     * @param conditionMet  会员是否达到任务的领取条件
     * @param rewardClaimed 任务奖励是否已被领取
     */
    public void recordTaskConditionAndReward(String memberId, String taskId, boolean conditionMet, boolean rewardClaimed) {
        String key = "taskStatus:" + memberId;
        // 将会员是否达到领取条件的状态存储到Redis的Hash中
        redisTemplate.opsForHash().put(key, taskId + ":conditionMet", String.valueOf(conditionMet));
        // 将会员奖励是否已被领取的状态存储到Redis的Hash中
        redisTemplate.opsForHash().put(key, taskId + ":rewardClaimed", String.valueOf(rewardClaimed));
    }


    /**
     * 检查会员对于特定任务的状态，包括是否达到领取条件和奖励是否已被领取
     *
     * @param memberId 会员ID
     * @param taskId   任务ID
     * @return 包含两个状态的Map：{"conditionMet": true/false, "rewardClaimed": true/false}
     */
    public Map<String, Boolean> checkTaskStatus(String memberId, String taskId) {
        String key = "taskStatus:" + memberId;
        Map<String, Boolean> status = new HashMap<>();
        // 从Redis的Hash中获取会员是否达到领取条件的状态
        status.put("conditionMet", "true".equals(redisTemplate.opsForHash().get(key, taskId + ":conditionMet")));
        // 从Redis的Hash中获取会员奖励是否已被领取的状态
        status.put("rewardClaimed", "true".equals(redisTemplate.opsForHash().get(key, taskId + ":rewardClaimed")));
        return status;
    }


    /**
     * 查看会员是否完成过任务 true 已完成过该任务
     *
     * @param memberId
     * @param taskId
     * @return boolean
     */
    @Override
    public boolean hasMemberCompletedTask(Long memberId, Long taskId) {
        String key = TASK_COMPLETE_KEY_PREFIX + memberId + ":" + taskId;
        // 尝试从Redis获取任务完成状态
        return redisTemplate.opsForValue().get(key) != null;
    }

    @Override
    public boolean tryCompleteTask(Long memberId, Long taskId, TaskCollectionRecord taskCollectionRecord) {
        return false;
    }


    /**
     * 查看会员是否完成过任务
     *
     * @param memberId  会员ID
     * @param taskId    任务ID
     * @param taskCycle 任务周期
     * @return boolean 是否已完成
     */
    public boolean hasMemberCompletedTask(Long memberId, Long taskId, String taskCycle) {
        String key = TASK_COMPLETE_KEY_PREFIX + memberId;
        String taskCompletionDate = (String) redisTemplate.opsForHash().get(key, taskId.toString());

        if (taskCycle.equals("1")) { // 一次性任务
            return taskCompletionDate != null;
        } else if (taskCycle.equals("2")) { // 每日任务
            // 检查任务完成日期是否是今天
            return taskCompletionDate != null && taskCompletionDate.equals(LocalDate.now().toString());
        }
        return false;
    }

    /**
     * 记录任务完成状态到Redis
     *
     * @param memberId 会员ID
     * @param taskId 任务ID
     */
//    public void recordTaskCompletion(Long memberId, Long taskId) {
//        String key = TASK_COMPLETE_KEY_PREFIX + memberId;
//        updateRedisWithRetry(key, taskId.toString(), LocalDate.now().toString());
//    }


    /**
     * 完成任务处理
     * 1.将领取记录到redis
     * 2.将领取记录到mysql
     *
     * @param memberId
     * @param taskId
     * @return boolean
     */
//    @Transactional
//    @Override
//    public boolean tryCompleteTask(Long memberId, Long taskId, TaskCollectionRecord taskCollectionRecord) {
//
//        //添加分布式锁
//        //分布式锁key ar-wallet-tryCompleteTask+会员id
//        String lockKey = "ar-wallet-tryCompleteTask" + memberId;
//        RLock lock = redissonUtil.getLock(lockKey);
//
//        boolean req = false;
//
//        try {
//            req = lock.tryLock(10, TimeUnit.SECONDS);
//
//            if (req) {
//
//                String redisKey = TASK_COMPLETE_KEY_PREFIX + memberId + ":" + taskId;
//                // 尝试从Redis获取任务完成状态
//                Boolean isCompleted = redisTemplate.opsForValue().get(redisKey) != null;
//
//                if (Boolean.TRUE.equals(isCompleted)) {
//                    // 如果Redis中存在标记，则表示任务已完成
//                    return true; // 返回任务已完成的状态
//                } else {
//                    // 如果Redis中没有找到标记，进一步查询数据库确认
//                    boolean completedInDB = taskCollectionRecordService.checkTaskCompletedByMember(memberId, taskId);
//                    if (!completedInDB) {
//
//                        // 如果数据库中也未标记为完成，则允许领取任务
//
//                        // 将领取记录到redis
//                        updateRedisWithRetry(redisKey, "completed");
//
//                        // 2.将领取记录到mysql
//                        taskCollectionRecordService.recordMemberTaskCompletion(taskCollectionRecord);
//
//                        //TODO 记录会员账变
//
//                        //成功返回 true  失败返回false
//                        return true;
//                    } else {
//                        // 如果数据库中已标记为完成，同样将此状态更新至Redis
//                        updateRedisWithRetry(redisKey, "completed");
//                        return true; // 任务已完成，不允许重复领取
//                    }
//                }
//            }
//        } catch (Exception e) {
//            //手动回滚
//            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//            log.error("领取任务奖励处理失败 会员id: {}, 任务id: {}, 任务信息: {}, e: {}", memberId, taskId, taskCollectionRecord, e.getMessage());
//            return false;
//        } finally {
//            //释放锁
//            if (req && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//            }
//        }
//        return false;
//    }


    /**
     * 将领取记录添加到redis 使用重试机制
     *
     * @param key
     * @param value
     */
    private void updateRedisWithRetry(String key, String value) {
        redisUtil.retryTemplate(() -> redisTemplate.opsForValue().set(key, value), 3, 100);
    }

    /**
     * 按业务规则对任务排序
     *
     * @param sourceList
     * @return
     */
    private List<MemberTaskVo> sort(List<MemberTaskVo> sourceList) {
        if (Collections.isEmpty(sourceList)) {
            return sourceList;
        }
        // 完成状态的任务放到最后
        List<MemberTaskVo> resultList = Lists.newArrayListWithExpectedSize(sourceList.size());
        List<MemberTaskVo> finishList = Lists.newArrayListWithExpectedSize(sourceList.size());
        sourceList.forEach(data -> {
            if (MemberTaskStatusEnum.FINISHED.getCode() == data.getTaskStatus()) {
                finishList.add(data);
            } else {
                resultList.add(data);
            }
        });
        if (!Collections.isEmpty(finishList)) {
            resultList.addAll(finishList);
        }
        return resultList;
    }

    private String getTaskCurrentValue(MemberInfo memberInfo, MemberTaskVo dailyTaskVo) {
        if (RewardTaskTypeEnum.BUY.getCode().equals(dailyTaskVo.getTaskType()) && TaskTargetTypeEnum.TIMES.getCode().equals(dailyTaskVo.getTaskTarget())) {
            return Optional.ofNullable(memberInfo.getTodayBuySuccessCount()).orElse(0).toString();
        } else if (RewardTaskTypeEnum.BUY.getCode().equals(dailyTaskVo.getTaskType()) && TaskTargetTypeEnum.MONEY.getCode().equals(dailyTaskVo.getTaskTarget())) {
            return Optional.ofNullable(memberInfo.getTodayBuySuccessAmount()).orElse(BigDecimal.ZERO).toString();
        } else if (RewardTaskTypeEnum.SELL.getCode().equals(dailyTaskVo.getTaskType()) && TaskTargetTypeEnum.TIMES.getCode().equals(dailyTaskVo.getTaskTarget())) {
            return Optional.ofNullable(memberInfo.getTodaySellSuccessCount()).orElse(0).toString();
        } else if (RewardTaskTypeEnum.SELL.getCode().equals(dailyTaskVo.getTaskType()) && TaskTargetTypeEnum.MONEY.getCode().equals(dailyTaskVo.getTaskTarget())) {
            return Optional.ofNullable(memberInfo.getTodaySellSuccessAmount()).orElse(BigDecimal.ZERO).toString();
        }
        return null;
    }

    private MemberTaskVo buildNewUserGuideTask(TaskManager newUserTask, long memberId, Integer guideStatus){
        MemberTaskVo beginnerTasksVo = new MemberTaskVo();
        BeanUtils.copyProperties(newUserTask, beginnerTasksVo);
        beginnerTasksVo.setTaskId(newUserTask.getId());
        if (guideStatus == 0) {
            beginnerTasksVo.setTaskStatus(MemberTaskStatusEnum.NOT_FINISH.getCode());
        } else {
            boolean collectedReward = taskCollectionRecordService.checkTaskCompletedByMember(memberId, newUserTask.getId());
            beginnerTasksVo.setTaskStatus(collectedReward ? MemberTaskStatusEnum.FINISHED.getCode() : MemberTaskStatusEnum.TO_COLLECT.getCode());
        }
        return beginnerTasksVo;
    }


}
