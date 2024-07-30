package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.KycPartners;

import java.util.List;

/**
 * <p>
 * kyc信息表 Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-04-20
 */
@Mapper
public interface KycPartnersMapper extends BaseMapper<KycPartners> {


}
