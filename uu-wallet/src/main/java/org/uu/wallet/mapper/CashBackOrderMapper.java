package org.uu.wallet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.CashBackOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 退回订单表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-05-09
 */
@Mapper
public interface CashBackOrderMapper extends BaseMapper<CashBackOrder> {

}
