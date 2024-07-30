package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.KycApprovedOrder;

/**
 * <p>
 * 通过 KYC 验证完成的订单表 Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-05-03
 */
@Mapper
public interface KycApprovedOrderMapper extends BaseMapper<KycApprovedOrder> {
}
