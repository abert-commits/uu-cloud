package org.uu.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.uu.common.pay.bo.MemberInfoBO;
import org.uu.common.pay.dto.*;
import org.uu.wallet.entity.MemberInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Mapper
public interface MemberInfoMapper extends BaseMapper<MemberInfo> {
    MemberAuthDTO getByUsername(@Param("userName") String userName);

    MemberInfo getMemberInfoById(@Param("mid") String mid);

    MemberInfo getMemberInfoByIdForUpdate(@Param("mid") String mid);

    int updateBalanceById(@Param("finalAmount") BigDecimal finalAmount, @Param("mid") String mid,@Param("frozenAmountFlag") String frozenAmountFlag,
                           @Param("frozenAmount") BigDecimal frozenAmount);

    /**
     * 查询会员信息 加上排他行锁
     *
     * @param id
     * @return {@link MemberInfo}
     */
    @Select("SELECT * FROM member_info WHERE id = #{id} FOR UPDATE")
    MemberInfo selectMemberInfoForUpdate(Long id);


    Integer updateByMemberId(@Param("vo") MemberInfo memberInfo);

    List<MerchantActivationInfoDTO> selectMerchantInfoList();

    BigDecimal selectMemberTotalBalance();

    List<MerchantActivationInfoDTO> selectActiveInfoList(@Param("dateStr") String dateStr);

    List<MerchantActivationInfoDTO> selectActiveInfoMonthList(@Param("dateStr") String dateStr);

    List<MemberInfo> selectSumInfo();

    List<MemberInfo> selectSumNumInfo();

    Long selectActiveNum(@Param("startTime") String startTime, @Param("endTime") String endTime);

    Integer automicDelegation(@Param("startTime") String startTime, @Param("endTime") String endTime);


    Long selectRealNameNum();

    Long selectBuyNum();

    Long selectSellNum();

    Long selectBuyAndSellNum();

    Long selectBuyUsdtNum();

    Long selectBuyDisableFuture();

    Long selectSellDisableFuture();

    List<MemberInfo> selectMemberInfoInfo();


    List<MemberInfo> selectMerchantActiveNum(@Param("startTime") String startTime, @Param("endTime") String endTime);

    List<MemberInfo> selectMerchantRealNameNum();

    List<MemberInfo> selectMerchantBuyNum();

    List<MemberInfo> selectMerchantSellNum();

    List<MemberInfo> getRechargeInfo(@Param("list") List<MemberInfo> list);

    List<MemberInfo> getWithdrawInfo(@Param("list") List<MemberInfo> list);

    void updateRechargeInfo(@Param(value = "list") List<MemberInfo> userIdList);

    void updateWithdrawInfo(@Param("id")Long id, @Param("withdrawNum")Long withdrawNum, @Param("withdrawTotalAmount")BigDecimal withdrawTotalAmount);

    List<MemberInfo> selectMyPage(@Param("page")long page, @Param("size")long size, @Param(value = "userIdList") List<String> userIdList);

    long count(@Param(value = "userIdList") List<String> userIdList);

    List<MemberInfo> selectTaskReward();

    // 更新会员状态信息
    void updateMemberInfoStatus(@Param("id")String id, @Param("status")String status,@Param("buyStatus")String buyStatus, @Param("sellStatus")String sellStatus);


    MemberAuthDTO getByAppUsername(@Param("userName") String userName);

    List<MemberLevelInfoDTO> getLevelNum(@Param("merchantCode")String merchantCode);

    Long selectblackMemberNum();

    List<MemberInfoBO> selectMemberInfoByIds(@Param(value = "uidList") List<Long> uidList);

    /**
     * 根据当前UID获取买入奖励比率
     * @param uid 当前UID
     * @return 当前UID的买入奖励比率
     */
    @Select("SELECT buy_bonus_proportion FROM member_info WHERE member_id = #{uid}")
    BigDecimal selectBuyBonusProportion(Long uid);

    /**
     * 根据当前UID获取卖出奖励比率
     * @param uid 当前UID
     * @return 当前UID的卖出奖励比率
     */
    @Select("SELECT sell_bonus_proportion FROM member_info WHERE member_id = #{uid}")
    BigDecimal selectSellBonusProportion(Long uid);

    /**
     * 获取在线用户数量
     * @return Long
     */
    @Select("select count(1) from member_info where online_status=1")
    Long selectOnlineMemberCount();

    /**
     * 获取委托用户数量和委托资金池
     * @return Long
     */
    @Select("select count(1) as delegationMemberCount,sum(balance) as delegationAmount from uu_wallet.member_info where delegation_status=1")
    MemberOverviewDTO selectDelegatedMemberCount();
}
