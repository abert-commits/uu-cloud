package org.uu.manager.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;

import org.uu.common.pay.dto.MemberUserAuthDTO;
import org.uu.manager.entity.MemberUser;
import org.uu.manager.mapper.MemberUserMapper;
import org.uu.manager.service.IMemberUserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserServiceImpl extends ServiceImpl<MemberUserMapper, MemberUser> implements IMemberUserService {
    @Override
    public MemberUserAuthDTO getByUsername(String username) {
        MemberUserAuthDTO memberUserAuthDTO = this.baseMapper.getByUsername(username);
        return memberUserAuthDTO;
    }

}