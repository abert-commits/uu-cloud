package org.uu.wallet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.uu.wallet.entity.TronWallet;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 波场钱包地址表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-07-12
 */
@Mapper
public interface TronWalletMapper extends BaseMapper<TronWallet> {

}
