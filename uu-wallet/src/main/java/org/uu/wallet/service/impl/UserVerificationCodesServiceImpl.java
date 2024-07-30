package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.UserVerificationCodeslistPageDTO;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.UserVerificationCodes;
import org.uu.wallet.mapper.UserVerificationCodesMapper;
import org.uu.common.pay.req.UserTextMessageReq;
import org.uu.wallet.service.IUserVerificationCodesService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户验证码记录表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-01-20
 */
@Service
@RequiredArgsConstructor
public class UserVerificationCodesServiceImpl extends ServiceImpl<UserVerificationCodesMapper, UserVerificationCodes> implements IUserVerificationCodesService {

    private final WalletMapStruct walletMapStruct;

    @Override
    public PageReturn<UserVerificationCodeslistPageDTO> listPage(UserTextMessageReq req) {
        Page<UserVerificationCodes> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<UserVerificationCodes> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(req.getUserId())) {
            lambdaQuery.eq(UserVerificationCodes::getUserId, req.getUserId());
        }
        if (StringUtils.isNotBlank(req.getType())) {
            lambdaQuery.eq(UserVerificationCodes::getCodeType, req.getType());
        }
        if (StringUtils.isNotBlank(req.getReceiver())) {
            lambdaQuery.eq(UserVerificationCodes::getReceiver, req.getReceiver());
        }
        if (StringUtils.isNotBlank(req.getStartTime())) {
            lambdaQuery.ge(UserVerificationCodes::getSendTime, req.getStartTime());
        }
        if (StringUtils.isNotBlank(req.getEndTime())) {
            lambdaQuery.le(UserVerificationCodes::getSendTime, req.getEndTime());
        }
        lambdaQuery.orderByDesc(UserVerificationCodes::getSendTime);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<UserVerificationCodes> records = page.getRecords();
        List<UserVerificationCodeslistPageDTO> list = walletMapStruct.userVerificationCodesToDto(records);
        return PageUtils.flush(page, list);

    }
}
