package org.uu.job.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.redis.util.RedisUtils;
import org.uu.wallet.Enum.UserLevelEnum;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberLevelConfig;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


/**
 * 计算会员等级
 *
 * @author Admin
 */
@Component("handleMemberFieldProcessor")
@Slf4j
@RequiredArgsConstructor
public class HandleMemberFieldProcessor implements BasicProcessor {

    private final MemberInfoMapper memberInfoMapper;

    private final RedisUtils redisUtils;

    /**
     * 批次大小
     */
    private static final int BATCH_SIZE = 500;




    /**
     * dateType：today，interval
     * 1.当天执行重跑: {"userId":"12470,12547"}
     * 2.跨天数据处理 ：凌晨半个小时处理前天数据
     * 3.重跑一段时间数据：连续天数
     * 4.Job开关,停服开关控制,服务重启以免数据丢失
     * 5.自动跑今天数据
     *
     * @return
     */
    @Override
    public ProcessResult process(TaskContext context) {

        try {
            // 在线日志功能，可以直接在控制台查看任务日志，非常便捷
            OmsLogger omsLogger = context.getOmsLogger();
            omsLogger.info("计算会员等级数据参数->{}", context.getJobParams());
            log.info("计算会员等级参数->{}", context.getJobParams());
            String jobSwitch = (String) redisUtils.get(RedisConstants.JOB_SWITCH);
            if (!StringUtils.isEmpty(jobSwitch) && GlobalConstants.STATUS_OFF.equals(Integer.parseInt(jobSwitch))) {
                log.info("计算会员等级Job未执行,Job开关已关闭.");
                return new ProcessResult(true, "Job switch turned off");
            }
            String params = !StringUtils.isEmpty(context.getJobParams()) ? context.getJobParams() : context.getInstanceParams();
            JSONObject jsonObject = StringUtils.isEmpty(params) ? new JSONObject() : (JSONObject) JSONObject.parse(params);
            String userId = jsonObject.getString("userId");
            handleMemberInfo(userId);

        } catch (Exception e) {
            log.error("HandleMemberFieldProcessor.process" + e.getMessage());
        }

        return new ProcessResult(true, "return success");
    }

    private void handleMemberInfo(String userId) {
        int pageNo = 1;
        long totalSize = 0;
        Page<MemberInfo> page = new Page<>();
        page.setCurrent(pageNo);
        page.setSize(BATCH_SIZE);
        // startTime <= time < endTime
        page = updateBiPaymentOrder(page, userId, 0L);
        List<MemberInfo> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            log.info("查询记录数为空,直接返回");
            return;
        }
        if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE > 0) {
            totalSize = (page.getTotal() / BATCH_SIZE) + 1;
            log.info("总记录数大于批次,且余数大于0,totalSize->{}", totalSize);
        } else if (page.getTotal() > BATCH_SIZE && page.getTotal() % BATCH_SIZE <= 0) {
            totalSize = (page.getTotal() / BATCH_SIZE);
            log.info("总记录数大于批次,且余数等于0,totalSize->{}", totalSize);
        }
        for (int i = 0; i < totalSize; i++) {
            pageNo++;
            page.setCurrent(pageNo);
            page.setSize(BATCH_SIZE);
            updateBiPaymentOrder(page, userId, page.getTotal());
        }
    }

    @SneakyThrows
    @NotNull
    private Page<MemberInfo> updateBiPaymentOrder(Page<MemberInfo> pageInfo, String userId, Long totalNum) {
        List<String> userIdList = new ArrayList<>();
        if(!StringUtils.isEmpty(userId)){
            userIdList =  Arrays.asList(userId.split(","));
        }

        LambdaQueryChainWrapper<MemberInfo> lambdaQuery = new LambdaQueryChainWrapper<>(memberInfoMapper);
        if (!userIdList.isEmpty()){
            lambdaQuery.in(MemberInfo::getId, userIdList);
        }
        long page = (pageInfo.getCurrent()-1)*pageInfo.getSize();
        long size = pageInfo.getSize();
        if(totalNum <= 0){
            totalNum = memberInfoMapper.count(userIdList);
        }
        List<MemberInfo> records = memberInfoMapper.selectMyPage(page, size, userIdList);
        pageInfo.setRecords(records);
        pageInfo.setTotal(totalNum);
        String key = (String) redisUtils.get(RedisConstants.MEMBER_LEVEL_CONFIG);
        List<MemberLevelConfig> listRe = JSON.parseObject(key, new TypeReference<List<MemberLevelConfig>>(){});
        log.info("计算会员等级数量->{}", records.size());
        List<MemberInfo> allList = new ArrayList<>();
        for (MemberInfo item : records){
            Integer level = item.getLevel();
            BigDecimal buySuccessRate = BigDecimal.ZERO;
            if(item.getTotalBuySuccessCount() > 0 && item.getTotalBuyCount()>0){
                buySuccessRate = new BigDecimal(item.getTotalBuySuccessCount()).divide(new BigDecimal(item.getTotalBuyCount()), 2, RoundingMode.DOWN);
            }
            item.setBeforeLevel(item.getLevel());
            for (MemberLevelConfig config : listRe){
                if(config.getLevel() > UserLevelEnum.NORMAL.getCode()){
                    if(item.getCreditScore().longValue() >= config.getCreditScore().longValue() &&
                            item.getTotalSellSuccessCount() >= config.getSellNum() &&
                            item.getTotalBuySuccessCount() >= config.getBuyNum() && buySuccessRate.doubleValue() >= config.getBuySuccessRate().doubleValue()){
                        level = config.getLevel();
                    }
                }else {
                    if(item.getCreditScore().longValue() < config.getCreditScore().longValue()){
                        level = UserLevelEnum.NORMAL.getCode();
                        break;
                    }
                }
            }

            if (!level.equals(item.getBeforeLevel())){
                item.setLevel(level);
                allList.add(item);
            }

        }
        if(!allList.isEmpty()){
            memberInfoMapper.updateRechargeInfo(allList);
        }

        return pageInfo;
    }
}
