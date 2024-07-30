package org.uu.wallet.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.message.CommissionAndDividendsMessage;
import org.uu.common.core.message.ExecuteCommissionAndDividendsMessage;
import org.uu.common.pay.bo.MemberAccountChangeBO;
import org.uu.wallet.Enum.CommissionFlagEnum;
import org.uu.wallet.consumer.CalcCommissionAndDividendsConsumer;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalcCommissionAndDividendsServiceImpl implements CalcCommissionAndDividendsService {
    private final AntRelationsService antRelationsService;

    private final MemberInfoMapper memberInfoMapper;

    private final IMemberAccountChangeService memberAccountChangeService;

    private final DividendConfigService dividendConfigService;

    private final ITradeConfigService tradeConfigService;

    private final RabbitMQService rabbitMQService;

    /**
     * 处理返佣   仅针对一级
     *
     * @param commissionMessage 返佣消息
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean calcCommission(CommissionAndDividendsMessage commissionMessage) {
        // 当前订单不存在或已返佣直接返回  幂等消费
        MemberAccountChange accountChange = this.memberAccountChangeService.lambdaQuery()
                .eq(MemberAccountChange::getMid, commissionMessage.getUid())
                .eq(MemberAccountChange::getChangeType, commissionMessage.getChangeType().getCode())
                .eq(MemberAccountChange::getOrderNo, commissionMessage.getOrderNo())
                .one();
        log.info("[Rabbit消费计算分红和返佣队列消息] mid: {}, changeType: {}, orderNo: {}, Result: {}", commissionMessage.getUid(), commissionMessage.getChangeType().getCode(), commissionMessage.getOrderNo(), accountChange);
        if (Objects.isNull(accountChange) || CommissionFlagEnum.COMMISSION_YES.getCommissionFlag().equals(accountChange.getCommissionFlag())) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 返佣失败 当前订单不存在或已返佣");
            return true;
        }

        // 判断是否有上一级
        Long currentUserId = commissionMessage.getUid();
        AntRelations parentOne = this.antRelationsService.selectParentByCount(currentUserId, 1);
        if (Objects.isNull(parentOne)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 返佣失败 当前用户不存在上级");
            return true;
        }

        TradeConfig tradeConfig = tradeConfigService.getById(1);
        if (Objects.isNull(tradeConfig)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 返佣失败 交易配置信息为空");
            return true;
        }

        BigDecimal commissionAmount = commissionMessage.getChangeType().equals(MemberAccountChangeEnum.RECHARGE)
                ?
                commissionMessage.getAmount()
                        .multiply(tradeConfig.getNextOneBuyCommissionRatio())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                        .setScale(2, RoundingMode.DOWN)
                :
                commissionMessage.getAmount()
                        .multiply(tradeConfig.getNextOneSellCommissionRatio())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                        .setScale(2, RoundingMode.DOWN);

        // 如果返佣金额小于等于0则不产生返佣的消息
        if (commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 金额为:{}不进行上级返佣消息生产", commissionAmount);
            return true;
        }

        // 发送上一级返佣信息   类型  fromId、toId、金额、orderNo
        rabbitMQService.sendExecuteCommissionOrDividendsMessage(
                ExecuteCommissionAndDividendsMessage.builder()
                        .type(
                                commissionMessage.getChangeType().equals(MemberAccountChangeEnum.RECHARGE) ? 1 : 2
                        )
                        .fromMemberId(commissionMessage.getUid())
                        .toMemberId(parentOne.getAntId())
                        .amount(commissionAmount)
                        .orderNo(commissionMessage.getOrderNo())
                        .build()
        );

        // 判断是否有上二级
        AntRelations parentTwo = this.antRelationsService.selectParentByCount(currentUserId, 2);
        if (Objects.isNull(parentTwo)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 返佣失败 当前用户不存在上上级");
            return true;
        }

        commissionAmount = commissionMessage.getChangeType().equals(MemberAccountChangeEnum.RECHARGE)
                ?
                commissionMessage.getAmount()
                        .multiply(tradeConfig.getNextTwoBuyCommissionRatio())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                        .setScale(2, RoundingMode.DOWN)
                :
                commissionMessage.getAmount()
                        .multiply(tradeConfig.getNextTwoSellCommissionRatio())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                        .setScale(2, RoundingMode.DOWN);
        // 如果返佣金额小于等于0则不产生返佣的消息
        if (commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 金额为:{}不进行上上级返佣消息生产", commissionAmount);
            return true;
        }

        // 发送上上级返佣信息   类型  fromId、toId、金额、orderNo
        rabbitMQService.sendExecuteCommissionOrDividendsMessage(
                ExecuteCommissionAndDividendsMessage.builder()
                        .type(
                                commissionMessage.getChangeType().equals(MemberAccountChangeEnum.RECHARGE) ? 1 : 2
                        )
                        .fromMemberId(commissionMessage.getUid())
                        .toMemberId(parentTwo.getAntId())
                        .amount(commissionAmount)
                        .orderNo(commissionMessage.getOrderNo())
                        .build()
        );
        return true;
    }

    /**
     * 处理分红 针对顶级用户
     *
     * @param dividendsMessage 分红消息
     * @return 是否处理成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean calcDividends(CommissionAndDividendsMessage dividendsMessage) {
        // 计算个人分红
        this.calcMyDividends(dividendsMessage);
        // 计算上级分红
        if (Objects.isNull(this.antRelationsService.selectParentByCount(dividendsMessage.getUid(), 1))) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 无上级: {}", dividendsMessage.getUid());
            return true;
        }
        this.calcParentOneDividends(dividendsMessage);
        // 计算上上级分红
        if (Objects.isNull(this.antRelationsService.selectParentByCount(dividendsMessage.getUid(), 2))) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 无上上级: {}", dividendsMessage.getUid());
            return true;
        }
        this.calcParentTwoDividends(dividendsMessage);
        return true;
    }

    public Boolean calcMyDividends(CommissionAndDividendsMessage dividendsMessage) {
        // 收集当前用户的下级、下下级的UID
        List<Long> childUidListOfMy = this.antRelationsService
                .rangeChildByCount(dividendsMessage.getUid(), 2, true)
                .stream()
                .filter(Objects::nonNull)
                .map(AntRelations::getAntId)
                .collect(Collectors.toList());
        log.info("[Rabbit消费计算分红和返佣队列消息] - 当前用户的下级和下下级用户:{}", childUidListOfMy);
        if (CollectionUtils.isEmpty(childUidListOfMy)) {
            return true;
        }

        List<MemberAccountChangeBO> memberAccountChangeBOS =
                this.memberAccountChangeService.queryAccountChangeListByIds(
                        childUidListOfMy,
                        null,
                        null,
                        false,
                        false,
                        false
                );
        if (CollectionUtils.isEmpty(memberAccountChangeBOS)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红失败 当前用户下级无账变记录  用户ID: {}, 下级ID(包含自己): {}", dividendsMessage.getUid(), childUidListOfMy);
            return false;
        }

        // 计算卖出和买入总金额
        BigDecimal reduceOfBuyAndSell = memberAccountChangeBOS.stream()
                .filter(item ->
                        Objects.nonNull(item) &&
                                CalcCommissionAndDividendsConsumer.BUY_AND_SELL_LIST.contains(
                                        MemberAccountChangeEnum.buildMemberAccountChangeEnumByCode(item.getChangeType())
                                )
                )
                .map(MemberAccountChangeBO::getAmountChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取上上级用户信息
        MemberInfo parentOneInfo = this.memberInfoMapper.selectById(dividendsMessage.getUid());
        if (Objects.isNull(parentOneInfo)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红 当前用户不存在， id：{}", dividendsMessage.getUid());
            return false;
        }

        // 获取下一层级分红配置
        DividendConfig dividendConfig = this.dividendConfigService.lambdaQuery()
                .eq(DividendConfig::getDividendsLevel, parentOneInfo.getDividendsLevel() + 1)
                .one();

        // 判断是否应该分红并获取分红比例
        if (Objects.isNull(dividendConfig)) {
            log.error("[Rabbit消费计算分红和返佣队列消息] - 处理分红  分红配置异常  请联系管理员  分红配置: {}, level: {}", dividendConfig, parentOneInfo.getDividendsLevel() + 1);
            return false;
        }
        if (reduceOfBuyAndSell.compareTo(new BigDecimal(dividendConfig.getCriticalPoint())) < 0) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红  暂未达到下一层级分红条件  当前用户ID: {}, 订单号: {}, 当前买卖总额: {}, 下一层级分红零界点: {}", dividendsMessage.getUid(), dividendsMessage.getOrderNo(), reduceOfBuyAndSell, dividendConfig.getCriticalPoint());
            return false;
        }

        BigDecimal amount = dividendConfig.getRewardRatio()
                .multiply(new BigDecimal(dividendConfig.getCriticalPoint()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                .setScale(2, RoundingMode.DOWN);

        // 如果返佣金额小于等于0则不产生分红的消息
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 金额为:{}不进行上级分红消息生产", amount);
            return true;
        }

        // 发送上上级返佣信息   类型  fromId、toId、金额、orderNo
        rabbitMQService.sendExecuteCommissionOrDividendsMessage(
                ExecuteCommissionAndDividendsMessage.builder()
                        .type(3)
                        .fromMemberId(dividendsMessage.getUid())
                        .toMemberId(dividendsMessage.getUid())
                        .amount(amount)
                        .orderNo(dividendsMessage.getOrderNo())
                        .build()
        );
        return true;
    }

    public Boolean calcParentOneDividends(CommissionAndDividendsMessage dividendsMessage) {
        // 判断是否有上级
        Long currentUserId = dividendsMessage.getUid();
        AntRelations parentOne = this.antRelationsService.selectParentByCount(currentUserId, 1);
        if (Objects.isNull(parentOne)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 分红失败 当前用户不存在上级");
            return false;
        }

        // 获取上级用户的下级和下下级
        // 收集上上级用户的下级、下下级的UID
        List<Long> childUidListOfParentOne = this.antRelationsService
                .rangeChildByCount(parentOne.getAntId(), 2, true)
                .stream()
                .filter(Objects::nonNull)
                .map(AntRelations::getAntId)
                .collect(Collectors.toList());
        log.info("[Rabbit消费计算分红和返佣队列消息] - 当前用户上级的下级和下下级用户:{}", childUidListOfParentOne);
        if (CollectionUtils.isEmpty(childUidListOfParentOne)) {
            return true;
        }

        List<MemberAccountChangeBO> memberAccountChangeBOS =
                this.memberAccountChangeService.queryAccountChangeListByIds(
                        childUidListOfParentOne,
                        null,
                        null,
                        false,
                        false,
                        false
                );
        if (CollectionUtils.isEmpty(memberAccountChangeBOS)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红失败 当前用户下级无账变记录  用户ID: {}, 下级ID(包含自己): {}", dividendsMessage.getUid(), childUidListOfParentOne);
            return false;
        }

        // 计算卖出和买入总金额
        BigDecimal reduceOfBuyAndSell = memberAccountChangeBOS.stream()
                .filter(item ->
                        Objects.nonNull(item) &&
                                CalcCommissionAndDividendsConsumer.BUY_AND_SELL_LIST.contains(
                                        MemberAccountChangeEnum.buildMemberAccountChangeEnumByCode(item.getChangeType())
                                )
                )
                .map(MemberAccountChangeBO::getAmountChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取上上级用户信息
        MemberInfo parentOneInfo = this.memberInfoMapper.selectById(parentOne.getAntId());
        if (Objects.isNull(parentOneInfo)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红 上级用户不存在， id：{}", parentOne.getAntId());
            return false;
        }

        // 获取下一层级分红配置
        DividendConfig dividendConfig = this.dividendConfigService.lambdaQuery()
                .eq(DividendConfig::getDividendsLevel, parentOneInfo.getDividendsLevel() + 1)
                .one();

        // 判断是否应该分红并获取分红比例
        if (Objects.isNull(dividendConfig)) {
            log.error("[Rabbit消费计算分红和返佣队列消息] - 处理分红  分红配置异常  请联系管理员  分红配置: {}, level: {}", dividendConfig, parentOneInfo.getDividendsLevel() + 1);
            return false;
        }
        if (reduceOfBuyAndSell.compareTo(new BigDecimal(dividendConfig.getCriticalPoint())) < 0) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红  暂未达到下一层级分红条件  当前用户ID: {}, 订单号: {}, 当前买卖总额: {}, 下一层级分红零界点: {}", dividendsMessage.getUid(), dividendsMessage.getOrderNo(), reduceOfBuyAndSell, dividendConfig.getCriticalPoint());
            return false;
        }

        BigDecimal amount = dividendConfig.getRewardRatio()
                .multiply(new BigDecimal(dividendConfig.getCriticalPoint()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                .setScale(2, RoundingMode.DOWN);

        // 如果返佣金额小于等于0则不产生分红的消息
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 金额为:{}不进行上上级分红消息生产", amount);
            return true;
        }

        // 发送上上级返佣信息   类型  fromId、toId、金额、orderNo
        rabbitMQService.sendExecuteCommissionOrDividendsMessage(
                ExecuteCommissionAndDividendsMessage.builder()
                        .type(3)
                        .fromMemberId(dividendsMessage.getUid())
                        .toMemberId(parentOne.getAntId())
                        .amount(amount)
                        .orderNo(dividendsMessage.getOrderNo())
                        .build()
        );
        return true;
    }

    public Boolean calcParentTwoDividends(CommissionAndDividendsMessage dividendsMessage) {
        // 判断是否有上上级
        Long currentUserId = dividendsMessage.getUid();
        AntRelations parentTwo = this.antRelationsService.selectParentByCount(currentUserId, 2);
        if (Objects.isNull(parentTwo)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 分红失败 当前用户不存在上上级");
            return true;
        }

        // 获取上级用户的下级和下下级
        // 收集上上级用户的下级、下下级的UID
        List<Long> childUidListOfParentTwo = this.antRelationsService
                .rangeChildByCount(parentTwo.getAntId(), 2, true)
                .stream()
                .filter(Objects::nonNull)
                .map(AntRelations::getAntId)
                .collect(Collectors.toList());
        log.info("[Rabbit消费计算分红和返佣队列消息] - 当前用户上上级的下级和下下级用户:{}", childUidListOfParentTwo);
        if (CollectionUtils.isEmpty(childUidListOfParentTwo)) {
            return true;
        }

        List<MemberAccountChangeBO> memberAccountChangeBOS =
                this.memberAccountChangeService.queryAccountChangeListByIds(
                        childUidListOfParentTwo,
                        null,
                        null,
                        false,
                        false,
                        false
                );
        if (CollectionUtils.isEmpty(memberAccountChangeBOS)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红失败 当前上级的下级无账变记录  用户ID: {}, 下级ID(包含自己): {}", dividendsMessage.getUid(), childUidListOfParentTwo);
            return true;
        }

        // 计算卖出和买入总金额
        BigDecimal reduceOfBuyAndSell = memberAccountChangeBOS.stream()
                .filter(item ->
                        Objects.nonNull(item) &&
                                CalcCommissionAndDividendsConsumer.BUY_AND_SELL_LIST.contains(
                                        MemberAccountChangeEnum.buildMemberAccountChangeEnumByCode(item.getChangeType())
                                )
                )
                .map(MemberAccountChangeBO::getAmountChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取上上级用户信息
        MemberInfo parentOneInfo = this.memberInfoMapper.selectById(parentTwo.getAntId());
        if (Objects.isNull(parentOneInfo)) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红 上上级用户不存在， id：{}", parentTwo.getAntId());
            return true;
        }

        // 获取下一层级分红配置
        DividendConfig dividendConfig = this.dividendConfigService.lambdaQuery()
                .eq(DividendConfig::getDividendsLevel, parentOneInfo.getDividendsLevel() + 1)
                .one();

        // 判断是否应该分红并获取分红比例
        if (Objects.isNull(dividendConfig)) {
            log.error("[Rabbit消费计算分红和返佣队列消息] - 处理分红  分红配置异常  请联系管理员  分红配置: {}, level: {}", dividendConfig, parentOneInfo.getDividendsLevel() + 1);
            return false;
        }
        if (reduceOfBuyAndSell.compareTo(new BigDecimal(dividendConfig.getCriticalPoint())) < 0) {
            log.info("[Rabbit消费计算分红和返佣队列消息] - 处理分红  暂未达到下一层级分红条件  当前用户ID: {}, 订单号: {}, 当前买卖总额: {}, 下一层级分红零界点: {}", dividendsMessage.getUid(), dividendsMessage.getOrderNo(), reduceOfBuyAndSell, dividendConfig.getCriticalPoint());
            return true;
        }

        BigDecimal amount = dividendConfig.getRewardRatio()
                .multiply(new BigDecimal(dividendConfig.getCriticalPoint()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN)
                .setScale(2, RoundingMode.DOWN);

        // 发送上上级返佣信息   类型  fromId、toId、金额、orderNo
        rabbitMQService.sendExecuteCommissionOrDividendsMessage(
                ExecuteCommissionAndDividendsMessage.builder()
                        .type(3)
                        .fromMemberId(dividendsMessage.getUid())
                        .toMemberId(parentTwo.getAntId())
                        .amount(amount)
                        .orderNo(dividendsMessage.getOrderNo())
                        .build()
        );
        return true;
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        CommissionAndDividendsMessage message = CommissionAndDividendsMessage.builder()
                .uid(1L)
                .amount(new BigDecimal("1000"))
                .orderNo("MC1122212312312")
                .changeType(MemberAccountChangeEnum.RECHARGE)
                .build();
        System.out.println(JSON.toJSONString(message));
    }
}
