package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.InviteCodeInfoDTO;
import org.uu.common.pay.req.InviteCodeInfoReq;
import org.uu.common.pay.vo.request.InviteLinkSaveRequestVO;
import org.uu.common.pay.vo.response.InviteInfoDetailResponseVO;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.InviteLinkMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.service.*;
import org.uu.wallet.util.UniqueCodeGeneratorUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 邀请链接表 服务实现类
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteLinkServiceImpl extends ServiceImpl<InviteLinkMapper, InviteLink> implements InviteLinkService {

    private final RegisterRecordService registerRecordService;

    private final MemberInfoMapper memberInfoMapper;

    private final AntRelationsService antRelationsService;

    private final CommissionDividendsService commissionDividendsService;

    @Override
    public RestResult<PageReturn<InviteInfoDetailResponseVO>> inviteLinkList(PageRequestHome pageRequest) {
        // 获取当前用户ID
        Long currentUserId = UserContext.getCurrentUserId();
        MemberInfo memberInfo = this.memberInfoMapper.selectById(currentUserId);
        if (Objects.isNull(memberInfo)) {
            return RestResult.failed("User does not exist");
        }

        // 获取当前用户的邀请链接
        Page<InviteLink> pageCollectionOrder = new Page<>();
        pageCollectionOrder.setCurrent(pageRequest.getPageNo());
        pageCollectionOrder.setSize(pageRequest.getPageSize());

        Page<InviteLink> inviteLinkPage = this.lambdaQuery()
                .eq(InviteLink::getAntId, currentUserId)
                .eq(InviteLink::getDeleted, 0)
                .orderByAsc(InviteLink::getCreateTime)
                .page(pageCollectionOrder);
        List<InviteLink> inviteLinkList = inviteLinkPage.getRecords();

        // 获取下级和下下级用户信息
        List<AntRelations> childAntRelationList = this.antRelationsService.rangeChildByCount(currentUserId, 2, false);

        // 获取下级和下下级用户邀请码信息
        List<InviteLink> chiledInviteCodeList = CollectionUtils.isEmpty(childAntRelationList) ? Collections.emptyList() :
                this.lambdaQuery()
                        .in(
                                InviteLink::getAntId,
                                childAntRelationList.stream()
                                        .filter(Objects::nonNull)
                                        .map(AntRelations::getAntId)
                                        .collect(Collectors.toList())
                        )
                        .list();

        // 获取下级和下下级用户给自己的返佣奖励
        List<CommissionDividends> childCommissionList = CollectionUtils.isEmpty(childAntRelationList) ? Collections.emptyList() :
                commissionDividendsService.lambdaQuery()
                        .in(CommissionDividends::getFromMember,
                                childAntRelationList.stream().filter(Objects::nonNull).map(AntRelations::getAntId)
                                        .collect(Collectors.toSet())
                        )
                        .eq(CommissionDividends::getRecordType, 1)
                        .eq(CommissionDividends::getToMember, memberInfo.getId())
                        .list();

        // 组装自己、下级、下下级的邀请注册信息
        Set<InviteLink> allInviteCodeSets = Stream.concat(
                inviteLinkList.stream(),
                chiledInviteCodeList.stream()
        ).collect(Collectors.toSet());

        Set<String> allInviteCodeSet = allInviteCodeSets.stream()
                .filter(Objects::nonNull)
                .map(InviteLink::getInviteCode)
                .collect(Collectors.toSet());

        // 获取自己、下级、下下级邀请注册记录
        List<RegisterRecord> allRegisterList = CollectionUtils.isEmpty(allInviteCodeSet) ? Collections.emptyList()
                : this.registerRecordService.lambdaQuery()
                .in(RegisterRecord::getInviteCode, allInviteCodeSet)
                .list();

        List<InviteInfoDetailResponseVO> responseVOList = CollectionUtils.isEmpty(inviteLinkList)
                ?
                Collections.emptyList()
                :
                inviteLinkList.stream()
                        .filter(Objects::nonNull)
                        .map(item -> InviteInfoDetailResponseVO.builder()
                                .id(item.getId())
                                .title(item.getTitle())
                                .antId(item.getAntId())
                                .inviteCode(item.getInviteCode())
                                .defaultLink(item.getDefaultLink())
                                .createTime(item.getCreateTime())
                                .nextOneRegisterCount(
                                        countRegister(
                                                allRegisterList,
                                                allInviteCodeSets,
                                                item.getInviteCode(),
                                                true
                                        )
                                )
                                .nextTwoRegisterCount(
                                        countRegister(
                                                allRegisterList,
                                                allInviteCodeSets,
                                                item.getInviteCode(),
                                                false
                                        )
                                )
                                .commissionAmount(
                                        totalAmount(
                                                childCommissionList,
                                                allRegisterList,
                                                allInviteCodeSets,
                                                item.getInviteCode()
                                        )
                                )
                                .build())
                        .collect(Collectors.toList());

        return RestResult.ok(PageUtils.flush(pageCollectionOrder, responseVOList));
    }

    /**
     * 计算邀请码对应的奖励金额
     * @param childCommissionList 返佣分红记录
     * @param allRegisterList 邀请注册记录
     * @param allInviteCodeSets 邀请码列表
     * @param concurrentInviteCode 当前邀请码
     */
    private BigDecimal totalAmount(
            List<CommissionDividends> childCommissionList,
            List<RegisterRecord> allRegisterList,
            Set<InviteLink> allInviteCodeSets,
            String concurrentInviteCode
    ) {
        if (CollectionUtils.isEmpty(childCommissionList)) {
            return BigDecimal.ZERO;
        }
        Set<Long> childUserIds = new HashSet<>(Collections.emptySet());
        if (CollectionUtils.isNotEmpty(allRegisterList)) {
            // 根据邀请码获取下级和下下级用户ID
            Set<Long> nextOneUserIds = allRegisterList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getInviteCode().equals(concurrentInviteCode))
                    .map(RegisterRecord::getAntId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(nextOneUserIds) && CollectionUtils.isNotEmpty(allInviteCodeSets)) {
                // 根据下级用户ID下级用户邀请码
                Set<String> nextOneInviteCodes = allInviteCodeSets.stream()
                        .filter(item -> Objects.nonNull(item) && nextOneUserIds.contains(item.getAntId()))
                        .map(InviteLink::getInviteCode)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(nextOneInviteCodes)) {
                    Set<Long> nextTwoUserIds = allRegisterList.stream()
                            .filter(item -> Objects.nonNull(item) && nextOneInviteCodes.contains(item.getInviteCode()))
                            .map(RegisterRecord::getAntId)
                            .collect(Collectors.toSet());
                    childUserIds.addAll(nextTwoUserIds);
                    childUserIds.addAll(nextOneUserIds);
                } else {
                    childUserIds.addAll(nextOneUserIds);
                }
            }
        }

        return childCommissionList.stream()
                .filter(item -> Objects.nonNull(item) && childUserIds.contains(item.getFromMember()))
                .map(CommissionDividends::getRewardAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 计算注册数量
     *
     * @param allRegisterList      注册记录
     * @param allInviteCodeSets    邀请码列表
     * @param concurrentInviteCode 当前邀请码
     * @param isNextOne            TRUE-下级  FALSE/null - 下下级
     * @return
     */
    private Integer countRegister(
            List<RegisterRecord> allRegisterList,
            Set<InviteLink> allInviteCodeSets,
            String concurrentInviteCode,
            Boolean isNextOne
    ) {
        if (CollectionUtils.isEmpty(allRegisterList)) {
            return 0;
        }
        //  根据当前邀请码获取下N级用户ID
        Set<Long> userIds = Collections.emptySet();
        if (isNextOne) {
            userIds = allRegisterList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getInviteCode().equals(concurrentInviteCode))
                    .map(RegisterRecord::getAntId)
                    .collect(Collectors.toSet());
        } else {
            // 获取当前邀请码的下级用户ID
            Set<Long> nextOneUserIds = allRegisterList.stream()
                    .filter(item -> Objects.nonNull(item) && item.getInviteCode().equals(concurrentInviteCode))
                    .map(RegisterRecord::getAntId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(nextOneUserIds)) {
                Set<String> nextOneInviteCodes = allInviteCodeSets.stream()
                        .filter(item -> Objects.nonNull(item) && nextOneUserIds.contains(item.getAntId()))
                        .map(InviteLink::getInviteCode)
                        .collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(nextOneInviteCodes)) {
                    userIds = allRegisterList.stream()
                            .filter(item -> Objects.nonNull(item) && nextOneInviteCodes.contains(item.getInviteCode()))
                            .map(RegisterRecord::getAntId)
                            .collect(Collectors.toSet());
                }
            }
        }
        return userIds.size();
    }

    @Override
    public RestResult<Void> removeInviteLink(Long id) {
        InviteLink inviteLink = this.getById(id);
        if (Objects.isNull(inviteLink)) {
            return RestResult.failed("This inviteLink does not exist");
        }

        boolean remove = this.removeById(id);
        if (remove) {
            return RestResult.ok();
        }
        return RestResult.failed();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResult<Void> setDefaultInviteLink(Long id) {
        InviteLink inviteLink = this.getById(id);
        if (Objects.isNull(inviteLink)) {
            return RestResult.failed("This inviteLink does not exist");
        }
        if (inviteLink.getDefaultLink().equals(0)) {
            return RestResult.failed("InviteLink is already the default");
        }

        // 将之前的默认邀请链接置为非默认
        InviteLink inviteLink1 = this.lambdaQuery().eq(InviteLink::getAntId, inviteLink.getAntId())
                .eq(InviteLink::getDefaultLink, 0)
                .one();
        this.updateById(InviteLink.builder().id(inviteLink1.getId()).defaultLink(1).build());

        // 将当前默认邀请链接置为默认
        this.updateById(InviteLink.builder().id(id).defaultLink(0).build());

        return RestResult.ok();
    }

    @Override
    public RestResult<Void> saveInviteLink(InviteLinkSaveRequestVO requestVO) {
        Long currentUserId = UserContext.getCurrentUserId();
        MemberInfo memberInfo = this.memberInfoMapper.selectById(currentUserId);
        if (Objects.isNull(memberInfo)) {
            return RestResult.failed("User does not exist");
        }

        List<InviteLink> inviteLinkList = this.lambdaQuery()
                .eq(InviteLink::getAntId, currentUserId)
                .eq(InviteLink::getDeleted, 0)
                .list();
        if (inviteLinkList.size() > 20) {
            return RestResult.failed("The count of inviteLink cannot exceed 20");
        }

        InviteLink inviteLink = this.lambdaQuery()
                .eq(InviteLink::getTitle, requestVO.getTitle())
                .eq(InviteLink::getDeleted, 0)
                .eq(InviteLink::getAntId, currentUserId)
                .one();
        if (Objects.nonNull(inviteLink)) {
            return RestResult.failed(String.format("You have used an invitation link called %s", requestVO.getTitle()));
        }

        // 查询当前用户邀请码信息
        InviteLink inviteLinkOfUser = this.lambdaQuery()
                .eq(InviteLink::getInviteCode, memberInfo.getReferrerCode())
                .one();

        boolean save = this.save(InviteLink.builder()
                .antId(currentUserId)
                .title(requestVO.getTitle())
                .defaultLink(CollectionUtils.isEmpty(inviteLinkList) ? 0 : 1)
                .level(memberInfo.getAntLevel())
                .treeFlag(Objects.isNull(inviteLinkOfUser) ? memberInfo.getId() : inviteLinkOfUser.getTreeFlag())
                .inviteCode(UniqueCodeGeneratorUtil.generateInvitationCode())
                .build()
        );
        if (save) {
            return RestResult.ok();
        }
        return RestResult.failed();
    }

    @Override
    public RestResult<PageReturn<InviteCodeInfoDTO>> inviteCodeList(InviteCodeInfoReq requestVO) {
        Page<InviteLink> page = new Page<>();
        // 构建分页
        page.setCurrent(requestVO.getPageNo())
                .setSize(requestVO.getPageSize())
                // 构建排序
                .setOrders(Collections.singletonList(OrderItem.desc("create_time")));
        // 查询
        Page<InviteLink> inviteLinkPage = this.lambdaQuery()
                .eq(InviteLink::getAntId, requestVO.getMemberId())
                .eq(InviteLink::getDeleted, 0)
                .page(page);
        List<InviteLink> inviteLinkList = inviteLinkPage.getRecords();
        // 组转返回
        List<InviteCodeInfoDTO> resultList = CollectionUtils.isEmpty(inviteLinkList) ? new ArrayList<>() :
                inviteLinkList.stream()
                        .filter(Objects::nonNull)
                        .map(item -> InviteCodeInfoDTO.builder()
                                .id(item.getId())
                                .memberId(item.getAntId())
                                .inviteCode(item.getInviteCode())
                                .defaultInviteCode(item.getDefaultLink())
                                .createTime(item.getCreateTime())
                                .build())
                        .collect(Collectors.toList());
        return RestResult.ok(PageUtils.flush(page, resultList));
    }

    @Override
    public String getDefaultInviteCode(Long currentUserId) {
        return this.baseMapper.getDefaultInviteCode(currentUserId);
    }
}
