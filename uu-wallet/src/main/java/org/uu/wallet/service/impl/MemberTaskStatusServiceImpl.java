package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.MemberTaskStatusMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.*;
import org.uu.wallet.util.OrderNumberGeneratorUtil;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 会员任务状态表, 记录会员完成任务和领取奖励的状态 服务实现类
 * </p>
 *
 * @author
 * @since 2024-03-22
 */
@Service
@Slf4j
public class MemberTaskStatusServiceImpl extends ServiceImpl<MemberTaskStatusMapper, MemberTaskStatus> implements IMemberTaskStatusService {

    @Autowired
    private RedissonUtil redissonUtil;

    @Autowired
    private OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private RabbitMQService rabbitMQService;

    /**
     * 处理每日买入任务
     *
     * @param memberInfo
     * @param taskManager
     * @return boolean
     */
    @Override
    public boolean handleDailyBuyTask(MemberInfo memberInfo, TaskManager taskManager) {

        Integer taskType = Integer.valueOf(RewardTaskTypeEnum.BUY.getCode());

        //分布式锁key ar-wallet-handleDailyTask+会员id
        String key = "uu-wallet-handleDailyTask" + memberInfo.getId();
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {
                // 查看是否完成了任务

                //任务目标类型 1-次数 2-金额
                int taskTargetNum = "1".equals(taskManager.getTaskTarget())
                        ? memberInfo.getTodayBuySuccessCount()
                        : memberInfo.getTodayBuySuccessAmount().intValue();

                if (taskTargetNum >= taskManager.getTaskTargetNum()) {
                    //完成任务

                    log.info("处理每日买入任务: 完成任务 会员id: {}", memberInfo.getId());


                    LocalDate today = LocalDate.now();
                    // 检查是否已存在今日首次卖出的记录
                    long existingTaskCount = lambdaQuery()
                            .eq(MemberTaskStatus::getMemberId, memberInfo.getId())
                            .eq(MemberTaskStatus::getTaskId, taskManager.getId())
                            .eq(MemberTaskStatus::getCompletionDate, today)
                            .count();

                    if (existingTaskCount > 0) {
                        // 如果已存在记录，则表示今日任务已完成，不做处理
                        return true;
                    } else {
                        // 不存在，表示今日首次卖出，记录任务完成
                        MemberTaskStatus newRecord = new MemberTaskStatus();
                        newRecord.setMemberId(memberInfo.getId());//会员id
                        newRecord.setTaskType(taskType);//任务类型
                        newRecord.setTaskId(taskManager.getId());//任务id
                        newRecord.setCompletionStatus(1); // 任务完成
                        newRecord.setRewardClaimed(0); // 奖励未领取
                        newRecord.setCompletionDate(today);//任务完成日期 今日
                        newRecord.setOrderNo(orderNumberGenerator.generateOrderNo("RW"));//任务订单号
                        newRecord.setCreateTime(LocalDateTime.now());//任务完成时间
                        newRecord.setTaskCycle(2);//任务周期 1:一次性任务 2:周期性-每天

                        boolean save = save(newRecord);

                        if (save) {
                            log.info("处理每日任务成功: 买入任务 会员id: {}, 任务状态信息: {}", memberInfo.getId(), newRecord);

                            //发送次日凌晨自动领取奖励MQ延时消息
                            sendAutoClaimRewardMessage(newRecord, memberInfo.getId());

                        } else {
                            log.error("处理每日任务失败: 买入任务 会员id: {}, 任务状态信息: {}", memberInfo.getId(), newRecord);
                        }

                        return save;
                    }
                } else {
                    //未完成任务
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("处理会员每日任务失败 买入任务 会员id: {}, taskType: {}, e: ", memberInfo.getId(), taskType, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }


    /**
     * 处理每日卖出任务
     *
     * @param memberInfo
     * @param taskManager
     * @return boolean
     */
    @Override
    public boolean handleDailySellTask(MemberInfo memberInfo, TaskManager taskManager) {

        Integer taskType = Integer.valueOf(RewardTaskTypeEnum.SELL.getCode());

        //分布式锁key ar-wallet-handleDailyTask+会员id
        String key = "ar-wallet-handleDailyTask" + memberInfo.getId();
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                // 查看是否完成了任务

                //任务目标类型 1-次数 2-金额
                int taskTargetNum = "1".equals(taskManager.getTaskTarget())
                        ? memberInfo.getTodaySellSuccessCount()
                        : memberInfo.getTodaySellSuccessAmount().intValue();

                if (taskTargetNum >= taskManager.getTaskTargetNum()) {

                    LocalDate today = LocalDate.now();
                    // 检查是否已存在今日卖出的记录
                    long existingTaskCount = lambdaQuery()
                            .eq(MemberTaskStatus::getMemberId, memberInfo.getId())
                            .eq(MemberTaskStatus::getTaskId, taskManager.getId())
                            .eq(MemberTaskStatus::getCompletionDate, today)
                            .count();

                    if (existingTaskCount > 0) {
                        // 如果已存在记录，则表示今日任务已完成，不做处理
                        return true;
                    } else {
                        // 不存在，表示今日首次卖出，记录任务完成
                        MemberTaskStatus newRecord = new MemberTaskStatus();
                        newRecord.setMemberId(memberInfo.getId());//会员id
                        newRecord.setTaskType(taskType);//任务类型
                        newRecord.setTaskId(taskManager.getId());//任务id
                        newRecord.setCompletionStatus(1); // 任务完成
                        newRecord.setRewardClaimed(0); // 奖励未领取
                        newRecord.setCompletionDate(today);// 任务完成日期 今日
                        newRecord.setOrderNo(orderNumberGenerator.generateOrderNo("RW"));//任务订单号
                        newRecord.setCreateTime(LocalDateTime.now());//任务完成时间
                        newRecord.setTaskCycle(2);//任务周期 1:一次性任务 2:周期性-每天

                        boolean save = save(newRecord);

                        if (save) {
                            log.info("处理每日任务成功: 卖出任务 会员id: {}, 任务状态信息: {}", memberInfo.getId(), newRecord);

                            //发送次日凌晨自动领取奖励MQ延时消息
                            sendAutoClaimRewardMessage(newRecord, memberInfo.getId());

                        } else {
                            log.error("处理每日任务失败: 卖出任务 会员id: {}, 任务状态信息: {}", memberInfo.getId(), newRecord);
                        }

                        return save;
                    }
                } else {
                    //未完成任务
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("处理会员每日任务失败 卖出任务 会员id: {}, taskType: {}, e: ", memberInfo.getId(), taskType, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }

    /**
     * 发送次日凌晨自动领取奖励MQ延时消息
     *
     * @return long
     */
    public void sendAutoClaimRewardMessage(MemberTaskStatus newRecord, Long memberId) {
        //发送次日00:10自动领取奖励的MQ
        Long lastUpdateTimestamp = System.currentTimeMillis();
        TaskInfo taskInfo = new TaskInfo(newRecord.getOrderNo() + "|" + memberId, TaskTypeEnum.MERCHANT_AUTO_CLAIM_REWARD_QUEUE.getCode(), lastUpdateTimestamp);
        //计算当前时间至次日凌晨00:10的毫秒数 (MQ延迟时间)
        long millis = calculateDelayUntilNextDay010();
        rabbitMQService.sendTimeoutTask(taskInfo, millis);
    }


    /**
     * 计算当前时间到次日凌晨00:10的毫秒数
     *
     * @return long
     */
    private long calculateDelayUntilNextDay010() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDay010 = now.plusDays(1).withHour(0).withMinute(10).withSecond(0).withNano(0);
        long delay = ChronoUnit.MILLIS.between(now, nextDay010);
        return delay;
    }


    /**
     * 查询会员当天所有任务状态列表
     *
     * @param memberId
     * @return
     */
    @Override
    public List<MemberTaskStatus> queryMemberTodayTaskStatus(Long memberId) {
        return lambdaQuery()
                .eq(MemberTaskStatus::getMemberId, memberId)
                .eq(MemberTaskStatus::getCompletionDate, LocalDate.now()).list();
    }


    /**
     * 检查是否完成了任务 并且未领取奖励 加上排他行锁
     *
     * @param memberInfo
     * @param taskManager
     * @return {@link MemberTaskStatus}
     */
    @Override
    public MemberTaskStatus checkTaskCompletedAndRewardUnclaimed(MemberInfo memberInfo, TaskManager taskManager) {

        // 创建QueryWrapper
        QueryWrapper<MemberTaskStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(MemberTaskStatus::getMemberId, memberInfo.getId()) // 会员id
                .eq(MemberTaskStatus::getTaskId, taskManager.getId()) // 任务id
                .eq(MemberTaskStatus::getCompletionStatus, 1) // 任务完成状态 1 已完成
                .eq(MemberTaskStatus::getRewardClaimed, 0); // 任务领取状态 0 未领取

        // 判断任务是周期性还是一次性
        if ("2".equals(taskManager.getTaskCycle())) {
            // 周期性任务 需要加上时间是当天
            queryWrapper.lambda().eq(MemberTaskStatus::getCompletionDate, LocalDate.now());
        }

        // 使用selectForUpdate加上排他行锁
        queryWrapper.last("FOR UPDATE");

        // 执行查询
        return baseMapper.selectOne(queryWrapper);

    }

    /**
     * 完成一次性任务
     *
     * @param memberInfo
     */
    @Override
    public Boolean completeOnceTask(MemberInfo memberInfo, TaskManager taskManager) {


        //Integer taskType = Integer.valueOf(RewardTaskTypeEnum.REAL_AUTH.getCode());

        //分布式锁key ar-wallet-handleDailyTask 和将以前实名认证过会员的任务完成的锁一致
        String key = "ar-wallet-handleDailyTask";
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                log.info("完成一次性任务: 会员id: {}, type:{}", memberInfo.getId(), taskManager.getTaskType());

                LocalDate today = LocalDate.now();
                // 检查该会员是否完成过 实名认证任务
                long existingTaskCount = lambdaQuery()
                        .eq(MemberTaskStatus::getMemberId, memberInfo.getId())
                        .eq(MemberTaskStatus::getTaskId, taskManager.getId())
                        .count();

                if (existingTaskCount > 0) {
                    // 如果已存在记录，则表示实名认证任务已完成，不做处理
                    return true;
                } else {
                    // 不存在，实名认证，记录任务完成
                    MemberTaskStatus newRecord = new MemberTaskStatus();
                    newRecord.setMemberId(memberInfo.getId());//会员id
                    newRecord.setTaskType(Integer.valueOf(taskManager.getTaskType()));//任务类型
                    newRecord.setTaskId(taskManager.getId());//任务id
                    newRecord.setCompletionStatus(1); // 任务完成
                    newRecord.setRewardClaimed(0); // 奖励未领取
                    newRecord.setCompletionDate(today);//任务完成日期 今天
                    newRecord.setOrderNo(orderNumberGenerator.generateOrderNo("RW"));//任务订单号
                    newRecord.setCreateTime(LocalDateTime.now());//任务完成时间
                    newRecord.setTaskCycle(1);//任务周期 1:一次性任务 2:周期性-每天

                    log.info("完成一次性任务: 会员id: {}, 任务状态信息: {}", memberInfo.getId(), newRecord);

                    return save(newRecord);
                }
            }
        } catch (Exception e) {
            log.error("完成一次性任务失败 会员id: {}, taskType: {}, e: {}", memberInfo.getId(), taskManager.getTaskType(), e.getMessage());
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;

    }
}
