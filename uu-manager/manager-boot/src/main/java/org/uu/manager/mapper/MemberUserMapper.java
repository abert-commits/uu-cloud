package org.uu.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import org.uu.common.pay.dto.MemberUserAuthDTO;
import org.uu.manager.entity.MemberUser;

@Mapper
public interface MemberUserMapper extends BaseMapper<MemberUser> {

    MemberUserAuthDTO getByUsername(@Param("userName") String userName);
}
