package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.common.pay.dto.MerchantActivationInfoDTO;
import org.uu.common.pay.dto.UserAuthDTO;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.entity.MerchantInfo;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author
 */
@Mapper
public interface MerchantInfoMapper extends BaseMapper<MerchantInfo> {
    UserAuthDTO getByMerchantCode(@Param("merchantCode") String merchantCode);

    MerchantInfo getMerchantInfoById(@Param("code") String code);

    MerchantInfo getMerchantInfoByCode(@Param("code") String code);

    boolean updateBalanceByCode(@Param("balance") BigDecimal balance, @Param("code") String code);

    Integer updateMerchantPwd(@Param("id") Long userId, @Param("password") String password, @Param("passwordTips") String passwordTips);

    int updateUsdtAddress(@Param("id") Long id, String usdtAddress);

    Map<Integer, String> getMerchantName();


    /**
     * 查询商户信息 加上排他行锁
     *
     * @param code
     * @return {@link MemberInfo}
     */
    @Select("SELECT * FROM merchant_info WHERE code = #{code} FOR UPDATE")
    MerchantInfo selectMerchantInfoForUpdate(String code);

    void updateMerchantPublicKey(@Param("id") Long id, @Param("merchantPublicKey") String merchantPublicKey);

    void updateMerchantGoogleSecretKey(@Param("merchantCode") String merchantCode, @Param("newGoogleSecretKey") String newGoogleSecretKey, @Param("flag") Integer flag);

    void updateUserGoogelBindFlag(@Param("id") Long id, @Param("flag") Integer flag);

    MerchantActivationInfoDTO calcActiveNum();

}
