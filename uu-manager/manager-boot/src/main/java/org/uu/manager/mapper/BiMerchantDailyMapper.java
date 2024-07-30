package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.manager.entity.BiMerchantDaily;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.uu.manager.entity.BiPaymentOrder;

import java.util.List;

/**
 * @author
 */
@Mapper
public interface BiMerchantDailyMapper extends BaseMapper<BiMerchantDaily> {

    void updateByDateTime(@Param("vo") BiMerchantDaily biMerchantDaily);

    void updateWithdrawByDateTime(@Param("vo") BiMerchantDaily biMerchantDaily);

    void deleteDailyByDateTime(@Param("dateTime")String dateTime);

    List<BiMerchantDaily> selectInfoByDate(@Param("dateTime")String dateTime);
}
