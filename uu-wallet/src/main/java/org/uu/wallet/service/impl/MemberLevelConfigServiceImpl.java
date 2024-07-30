package org.uu.wallet.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.RedisConstants;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.utils.StringUtils;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MemberLevelConfigDTO;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.UserLevelEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MemberLevelConfig;
import org.uu.wallet.entity.TradeConfig;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.MemberLevelConfigMapper;
import org.uu.wallet.service.IMemberLevelConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.uu.wallet.service.ITradeConfigService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 会员等级配置 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberLevelConfigServiceImpl extends ServiceImpl<MemberLevelConfigMapper, MemberLevelConfig> implements IMemberLevelConfigService {

    private final WalletMapStruct walletMapStruct;
    private final RedisUtils redisUtils;
    private final MemberInfoMapper memberInfoMapper;
    @Autowired
    private ITradeConfigService tradeConfigService;


    /**
     * 批次大小
     */
    private static final int BATCH_SIZE = 500;


    @Override
    public PageReturn<MemberLevelConfigDTO> listPage(MemberManualLogsReq req) {
        TradeConfig tradeConfig = tradeConfigService.getById(1);
        JSONObject extend = new JSONObject();
        extend.put("tradeCreditScoreLimit", tradeConfig.getTradeCreditScoreLimit());
        Page<MemberLevelConfig> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MemberLevelConfig> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByAsc(MemberLevelConfig::getLevel);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MemberLevelConfig> records = page.getRecords();
        List<MemberLevelConfigDTO> list = walletMapStruct.memberLevelConfigToDto(records);
        return PageUtils.flush(page, list, extend);
    }
    @Override
    public RestResult updateInfo(MemberLevelConfigDTO req) {

        MemberLevelConfig memberLevelConfig = BeanUtil.toBean(req, MemberLevelConfig.class);
        memberLevelConfig.setUpdateTime(LocalDateTime.now());
        memberLevelConfig.setUpdateBy(UserContext.getCurrentUserName());
        baseMapper.updateById(memberLevelConfig);
        List<MemberLevelConfig> list = this.baseMapper.selectList(null);
        redisUtils.set(RedisConstants.MEMBER_LEVEL_CONFIG, JSON.toJSONString(list));

        CompletableFuture<Void> future = CompletableFuture.runAsync(()->{
            handleMemberInfo("");
        });
        return RestResult.ok();
    }

    /**
     * 加载等级redis
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        List<MemberLevelConfig> list = this.baseMapper.selectList(null);
        redisUtils.set(RedisConstants.MEMBER_LEVEL_CONFIG, JSON.toJSONString(list));
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
