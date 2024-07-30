package org.uu.manager.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.manager.entity.BiPaymentOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author
 */
@Mapper
public interface BiPaymentOrderMapper extends BaseMapper<BiPaymentOrder> {

    List<BiPaymentOrder> selectPaymentOrderList(@Param("startTime")String startTime,
                                                @Param("endTime")String endTime,
                                                @Param("dateStr")String dateStr,
                                                @Param("start")String start,
                                                @Param("end")String end);

    void updateByDateTime(@Param("vo")BiPaymentOrder biPaymentOrder);

    void deleteDailyByDateTime(@Param("dateTime")String dateTime);

}
