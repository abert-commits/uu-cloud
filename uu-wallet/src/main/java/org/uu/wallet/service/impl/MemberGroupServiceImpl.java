package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MemberGroupListPageDTO;
import org.uu.common.pay.req.MemberGroupListPageReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.MemberGroup;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.mapper.MemberGroupMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.service.IMemberGroupService;
import org.uu.wallet.service.IMemberInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberGroupServiceImpl extends ServiceImpl<MemberGroupMapper, MemberGroup> implements IMemberGroupService {
    private final WalletMapStruct walletMapStruct;
    @Autowired
    private IMemberInfoService memberInfoService;
    private final MemberInfoMapper memberInfoMapper;
    private final MemberGroupMapper memberGroupMapper;

    @Override
    public PageReturn<MemberGroupListPageDTO> listPage(MemberGroupListPageReq req) {
        Page<MemberGroup> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MemberGroup> lambdaQuery = lambdaQuery();
//        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getCode())) {
//            lambdaQuery.eq(MemberGroup::getCode, req.getCode());
//        }
//        if (!com.alibaba.nacos.api.utils.StringUtils.isBlank(req.getUsername())) {
//            lambdaQuery.eq(MemberGroup::getUsername, req.getUsername());
//        }
        //baseMapper.selectPage(page, fetchMemberGroup);
      //  IPage<MemberGroup> memberGroup = memberGroupMapper.fetchMemberGroup(page);
        List<MemberGroup> records = new ArrayList<MemberGroup>();
       List<MemberGroupListPageDTO> list = walletMapStruct.MemberGroupTransform(records);
        return PageUtils.flush(page, list);
    }


    /**
     * 根据会员的交易数据 进行会员分组
     *
     * @param memberInfo
     * @return {@link MemberInfo}
     */
    @Override
    public MemberInfo determineMemberGroup(MemberInfo memberInfo) {

        //查询所有分组信息
        List<MemberGroup> groups = lambdaQuery().list();

        if (groups != null && !groups.isEmpty()) {
            // 按卖出金额降序排序分组
            groups.sort(Comparator.comparing(MemberGroup::getSellAmount).reversed());

            // 遍历分组，找到第一个符合条件的分组
            for (MemberGroup group : groups) {
                if (memberInfo.getTotalSellSuccessAmount().compareTo(group.getSellAmount()) >= 0 //累计成功卖出金额
                        && memberInfo.getTotalBuySuccessAmount().compareTo(group.getBuyAmount()) >= 0 //累计成功买入金额
                        && memberInfo.getTotalSellSuccessCount() >= group.getSellCount() //累计卖出成功次数
                        && memberInfo.getTotalBuySuccessCount() >= group.getBuyCount() //累计买入成功次数
                        && group.getId() != 1//排除默认分组
                        && group.getId() != 2//排除黑名单分组
                ) {
                    log.info("更新会员分组: 会员账号: {}, 新组名: {}", memberInfo.getMemberAccount(), group.getName());
                    //设置会员分组
                    memberInfo.setMemberGroup(group.getId());
                    //更新会员信息
                    break;
                }
            }
        }
        return memberInfo;
    }

    /**
     * 根据分组id获取权限列表
     *
     * @param id
     * @return {@link String}
     */
    @Override
    public String getAuthListById(Long id) {
        MemberGroup memberGroup = lambdaQuery().eq(MemberGroup::getId, id).select(MemberGroup::getAuthList).one();
        return memberGroup != null ? memberGroup.getAuthList() : null;
    }
}
