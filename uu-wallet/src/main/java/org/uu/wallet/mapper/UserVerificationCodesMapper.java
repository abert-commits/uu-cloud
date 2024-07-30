package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.UserVerificationCodes;

/**
 * <p>
 * 用户验证码记录表 Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-01-20
 */
@Mapper
public interface UserVerificationCodesMapper extends BaseMapper<UserVerificationCodes> {

}
