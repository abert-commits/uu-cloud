package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.CurrencyPayType;

/**
 * <p>
 * 币种对应的代收代付类型 Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-07-15
 */
@Mapper
public interface CurrencyPayTypeMapper extends BaseMapper<CurrencyPayType> {

}
