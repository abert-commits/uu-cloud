package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.manager.entity.BiMerchantPayOrderMonth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.uu.manager.entity.BiPaymentOrderMonth;

import java.util.List;

/**
 * @author
 */
@Mapper
public interface BiMerchantPayOrderMonthMapper extends BaseMapper<BiMerchantPayOrderMonth> {

    void updateByDateTime(@Param("vo")BiPaymentOrderMonth biPaymentOrderMonth);

    void deleteMonthByDateTime(@Param("dateTime")String dateTime);

    List<BiPaymentOrderMonth> selectDataInfoByMonth(@Param("dateTime")String dateTime);
}
