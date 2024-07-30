package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.TaskCollectionRecordDTO;
import org.uu.common.pay.req.TaskCollectionRecordReq;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.common.web.utils.UserContext;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.wallet.Enum.MemberTypeEnum;
import org.uu.wallet.Enum.SwitchIdEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberTaskStatus;
import org.uu.wallet.entity.TaskCollectionRecord;
import org.uu.wallet.entity.TaskManager;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.TaskCollectionRecordMapper;
import org.uu.wallet.req.ClaimTaskRewardReq;
import org.uu.wallet.req.TaskCollectionRecordPageReq;
import org.uu.wallet.service.*;
import org.uu.wallet.util.IpUtil;
import org.uu.wallet.vo.PrizeWinnersVo;
import org.uu.wallet.vo.TaskCollectionRecordListVo;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 会员领取任务记录 服务实现类
 * </p>
 *
 * @author
 * @since 2024-03-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCollectionRecordServiceImpl extends ServiceImpl<TaskCollectionRecordMapper, TaskCollectionRecord> implements ITaskCollectionRecordService {
    private final WalletMapStruct walletMapStruct;
    private final IMemberInfoService memberInfoService;
    private final TaskCollectionRecordMapper taskCollectionRecordMapper;

    @Autowired
    private ITaskManagerService taskManagerService;


    @Override
    public PageReturn<TaskCollectionRecordDTO> listPage(TaskCollectionRecordReq req) {
        Page<TaskCollectionRecord> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<TaskCollectionRecord> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(req.getUserId())) {
            lambdaQuery.eq(TaskCollectionRecord::getMemberId, req.getUserId());
        }
        if (StringUtils.isNotBlank(req.getMerchantName())) {
            lambdaQuery.eq(TaskCollectionRecord::getMerchantName, req.getMerchantName());
        }
        if (StringUtils.isNotBlank(req.getTaskName())) {
            lambdaQuery.eq(TaskCollectionRecord::getTaskName, req.getTaskName());
        }
        if (StringUtils.isNotBlank(req.getTaskType())) {
            lambdaQuery.eq(TaskCollectionRecord::getTaskType, req.getTaskType());
        }
        if (StringUtils.isNotBlank(req.getFrequency())) {
            lambdaQuery.eq(TaskCollectionRecord::getTaskCycle, req.getFrequency());
        }
        //--动态查询 提现结束
        if (StringUtils.isNotBlank(req.getReceiveStartTime())) {
            lambdaQuery.ge(TaskCollectionRecord::getCreateTime, req.getReceiveStartTime());
        }
        //--动态查询 提现结束
        if (StringUtils.isNotBlank(req.getReceiveEndTime())) {
            lambdaQuery.le(TaskCollectionRecord::getCreateTime, req.getReceiveEndTime());
        }

        //--动态查询 提现结束
        if (StringUtils.isNotBlank(req.getCompleteStartTime())) {
            lambdaQuery.ge(TaskCollectionRecord::getCompletionTime, req.getCompleteStartTime());
        }
        //--动态查询 提现结束
        if (StringUtils.isNotBlank(req.getCompleteEndTime())) {
            lambdaQuery.le(TaskCollectionRecord::getCompletionTime, req.getCompleteEndTime());
        }
        lambdaQuery.orderByDesc(TaskCollectionRecord::getCompletionTime);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<TaskCollectionRecord> records = page.getRecords();
        List<TaskCollectionRecordDTO> list = walletMapStruct.taskCollectionRecordTransform(records);
        return PageUtils.flush(page, list);
    }


    @Override
    public RestResult<PageReturn<TaskCollectionRecordListVo>> getPageList(TaskCollectionRecordPageReq req) {
        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();
        if (memberInfo == null) {
            log.error("查询奖励明细失败: 未获取到会员认证信息");
            return RestResult.failure(ResultCode.RELOGIN);
        }
        if (req == null) {
            req = new TaskCollectionRecordPageReq();
            req.setStartDate(DateUtil.format(LocalDateTime.now(), GlobalConstants.DATE_FORMAT_DAY));
        }

        LambdaQueryChainWrapper<TaskCollectionRecord> lambdaQuery = lambdaQuery();
        //查询当前会员的奖励明细
        lambdaQuery.eq(TaskCollectionRecord::getMemberId, memberInfo.getId());
        //--动态查询 任务类型
        if (StringUtils.isNotEmpty(req.getTaskType())) {
            lambdaQuery.eq(TaskCollectionRecord::getTaskType, Integer.valueOf(req.getTaskType()));
        }
        //--动态查询 时间范围
        if (StringUtils.isNotEmpty(req.getStartDate())) {
            LocalDate localDate = LocalDate.parse(req.getStartDate());
            LocalDateTime startOfDay = localDate.atStartOfDay();
            lambdaQuery.ge(TaskCollectionRecord::getCreateTime, startOfDay);
        }
        if (StringUtils.isNotEmpty(req.getEndDate())) {
            LocalDate localDate = LocalDate.parse(req.getEndDate());
            LocalDateTime endOfDay = LocalDateTime.of(localDate, LocalTime.MAX);
            lambdaQuery.le(TaskCollectionRecord::getCreateTime, endOfDay);
        }
        // 倒序排序
        lambdaQuery.orderByDesc(TaskCollectionRecord::getCreateTime);

        Page<TaskCollectionRecord> pageCollectionOrder = new Page<>();
        pageCollectionOrder.setCurrent(req.getPageNo());
        pageCollectionOrder.setSize(req.getPageSize());
        baseMapper.selectPage(pageCollectionOrder, lambdaQuery.getWrapper());

        List<TaskCollectionRecord> records = pageCollectionOrder.getRecords();

        PageReturn<TaskCollectionRecord> flush = PageUtils.flush(pageCollectionOrder, records);

        //IPage＜实体＞转 IPage＜Vo＞
        ArrayList<TaskCollectionRecordListVo> resultList = new ArrayList<>();
        for (TaskCollectionRecord record : flush.getList()) {
            TaskCollectionRecordListVo listVo = new TaskCollectionRecordListVo();
            BeanUtil.copyProperties(record, listVo);
            resultList.add(listVo);
        }

        PageReturn<TaskCollectionRecordListVo> resultPage = new PageReturn<>();
        resultPage.setPageNo(flush.getPageNo());
        resultPage.setPageSize(flush.getPageSize());
        resultPage.setTotal(flush.getTotal());
        resultPage.setList(resultList);

        return RestResult.ok(resultPage);
    }


    /**
     * 获取领奖会员列表
     *
     * @return {@link List}<{@link PrizeWinnersVo}>
     */
    @Override
    public List<PrizeWinnersVo> getPrizeWinnersList() {

        List<TaskCollectionRecord> records = lambdaQuery()
                .select(TaskCollectionRecord::getMemberAccount, TaskCollectionRecord::getTaskType, TaskCollectionRecord::getRewardAmount)
                .orderByDesc(TaskCollectionRecord::getId)// 根据create_time字段降序排序
                .last("LIMIT 15")// 限制结果为最新的15条记录
                .list();

        List<PrizeWinnersVo> results = records.stream().map(record -> {
            PrizeWinnersVo prizeWinnersVo = new PrizeWinnersVo();
            prizeWinnersVo.setMemberAccount(maskAccount(record.getMemberAccount()));
            prizeWinnersVo.setTaskType(String.valueOf(record.getTaskType()));
            prizeWinnersVo.setRewardAmount(record.getRewardAmount());
            return prizeWinnersVo;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(results) || results.size() < 15) {
            int rowCount = CollectionUtils.isEmpty(results) ? 0 : results.size();
            int supplyCount = 15 - rowCount;
            List<TaskManager> activeTaskTypes = taskManagerService.getActiveTaskTypes();
            if (CollectionUtils.isEmpty(activeTaskTypes)) {
                return results;
            }
            for (int i = 0; i < supplyCount; i++) {
                PrizeWinnersVo prizeWinnersVo = new PrizeWinnersVo();
                // 随机任务类型
                TaskManager taskType = activeTaskTypes.get(new Random().nextInt(activeTaskTypes.size()));
                // 生成开头7-9且为10位的数字账户
                String randomAccount = (new Random().nextInt(3) + 7) + RandomStringUtils.randomNumeric(9);
                prizeWinnersVo.setMemberAccount(maskAccount(randomAccount));
                prizeWinnersVo.setTaskType(taskType.getTaskType());
                prizeWinnersVo.setRewardAmount(taskType.getTaskReward());
                results.add(prizeWinnersVo);
            }
        }

        return results;
    }

    @SneakyThrows
    @Override
    public TaskCollectionRecordDTO getStatisticsData() {
        TaskCollectionRecordDTO recordDTO = new TaskCollectionRecordDTO();
        // 完成人数
        CompletableFuture<Long> finishNumFuture = CompletableFuture.supplyAsync(() -> {
            return taskCollectionRecordMapper.getFinishNum();
        });

        // 领取人数
        CompletableFuture<Long> receiveNumFuture = CompletableFuture.supplyAsync(() -> {
            return taskCollectionRecordMapper.getReceiveNum();
        });

        // 奖励金额
        CompletableFuture<BigDecimal> rewardAmountFuture = CompletableFuture.supplyAsync(() -> {
            return taskCollectionRecordMapper.getRewardAmount();
        });

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(finishNumFuture, receiveNumFuture, rewardAmountFuture);
        allFutures.get();
        recordDTO.setRewardAmount(rewardAmountFuture.get());
        recordDTO.setRecipientsNum(receiveNumFuture.get());
        recordDTO.setCompletionNum(finishNumFuture.get());
        return recordDTO;
    }

    /**
     * 查看会员是否领取过任务奖励
     *
     * @param memberId 会员id
     * @param taskId   任务id
     * @return boolean
     */
    @Override
    public boolean checkTaskCompletedByMember(Long memberId, Long taskId) {

        Integer count = lambdaQuery()
                .eq(TaskCollectionRecord::getMemberId, memberId)
                .eq(TaskCollectionRecord::getTaskId, taskId)
                .count();

        return count > 0;
    }


    /**
     * 会员账号去敏
     *
     * @param account
     * @return {@link String}
     */
    private String maskAccount(String account) {
        if (StringUtils.isBlank(account)) {
            return account;
        }
        //正常账号 屏蔽****
        if (account.length() > 7) {
            int start = (account.length() - 4) / 2;
            return account.substring(0, start) + "****" + account.substring(start + 4);
        } else if (account.length() > 5) {
            //小于7 大于5的账号 屏蔽**
            int start = (account.length() - 2) / 2;
            return account.substring(0, start) + "**" + account.substring(start + 2);
        }
        return account; // 如果长度不超过5位，直接返回原字符串
    }

}
