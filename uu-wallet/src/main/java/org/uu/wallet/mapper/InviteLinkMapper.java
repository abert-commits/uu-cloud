package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.InviteLink;

/**
 * <p>
 * 邀请链接表 Mapper 接口
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Mapper
public interface InviteLinkMapper extends BaseMapper<InviteLink> {
    @Select("SELECT invite_code FROM t_invite_link WHERE ant_id = #{currentUserId} AND default_link = 0")
    String getDefaultInviteCode(@Param("currentUserId") Long currentUserId);
}
