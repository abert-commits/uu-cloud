package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.bo.MemberAccountChangeBO;
import org.uu.common.pay.vo.request.MyGroupRequestVO;
import org.uu.common.pay.vo.response.GroupCenterResponseVO;
import org.uu.common.pay.vo.response.GroupDetailResponseVO;
import org.uu.common.pay.vo.response.MyGroupFilterBoxResponseVO;
import org.uu.common.pay.vo.response.MyGroupResponseVO;
import org.uu.common.redis.util.RedisUtils;
import org.uu.common.web.utils.UserContext;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.wallet.entity.*;
import org.uu.wallet.service.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupCenterServiceImpl implements GroupCenterService {
    private final IMemberInfoService memberInfoService;

    private final AntRelationsService antRelationsService;

    private final IMemberAccountChangeService memberAccountChangeService;

    private final InviteLinkService inviteLinkService;

    private final RegisterRecordService registerRecordService;

    private final RedisUtils redisUtils;

    private final DividendConfigService dividendConfigService;

    private final CommissionDividendsService commissionDividendsService;

    private final ITradeConfigService tradeConfigService;

    @Override
    public RestResult<GroupCenterResponseVO> index(Integer days) {
        Long currentUserId = UserContext.getCurrentUserId();
        // 查询个人信息并填充
        MemberInfo currentUser = this.memberInfoService.getById(currentUserId);
        // 获取 自己、下级、下下级
        List<AntRelations> childList = this.antRelationsService.rangeChildByCount(currentUserId, 2, true);

        List<Long> nextOneChildIdList = CollectionUtils.isEmpty(childList) ?
                Collections.emptyList()
                :
                childList.stream()
                        .filter(item -> Objects.nonNull(item) && item.getAntLevel().equals(currentUser.getLevel() + 1))
                        .map(AntRelations::getAntId)
                        .collect(Collectors.toList());

        //  获取当前用户今日的账变记录 包含奖励和返佣和USDT买入
        LocalDateTime localDateTime =  LocalDateTime.now();
        String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest()
                .getHeader(GlobalConstants.HEADER_TIME_ZONE);
        if (com.alibaba.cloud.commons.lang.StringUtils.isNotEmpty(timeZone)) {
            localDateTime = localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of(timeZone)).toLocalDateTime();
        }

        // 从 member_account_change 中查询总买入和总卖出、总返佣和总分红
        List<MemberAccountChangeBO> memberAccountChangeList =
                this.memberAccountChangeService.queryAccountChangeListByIds(
                        Collections.singletonList(currentUserId),
                        Objects.isNull(days) || days <= 0 ? null : localDateTime.plusDays(-days),
                        null,
                        true,
                        true,
                        false
                );

        // 设置分红最低级响应
        DividendConfig dividendConfig = this.dividendConfigService.lambdaQuery()
                .eq(DividendConfig::getDividendsLevel, 1).one();
        if (Objects.isNull(dividendConfig)) {
            RestResult.failed("Dividends config error");
        }

        // 计算当前用户今天的注册人数
        Integer inviteCountOfToday = !CollectionUtils.isEmpty(nextOneChildIdList) ? this.registerRecordService.lambdaQuery()
                .in(RegisterRecord::getAntId, nextOneChildIdList)
                .ge(RegisterRecord::getRegisterTime, LocalDate.now().atStartOfDay())
                .count() : 0;

        // 累计分红奖励
        BigDecimal totalDividendsAmount = totalAmount(memberAccountChangeList, MemberAccountChangeEnum.PLATFORM_DIVIDENDS);
        // 累计买入金额
        BigDecimal totalBuyAmount = totalAmount(memberAccountChangeList, MemberAccountChangeEnum.RECHARGE);
        // 累计卖出金额
        BigDecimal totalSellAmount = totalAmount(memberAccountChangeList, MemberAccountChangeEnum.WITHDRAW);
        // 累计买入奖励
        BigDecimal totalBuyRewardAmount = totalAmount(memberAccountChangeList, MemberAccountChangeEnum.BUY_BONUS);
        // 累计卖出奖励
        BigDecimal totalSellRewardAmount = totalAmount(memberAccountChangeList, MemberAccountChangeEnum.SELL_BONUS);
        // 累计买入返佣金额
        BigDecimal buyCommissionAmount = calcCommissionAmount(memberAccountChangeList, Boolean.TRUE);
        // 累计卖出返佣金额
        BigDecimal sellCommissionAmount = calcCommissionAmount(memberAccountChangeList, Boolean.FALSE);
        // 累计返佣金额
        BigDecimal groupAmount = buyCommissionAmount.add(sellCommissionAmount);
        return RestResult.ok(
                GroupCenterResponseVO.builder()
                        .uid(currentUserId)
                        .buyRewardRatio(currentUser.getBuyBonusProportion())
                        .platformDividends(dividendConfig.getRewardRatio())
                        .defaultInviteCode(this.inviteLinkService.getDefaultInviteCode(currentUserId))
                        .totalDividendsAmount(totalDividendsAmount)
                        .totalBuyAmount(totalBuyAmount)
                        .totalSellAmount(totalSellAmount)
                        .totalBuyRewardAmount(totalBuyRewardAmount)
                        .totalSellRewardAmount(totalSellRewardAmount)
                        .groupAmount(groupAmount)
                        .buyAndSellRewardAmount(totalBuyRewardAmount.add(totalSellRewardAmount))
                        .totalRewardAmount(
                                totalDividendsAmount.add(totalBuyRewardAmount)
                                        .add(totalSellRewardAmount)
                                        .add(groupAmount)
                        )
                        .inviteCountOfToday(inviteCountOfToday)
                        .groupCount(childList.size())
                        .level(currentUser.getAntLevel())
                        .build()
        );
    }

    /**
     * 计算返佣金额
     *
     * @param memberAccountChangeList 账变记录
     * @param isBuyCommission         是否买入返佣 TRUE-买入返佣 FALSE/null-卖出返佣
     */
    private BigDecimal calcCommissionAmount(
            List<MemberAccountChangeBO> memberAccountChangeList,
            Boolean isBuyCommission
    ) {
        // 根据isBuyCommission GET得到MemberAccountChangeEnum
        MemberAccountChangeEnum commissionType =
                Objects.nonNull(isBuyCommission) && isBuyCommission ?
                        MemberAccountChangeEnum.BUY_COMMISSION
                        :
                        MemberAccountChangeEnum.SELL_COMMISSION;
        return CollectionUtils.isEmpty(memberAccountChangeList) ?
                BigDecimal.ZERO
                :
                memberAccountChangeList.stream()
                        .filter(item ->
                                // item非空判断
                                Objects.nonNull(item)
                                        // item的账变类型是否与commissionType匹配
                                        && item.getChangeType().equals(commissionType.getCode())
                        )
                        // 收集账变金额
                        .map(MemberAccountChangeBO::getAmountChange)
                        // 从0开始累加
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 根据账变类型统计账变金额
     *
     * @param memberAccountChangeList 会员账变记录
     * @param memberAccountChangeEnum 会员账变类型
     * @return 累计账变金额
     */
    private BigDecimal totalAmount(
            List<MemberAccountChangeBO> memberAccountChangeList,
            MemberAccountChangeEnum memberAccountChangeEnum
    ) {
        return CollectionUtils.isEmpty(memberAccountChangeList) ?
                BigDecimal.ZERO
                :
                memberAccountChangeList.stream()
                        // 过滤掉null的数据和与当前账变类型不一致的数据
                        .filter(item ->
                                Objects.nonNull(item)
                                        && item.getChangeType().equals(memberAccountChangeEnum.getCode())
                        )
                        // 收集账变金额
                        .map(MemberAccountChangeBO::getAmountChange)
                        // 从0开始累加
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取在线用户MAP
     */
    public Map<Long, Integer> queryOnlineStatusByIds(List<MemberInfo> memberInfos) {
        Map<Long, Integer> resultMap = new HashMap<>();
        Map<Object, Object> onlineUserMap = this.redisUtils.hmget(GlobalConstants.ONLINE_USER_KEY);
        if (!CollectionUtils.isEmpty(memberInfos)) {
            memberInfos.stream()
                    .filter(Objects::nonNull)
                    .forEach(item -> {
                        boolean result = false;
                        if (Objects.nonNull(onlineUserMap.get(item.getMobileNumber()))) {
                            result = ((long) onlineUserMap.get(item.getMobileNumber())) >= System.currentTimeMillis();
                        }
                        resultMap.put(item.getId(), result ? 0 : 1);
                    });
        }
        return resultMap;
    }

    /**
     * 根据Channel清洗用户ID
     *
     * @param registerRecordList 注册记录
     * @param pageType           页面类型
     * @param currentLevel       当前用户等级
     */
    private List<Long> cleanUserIdByChannelId(
            List<InviteLink> inviteCodeList,
            List<RegisterRecord> registerRecordList,
            Integer pageType,
            Integer currentLevel
    ) {
        if (CollectionUtils.isEmpty(registerRecordList)) {
            return Collections.emptyList();
        }

        if (pageType == 2) {
            List<String> nextOneInviteCodeList = inviteCodeList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getLevel().equals(currentLevel + 1))
                    .map(InviteLink::getInviteCode)
                    .collect(Collectors.toList());
            if (! CollectionUtils.isEmpty(nextOneInviteCodeList)) {
                return registerRecordList.stream()
                        .filter(item -> Objects.nonNull(item) && nextOneInviteCodeList.contains(item.getInviteCode()))
                        .map(RegisterRecord::getAntId)
                        .collect(Collectors.toList());
            }
        } else {
            List<String> myInviteCodeList = inviteCodeList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getLevel().equals(currentLevel))
                    .map(InviteLink::getInviteCode)
                    .collect(Collectors.toList());
            if (! CollectionUtils.isEmpty(myInviteCodeList)) {
                return registerRecordList.stream()
                        .filter(item -> Objects.nonNull(item) && myInviteCodeList.contains(item.getInviteCode()))
                        .map(RegisterRecord::getAntId)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public RestResult<MyGroupResponseVO> myGroup(MyGroupRequestVO requestVO) {
        // 查询当前用户信息
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }
        MemberInfo currentUser = this.memberInfoService.getById(currentUserId);
        if (Objects.isNull(currentUser)) {
            return RestResult.failed(ResultCode.USER_NOT_EXIST);
        }

        Page<MemberInfo> pageCollectionOrder = new Page<>();
        pageCollectionOrder.setCurrent(requestVO.getPageNo())
                .setSize(requestVO.getPageSize())
                .setOrders(Collections.singletonList(
                        new OrderItem("create_time", !requestVO.getOrderByRegisterTime().equals(1))
                ));

        // 获取子级用户ID
        List<Long> childUserIdList = requestVO.getPageType() == 1 ?
                this.queryNextOneChildIds(currentUserId, requestVO.getChannelId())
                :
                this.queryNextTwoAndNextTwoChildIds(currentUserId, requestVO.getChannelId());


        // 获取下级和下下级用户邀请码信息
        List<InviteLink> chiledInviteCodeList = this.myAndNextOneInviteCodeList(currentUserId, requestVO.getChannelId());

        // 获取下级或下下级用户给自己的返佣奖励
        List<CommissionDividends> childCommissionList = CollectionUtils.isEmpty(childUserIdList) ? Collections.emptyList() :
                commissionDividendsService.lambdaQuery()
                        .in(CommissionDividends::getFromMember, childUserIdList)
                        .eq(CommissionDividends::getRecordType, 1)
                        .eq(CommissionDividends::getToMember, currentUserId)
                        .list();

        List<String> inviteCodeList = chiledInviteCodeList.stream()
                .filter(Objects::nonNull)
                .map(InviteLink::getInviteCode)
                .collect(Collectors.toList());

        // 获取自己、下级、下下级邀请注册记录
        List<RegisterRecord> allRegisterList = CollectionUtils.isEmpty(inviteCodeList) ? Collections.emptyList()
                : this.registerRecordService.lambdaQuery()
                .in(RegisterRecord::getInviteCode, inviteCodeList)
                .list();

        List<Long> childUIDs = cleanUserIdByChannelId(chiledInviteCodeList, allRegisterList, requestVO.getPageType(), currentUser.getAntLevel());

        if (CollectionUtils.isEmpty(childUIDs)) {
            return RestResult.ok(
                    MyGroupResponseVO.builder()
                            .inviteCode(this.inviteLinkService.getDefaultInviteCode(currentUserId))
                            .totalGroupAmount(BigDecimal.ZERO)
                            .groupList(PageUtils.flush(new Page<>(), Collections.EMPTY_LIST))
                            .build()
            );
        }

        TradeConfig tradeConfig = tradeConfigService.getById(1);
        if (Objects.isNull(tradeConfig)) {
            return RestResult.failed("Trade Config Error");
        }

        Page<MemberInfo> memberInfoPage = this.memberInfoService.lambdaQuery()
                .in(MemberInfo::getId, childUIDs)
                .eq(MemberInfo::getDeleted, "0")
                .page(pageCollectionOrder);
        List<MemberInfo> memberInfoList = memberInfoPage.getRecords();
        Map<Long, Integer> onlineMap = queryOnlineStatusByIds(memberInfoList);
        List<GroupDetailResponseVO> responseVOList = Collections.emptyList();
        if (!CollectionUtils.isEmpty(memberInfoList)) {
            responseVOList = memberInfoList.stream()
                    .filter(Objects::nonNull)
                    .map(item -> {
                        Integer temp = onlineMap.get(item.getId());
                        GroupDetailResponseVO responseVO = GroupDetailResponseVO
                                .builder()
                                .memberId(item.getId())
                                .memberName(item.getMobileNumber())
                                .fromChannel(queryInviteTitle(allRegisterList, chiledInviteCodeList, item.getId()))
                                .contributionAmount(contributionAmount(childCommissionList, item.getId()))
                                .online(Objects.isNull(temp) || temp == 1 ? 1 : 0)
                                .registerTime(item.getCreateTime().toLocalDate())
                                .build();
                        if (requestVO.getPageType() == 1) {
                            responseVO.setCountOfChild(countRegister(allRegisterList, chiledInviteCodeList, item.getId()))
                                    .setCommissionRatio(tradeConfig.getNextOneBuyCommissionRatio());
                        } else {
                            responseVO.setParentUID(getParentId(chiledInviteCodeList, item.getReferrerCode()))
                                    .setCommissionRatio(tradeConfig.getNextTwoBuyCommissionRatio());
                        }
                        return responseVO;
                    })
                    .collect(Collectors.toList());
        }

        return RestResult.ok(
                MyGroupResponseVO.builder()
                        .inviteCode(this.inviteLinkService.getDefaultInviteCode(currentUserId))
                        .totalGroupAmount(buyAndSellAmount(currentUserId))
                        .groupList(PageUtils.flush(pageCollectionOrder, responseVOList))
                        .build()
        );
    }

    /**
     * 获取下级用户ID
     * @param currentUserId 用户ID
     * @param channelId 指定我的邀请码ID
     */
    private List<Long> queryNextOneChildIds(Long currentUserId, Long channelId) {
        List<InviteLink> myInviteLinkList = this.queryMyInviteCodeList(currentUserId, channelId);
        if (! CollectionUtils.isEmpty(myInviteLinkList)) {
            return this.registerRecordService.lambdaQuery()
                    .in(
                            RegisterRecord::getInviteCode,
                            myInviteLinkList.stream()
                                    .filter(Objects::nonNull)
                                    .map(InviteLink::getInviteCode)
                                    .collect(Collectors.toList())
                    )
                    .list()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(RegisterRecord::getAntId)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 获取我的下级和下下级用户ID
     * @param currentUserId 用户ID
     * @param channelId 指定我的邀请码ID
     */
    private List<Long> queryNextTwoAndNextTwoChildIds(Long currentUserId, Long channelId) {
        if (Objects.nonNull(channelId) && channelId > 0) {
            List<Long> nextOneChildIds = queryNextOneChildIds(currentUserId, channelId);
            if (!CollectionUtils.isEmpty(nextOneChildIds)) {
                List<InviteLink> nextOneInviteCodeList = this.inviteLinkService.lambdaQuery()
                        .in(InviteLink::getAntId, nextOneChildIds)
                        .list();
                if (!CollectionUtils.isEmpty(nextOneInviteCodeList)) {
                    List<String> nextOneInviteCodes = nextOneInviteCodeList.stream()
                            .filter(Objects::nonNull)
                            .map(InviteLink::getInviteCode)
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(nextOneInviteCodes)) {
                        List<Long> nextTwoChildUserIds = this.registerRecordService.lambdaQuery()
                                .in(RegisterRecord::getInviteCode, nextOneInviteCodes)
                                .list()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(RegisterRecord::getAntId)
                                .collect(Collectors.toList());
                        return Stream.concat(
                                nextOneChildIds.stream(),
                                nextTwoChildUserIds.stream()
                        ).collect(Collectors.toList());
                    }
                }
                return nextOneChildIds;
            }
        }
        return this.antRelationsService.rangeChildByCount(currentUserId, 2,  false)
                .stream()
                .filter(Objects::nonNull)
                .map(AntRelations::getAntId)
                .collect(Collectors.toList());
    }

    /**
     * 获取我的邀请码
     * @param currentUserId 用户ID
     * @param channelId 邀请码ID
     */
    private List<InviteLink> queryMyInviteCodeList(Long currentUserId, Long channelId) {
        LambdaQueryChainWrapper<InviteLink> lambdaQuery = this.inviteLinkService.lambdaQuery()
                .eq(InviteLink::getAntId, currentUserId);
        if (Objects.nonNull(channelId) && channelId > 0) {
            lambdaQuery.eq(InviteLink::getId, channelId);
        }
        return lambdaQuery.list();
    }

    /**
     * 获取我和下级的邀请码
     * @param currentUserId 当前用户ID
     * @param channelId 指定我的邀请码ID
     */
    private List<InviteLink> myAndNextOneInviteCodeList(Long currentUserId, Long channelId) {
        List<InviteLink> myInviteCodeList = queryMyInviteCodeList(currentUserId, channelId);
        if (!CollectionUtils.isEmpty(myInviteCodeList)) {
            List<Long> nextOneChildUserIdList = this.registerRecordService.lambdaQuery()
                    .in(
                            RegisterRecord::getInviteCode,
                            myInviteCodeList.stream()
                                    .filter(Objects::nonNull)
                                    .map(InviteLink::getInviteCode)
                                    .collect(Collectors.toList())
                    )
                    .list()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(RegisterRecord::getAntId)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(nextOneChildUserIdList)) {
                List<InviteLink> nextOneChildInviteCodeList = this.inviteLinkService.lambdaQuery()
                        .in(InviteLink::getAntId, nextOneChildUserIdList)
                        .list();
                if (!CollectionUtils.isEmpty(nextOneChildInviteCodeList)) {
                    return Stream.concat(
                            myInviteCodeList.stream(),
                            nextOneChildInviteCodeList.stream()
                                    .filter(Objects::nonNull)
                    ).collect(Collectors.toList());
                }
            }
            return myInviteCodeList;
        }
        return Collections.emptyList();
    }

    @Override
    public RestResult<List<MyGroupFilterBoxResponseVO>> filterBox() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }

        List<InviteLink> inviteLinkList = this.inviteLinkService.lambdaQuery()
                .eq(InviteLink::getAntId, currentUserId)
                .eq(InviteLink::getDeleted, 0)
                .list();
        if (CollectionUtils.isEmpty(inviteLinkList)) {
            return RestResult.ok(Collections.emptyList());
        }

        return RestResult.ok(
                inviteLinkList.stream()
                        .filter(Objects::nonNull)
                        .map(item -> MyGroupFilterBoxResponseVO.builder()
                                .id(item.getId())
                                .title(item.getTitle())
                                .inviteCode(item.getInviteCode())
                                .build())
                        .collect(Collectors.toList())
        );
    }

    /**
     * 统计自己、下级、下下级买入 + 卖出金额
     *
     * @param currentUserId 当前用户ID
     */
    private BigDecimal buyAndSellAmount(Long currentUserId) {
        List<AntRelations> antRelations = this.antRelationsService.rangeChildByCount(currentUserId, 2, true);
        if (CollectionUtils.isEmpty(antRelations)) {
            return BigDecimal.ZERO;
        }
        List<MemberAccountChangeBO> memberAccountChangeBOS = this.memberAccountChangeService.queryAccountChangeListByIds(
                antRelations.stream()
                        .filter(Objects::nonNull)
                        .map(AntRelations::getAntId)
                        .collect(Collectors.toList()),
                null,
                null,
                false,
                false,
                false
        );
        if (CollectionUtils.isEmpty(memberAccountChangeBOS)) {
            return BigDecimal.ZERO;
        }
        return memberAccountChangeBOS.stream()
                .filter(Objects::nonNull)
                .map(MemberAccountChangeBO::getAmountChange)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取父级ID
     *
     * @param inviteLinkList    邀请码记录
     * @param currentInviteCode 当前邀请码
     * @return
     */
    private Long getParentId(
            List<InviteLink> inviteLinkList,
            String currentInviteCode
    ) {
        Long result = null;
        if (!CollectionUtils.isEmpty(inviteLinkList)) {
            result = inviteLinkList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getInviteCode().equals(currentInviteCode))
                    .map(InviteLink::getAntId)
                    .findAny()
                    .orElse(null);
        }
        return result;
    }

    /**
     * 计算贡献金额
     *
     * @param childCommissionList 奖励记录
     * @param currentUserId       当前用户ID
     */
    private BigDecimal contributionAmount(
            List<CommissionDividends> childCommissionList,
            Long currentUserId
    ) {
        BigDecimal result = BigDecimal.ZERO;
        if (!CollectionUtils.isEmpty(childCommissionList)) {
            result = childCommissionList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getFromMember().equals(currentUserId))
                    .map(CommissionDividends::getRewardAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return result;
    }

    /**
     * 查询当前用户的邀请码标题
     *
     * @param registerList   注册记录
     * @param inviteLinkList 邀请码记录
     * @param currentUserId  当前用户ID
     */
    private String queryInviteTitle(List<RegisterRecord> registerList,
                                    List<InviteLink> inviteLinkList,
                                    Long currentUserId
    ) {
        String result = StringUtils.EMPTY;
        if (!CollectionUtils.isEmpty(registerList)) {
            String inviteCode = registerList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getAntId().equals(currentUserId))
                    .map(RegisterRecord::getInviteCode)
                    .findAny()
                    .orElse("");
            if (!CollectionUtils.isEmpty(inviteLinkList)) {
                result = inviteLinkList.stream()
                        .filter(item -> Objects.nonNull(item) && item.getInviteCode().equals(inviteCode))
                        .map(InviteLink::getTitle)
                        .findAny()
                        .orElse("");
            }
        }
        return result;
    }

    /**
     * 计算注册人数
     *
     * @param registerList   注册列表
     * @param inviteLinkList 邀请码记录
     * @param currentUserId  当前用户ID
     */
    private Integer countRegister(
            List<RegisterRecord> registerList,
            List<InviteLink> inviteLinkList,
            Long currentUserId
    ) {
        if (CollectionUtils.isEmpty(inviteLinkList)) {
            return 0;
        }
        Set<String> nextOneInviteCodes = inviteLinkList.stream()
                .filter(item -> Objects.nonNull(item) && item.getAntId().equals(currentUserId))
                .map(InviteLink::getInviteCode)
                .collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(nextOneInviteCodes)) {
            return 0;
        }

        if (CollectionUtils.isEmpty(registerList)) {
            return 0;
        }

        return registerList.stream()
                .filter(item -> Objects.nonNull(item) && nextOneInviteCodes.contains(item.getInviteCode()))
                .collect(Collectors.toSet())
                .size();
    }

//    public static void main(String[] args) {
//        ZoneId zoneId = ZoneId.of("UTC+1");
//        LocalDateTime now = LocalDateTime.now(zoneId);
//        System.out.println(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now));
//    }
}
