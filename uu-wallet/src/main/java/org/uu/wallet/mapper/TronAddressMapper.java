package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.uu.wallet.entity.TronAddress;

/**
 * <p>
 * 波场用户钱包 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-07-03
 */
@Mapper
public interface TronAddressMapper extends BaseMapper<TronAddress> {

    /**
     * 查询地址信息 加上排他行锁
     *
     * @param address
     * @return {@link TronAddress}
     */
    @Select("SELECT * FROM tron_address WHERE address = #{address} FOR UPDATE")
    TronAddress selectTronAddressByAddress(String address);

}
