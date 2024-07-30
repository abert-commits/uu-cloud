package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.CorrelationMemberDTO;
import org.uu.common.pay.dto.MemberBlackDTO;
import org.uu.common.pay.req.MemberBlackReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.CorrelationMember;
import org.uu.wallet.entity.MemberAccountChange;
import org.uu.wallet.entity.MemberBlack;
import org.uu.wallet.mapper.CorrelationMemberMapper;
import org.uu.wallet.service.ICorrelationMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 关联会员信息 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-30
 */
@Service
@RequiredArgsConstructor
public class CorrelationMemberServiceImpl extends ServiceImpl<CorrelationMemberMapper, CorrelationMember> implements ICorrelationMemberService {


    private final WalletMapStruct walletMapStruct;

    @Override
    public PageReturn<CorrelationMemberDTO> listPage(MemberBlackReq req) {
        Page<CorrelationMember> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<CorrelationMember> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(req.getMemberId())) {
            lambdaQuery.eq(CorrelationMember::getMemberId, req.getMemberId());
        }
        if (StringUtils.isNotBlank(req.getMerchantMemberId())) {
            lambdaQuery.eq(CorrelationMember::getMerchantMemberId, req.getMerchantMemberId());
        }
        if (StringUtils.isNotBlank(req.getMemberAccount())) {
            lambdaQuery.eq(CorrelationMember::getMemberAccount, req.getMemberAccount());
        }
        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.ge(CorrelationMember::getMerchantCode, req.getMerchantCode());
        }
        if (StringUtils.isNotBlank(req.getRelationsIp())) {
            lambdaQuery.ge(CorrelationMember::getRelationsIp, req.getRelationsIp());
        }
        lambdaQuery.orderByDesc(CorrelationMember::getCreateTime);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<CorrelationMember> records = page.getRecords();
        List<CorrelationMemberDTO> list = walletMapStruct.correlationMemberToDto(records);
        return PageUtils.flush(page, list);
    }
}
