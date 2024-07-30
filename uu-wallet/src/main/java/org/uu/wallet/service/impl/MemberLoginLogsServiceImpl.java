package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MemberLoginLogsDTO;
import org.uu.common.pay.req.MemberLoginLogsReq;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.MemberLoginLogs;
import org.uu.wallet.entity.MemberOperationLogs;
import org.uu.wallet.mapper.MemberLoginLogsMapper;
import org.uu.wallet.service.IMemberLoginLogsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 会员登录日志表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-01-13
 */
@Service
@RequiredArgsConstructor
public class MemberLoginLogsServiceImpl extends ServiceImpl<MemberLoginLogsMapper, MemberLoginLogs> implements IMemberLoginLogsService {
    private final WalletMapStruct walletMapStruct;

    @Override
    public PageReturn<MemberLoginLogsDTO> listPage(MemberLoginLogsReq req) {
        Page<MemberLoginLogs> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MemberLoginLogs> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(req.getUserId())) {
            lambdaQuery.eq(MemberLoginLogs::getMemberId, req.getUserId());
        }
        if (StringUtils.isNotBlank(req.getType())) {
            lambdaQuery.eq(MemberLoginLogs::getAuthenticationMode, req.getType());
        }
        if (StringUtils.isNotBlank(req.getLoginIp())) {
            lambdaQuery.eq(MemberLoginLogs::getIpAddress, req.getLoginIp());
        }
        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(MemberLoginLogs::getLoginTime, req.getStartTime());
        }
        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(MemberLoginLogs::getLoginTime, req.getEndTime());
        }
        if (StringUtils.isNotBlank(req.getMemberAccount())) {
            lambdaQuery.eq(MemberLoginLogs::getUsername, req.getMemberAccount());
        }
        lambdaQuery.orderByDesc(MemberLoginLogs::getId);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MemberLoginLogs> records = page.getRecords();
        List<MemberLoginLogsDTO> list = walletMapStruct.memberLoginLogsToDto(records);
        return PageUtils.flush(page, list);
    }
}
