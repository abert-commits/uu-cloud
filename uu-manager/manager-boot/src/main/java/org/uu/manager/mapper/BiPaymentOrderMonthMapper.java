package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.manager.entity.BiPaymentOrder;
import org.uu.manager.entity.BiPaymentOrderMonth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author
 */
@Mapper
public interface BiPaymentOrderMonthMapper extends BaseMapper<BiPaymentOrderMonth> {

    void updateByDateTime(@Param("vo")BiPaymentOrderMonth biPaymentOrderMonth);

    void deleteMonthByDateTime(@Param("dateTime")String dateTime);

    List<BiPaymentOrderMonth> selectDataInfoByMonth(@Param("dateTime")String dateTime);

}
