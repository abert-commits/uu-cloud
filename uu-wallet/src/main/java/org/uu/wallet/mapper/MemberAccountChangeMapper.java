package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.uu.wallet.dto.TodayAccountChangeDTO;
import org.uu.wallet.entity.MemberAccountChange;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Mapper
public interface MemberAccountChangeMapper extends BaseMapper<MemberAccountChange> {

    List<MemberAccountChange> selectUpSumInfo(@Param("dateStr") String dateStr);

    List<MemberAccountChange> selectDownSumInfo(@Param("dateStr") String dateStr);

    List<MemberAccountChange> selectBuyBounsInfo(@Param("dateStr") String dateStr);

    List<MemberAccountChange> selectSellBounsInfo(@Param("dateStr") String dateStr);

    List<MemberAccountChange> selectBuyTeamBounsInfo(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<MemberAccountChange> selectSellTeamBounsInfo(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<MemberAccountChange> selectPlatformDividends(@Param("startTime") String startTime, @Param("endTime") String endTime);

    TodayAccountChangeDTO getTodayTradeConditionByMid(@Param("mid") String mid);

    void updateCommissionFlagByOrderNo(@Param("memberId") Long memberId, @Param("changeType") Integer changeType, @Param("orderNo") String orderNo, @Param("commissionFlag") Integer commissionFlag);

}
