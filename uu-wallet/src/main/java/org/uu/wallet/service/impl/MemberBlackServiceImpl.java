package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.MemberBlackDTO;
import org.uu.common.pay.req.MemberBlackReq;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.common.redis.util.RedisUtils;
import org.uu.wallet.Enum.BuyStatusEnum;
import org.uu.wallet.Enum.MemberStatusEnum;
import org.uu.wallet.Enum.SellStatusEnum;
import org.uu.wallet.config.WalletMapStruct;
import org.uu.wallet.entity.MemberBlack;
import org.uu.wallet.mapper.MemberBlackMapper;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.service.IMemberBlackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.uu.wallet.service.IMemberInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 会员黑名单 服务实现类
 * </p>
 *
 * @author 
 * @since 2024-03-29
 */
@Service
@RequiredArgsConstructor
public class MemberBlackServiceImpl extends ServiceImpl<MemberBlackMapper, MemberBlack> implements IMemberBlackService {

    private final WalletMapStruct walletMapStruct;
    private final MemberBlackMapper memberBlackMapper;
    private final MemberInfoMapper memberInfoMapper;
    private final RedisUtils redisUtil;


    @Override
    public PageReturn<MemberBlackDTO> listPage(MemberBlackReq req) {
        Page<MemberBlack> page = new Page<>();
        page.setCurrent(req.getPageNo());
        page.setSize(req.getPageSize());
        LambdaQueryChainWrapper<MemberBlack> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(req.getMemberId())) {
            lambdaQuery.eq(MemberBlack::getMemberId, req.getMemberId());
        }
        if (StringUtils.isNotBlank(req.getMerchantMemberId())) {
            lambdaQuery.eq(MemberBlack::getMerchantMemberId, req.getMerchantMemberId());
        }
        if (StringUtils.isNotBlank(req.getMemberAccount())) {
            lambdaQuery.eq(MemberBlack::getMemberAccount, req.getMemberAccount());
        }
        if (StringUtils.isNotBlank(req.getMerchantCode())) {
            lambdaQuery.eq(MemberBlack::getMerchantCode, req.getMerchantCode());
        }
        lambdaQuery.orderByDesc(MemberBlack::getOpTime);
        baseMapper.selectPage(page, lambdaQuery.getWrapper());
        List<MemberBlack> records = page.getRecords();
        List<MemberBlackDTO> list = walletMapStruct.memberBlackToDto(records);
        return PageUtils.flush(page, list);
    }

    @Override
    public RestResult removeBlack(MemberBlackReq req) {
        // 更新会员信息
        memberInfoMapper.updateMemberInfoStatus(req.getMemberId(), MemberStatusEnum.ENABLE.getCode(), BuyStatusEnum.ENABLE.getCode(), SellStatusEnum.ENABLE.getCode());
        this.baseMapper.delete(lambdaQuery().eq(MemberBlack::getMemberId, req.getMemberId()).getWrapper());
        redisUtil.lRemove(RedisKeys.MEMBER_BALCK_LIST, 0, req.getMemberId());
        return RestResult.ok();
    }


    @Override
    public Boolean addBlack(MemberBlack req) {

        LambdaQueryChainWrapper<MemberBlack> lambdaQuery = new LambdaQueryChainWrapper<>(memberBlackMapper);
        lambdaQuery.eq(MemberBlack::getMemberId, req.getMemberId());
        Integer count = this.baseMapper.selectCount(lambdaQuery.getWrapper());
        if(count > 0){
            return false;
        }
        // 更新会员信息
        //memberInfoMapper.updateMemberInfoStatus(req.getMemberId(),MemberStatusEnum.DISABLE.getCode(), BuyStatusEnum.DISABLE.getCode(), SellStatusEnum.DISABLE.getCode());
        redisUtil.lSet(RedisKeys.MEMBER_BALCK_LIST, req.getMemberId());
        this.baseMapper.insert(req);

        //todo 踢人
        return true;
    }
}
