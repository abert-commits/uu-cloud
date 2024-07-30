package org.uu.manager.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.BiMemberReconciliationDTO;
import org.uu.manager.entity.BiMemberReconciliation;
import org.uu.manager.mapper.BiMemberReconciliationMapper;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.service.IBiMemberReconciliationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 会员对账报表 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-06
 */
@Service
public class BiMemberReconciliationServiceImpl extends ServiceImpl<BiMemberReconciliationMapper, BiMemberReconciliation> implements IBiMemberReconciliationService {

    @Override
    @SneakyThrows
    public PageReturn<BiMemberReconciliationDTO> listPage(MerchantDailyReportReq req) {

        BigDecimal balancePageTotal = new BigDecimal(0);
        BigDecimal payMoneyPageTotal = new BigDecimal(0);
        BigDecimal withdrawMoneyPageTotal = new BigDecimal(0);
        BigDecimal payFeePageTotal = new BigDecimal(0);
        BigDecimal withdrawFeePageTotal = new BigDecimal(0);
        BigDecimal sellRewardPageTotal = new BigDecimal(0);
        BigDecimal buyRewardPageTotal = new BigDecimal(0);
        BigDecimal memberUpPageTotal = new BigDecimal(0);
        BigDecimal memberDownPageTotal = new BigDecimal(0);
        BigDecimal memberDiffPageTotal = new BigDecimal(0);
        BigDecimal usdtBuyPageMoneyTotal = new BigDecimal(0);
        BigDecimal sellTeamRewardTotal = BigDecimal.ZERO;
        BigDecimal buyTeamRewardTotal = BigDecimal.ZERO;
        BigDecimal platformDividensTotal = BigDecimal.ZERO;

        Page<BiMemberReconciliation> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());

        LambdaQueryChainWrapper<BiMemberReconciliation> lambdaQuery = lambdaQuery();
        lambdaQuery.orderByDesc(BiMemberReconciliation::getDateTime);

        LambdaQueryWrapper<BiMemberReconciliation> queryWrapper = new QueryWrapper<BiMemberReconciliation>()
                .select("IFNULL(sum(member_balance), 0) as balanceTotal,\n" +
                        "       IFNULL(sum(pay_money), 0)      as payMoneyTotal,\n" +
                        "       IFNULL(sum(withdraw_money), 0) as withdrawMoneyTotal,\n" +
                        "       IFNULL(sum(usdt_buy_money), 0)   as usdtBuyMoneyTotal,\n" +
                        "       IFNULL(sum(sell_reward), 0)    as sellRewardTotal,\n" +
                        "       IFNULL(sum(buy_reward), 0)     as buyRewardTotal,\n" +
                        "       IFNULL(sum(member_up), 0)      as memberUpTotal,\n" +
                        "       IFNULL(sum(member_down), 0)    as memberDownTotal,\n" +
                        "       IFNULL(sum(member_diff), 0)    as memberDiffTotal,\n" +
                        "       IFNULL(sum(sell_team_reward), 0)    as sellTeamReward,\n" +
                        "       IFNULL(sum(buy_team_reward), 0)    as buyTeamReward,\n" +
                        "       IFNULL(sum(platform_dividens), 0)    as platformDividens"
                )
                .lambda();

        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(BiMemberReconciliation::getDateTime, req.getStartTime());
            queryWrapper.ge(BiMemberReconciliation::getDateTime, req.getStartTime());

        }

        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(BiMemberReconciliation::getDateTime, req.getEndTime());
            queryWrapper.le(BiMemberReconciliation::getDateTime, req.getEndTime());
        }

        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(BiMemberReconciliation::getMerchantCode, req.getMerchantCode());
            queryWrapper.eq(BiMemberReconciliation::getMerchantCode, req.getMerchantCode());
        }

        CompletableFuture<BiMemberReconciliation> totalFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectOne(queryWrapper));

        Page<BiMemberReconciliation> finalPage = page;
        CompletableFuture<Page<BiMemberReconciliation>> resultFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectPage(finalPage, lambdaQuery.getWrapper()));

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(totalFuture, resultFuture);
        allFutures.get();
        page = resultFuture.get();
        BiMemberReconciliation totalData = totalFuture.get();
        JSONObject extend = new JSONObject();

        extend.put("balanceTotal", totalData.getBalanceTotal().toPlainString());
        extend.put("payMoneyTotal", totalData.getPayMoneyTotal().toPlainString());
        extend.put("withdrawMoneyTotal", totalData.getWithdrawMoneyTotal().toPlainString());
        extend.put("usdtBuyMoneyTotal", totalData.getUsdtBuyMoneyTotal().toPlainString());
        extend.put("sellRewardTotal", totalData.getSellRewardTotal().toPlainString());
        extend.put("buyRewardTotal", totalData.getBuyRewardTotal().toPlainString());
        extend.put("memberUpTotal", totalData.getMemberUpTotal().toPlainString());
        extend.put("memberDownTotal", totalData.getMemberDownTotal().toPlainString());
        extend.put("memberDiffTotal", totalData.getMemberDiffTotal().toPlainString());
        extend.put("sellTeamReward", totalData.getSellTeamReward().toPlainString());
        extend.put("buyTeamReward", totalData.getBuyTeamReward().toPlainString());
        extend.put("platformDividens", totalData.getPlatformDividens().toPlainString());

        List<BiMemberReconciliation> records = page.getRecords();
        List<BiMemberReconciliationDTO> list = new ArrayList<BiMemberReconciliationDTO>();

        for (BiMemberReconciliation record : records) {
            balancePageTotal = balancePageTotal.add(new BigDecimal(record.getMemberBalance().toString()));
            payMoneyPageTotal = payMoneyPageTotal.add(new BigDecimal(record.getPayMoney().toString()));
            usdtBuyPageMoneyTotal = usdtBuyPageMoneyTotal.add(new BigDecimal(record.getUsdtBuyMoney().toString()));
            sellRewardPageTotal = sellRewardPageTotal.add(new BigDecimal(record.getSellReward().toString()));
            buyRewardPageTotal = buyRewardPageTotal.add(new BigDecimal(record.getBuyReward().toString()));
            memberUpPageTotal = memberUpPageTotal.add(new BigDecimal(record.getMemberUp().toString()));
            memberDownPageTotal = memberDownPageTotal.add(new BigDecimal(record.getMemberDown().toString()));
            memberDiffPageTotal = memberDiffPageTotal.add(new BigDecimal(record.getMemberDiff().toString()));
            BiMemberReconciliationDTO data = new BiMemberReconciliationDTO();
            sellTeamRewardTotal = sellTeamRewardTotal.add(new BigDecimal(record.getSellTeamReward().toString()));
            buyTeamRewardTotal = buyTeamRewardTotal.add(new BigDecimal(record.getBuyTeamReward().toString()));
            platformDividensTotal = platformDividensTotal.add(new BigDecimal(record.getPlatformDividens().toString()));
            BeanUtils.copyProperties(record, data);
            list.add(data);
        }

        extend.put("balancePageTotal", balancePageTotal.toPlainString());
        extend.put("payMoneyPageTotal", payMoneyPageTotal.toPlainString());
        extend.put("withdrawMoneyPageTotal", withdrawMoneyPageTotal.toPlainString());
        extend.put("usdtBuyPageMoneyTotal", usdtBuyPageMoneyTotal.toPlainString());
        extend.put("payFeePageTotal", payFeePageTotal.toPlainString());
        extend.put("withdrawFeePageTotal", withdrawFeePageTotal.toPlainString());
        extend.put("sellRewardPageTotal", sellRewardPageTotal.toPlainString());
        extend.put("buyRewardPageTotal", buyRewardPageTotal.toPlainString());
        extend.put("memberUpPageTotal", memberUpPageTotal.toPlainString());
        extend.put("memberDownPageTotal", memberDownPageTotal.toPlainString());
        extend.put("memberDiffPageTotal", memberDiffPageTotal.toPlainString());
        extend.put("sellTeamRewardTotal", sellTeamRewardTotal.toPlainString());
        extend.put("buyTeamRewardTotal", buyTeamRewardTotal.toPlainString());
        extend.put("platformDividensTotal", platformDividensTotal.toPlainString());

        return PageUtils.flush(page, list, extend);
    }
}
