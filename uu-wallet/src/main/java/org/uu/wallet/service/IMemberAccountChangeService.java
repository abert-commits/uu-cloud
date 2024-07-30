package org.uu.wallet.service;


import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.bo.MemberAccountChangeBO;
import org.uu.common.pay.dto.MemberAccountChangeDTO;
import org.uu.common.pay.dto.MemberAccountChangeExportDTO;
import org.uu.common.pay.req.MemberAccountChangeReq;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.pay.vo.response.MemberAccountChangeDetailResponseVO;
import org.uu.common.pay.vo.response.MemberAccountChangeResponseVO;
import org.uu.wallet.entity.MemberAccountChange;
import org.uu.wallet.req.ViewTransactionHistoryReq;
import org.uu.wallet.vo.ViewTransactionHistoryVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author
 */
public interface IMemberAccountChangeService extends IService<MemberAccountChange> {

    PageReturn<MemberAccountChangeDTO> listPage(MemberAccountChangeReq req);


    PageReturn<MemberAccountChangeExportDTO> listpageForExport(MemberAccountChangeReq req);


    /**
     * 记录会员账变
     *
     * @param mid             会员ID
     * @param changeAmount    账变金额
     * @param changeType      交易类型
     * @param orderNo         订单号
     * @param previousBalance 账变前余额
     * @param newBalance      账变后余额
     * @param merchantOrder   商户订单号
     * @param payType         支付方式
     * @param remark          备注
     * @return {@link Boolean}
     */
    Boolean recordMemberTransaction(String mid, BigDecimal changeAmount, String changeType, String orderNo, BigDecimal previousBalance, BigDecimal newBalance, String merchantOrder, String payType, String remark);


    /**
     * 交易记录
     *
     * @param viewTransactionHistoryReq
     * @return {@link RestResult}<{@link PageReturn}<{@link ViewTransactionHistoryVo}>>
     */
    RestResult<PageReturn<ViewTransactionHistoryVo>> viewTransactionHistory(ViewTransactionHistoryReq viewTransactionHistoryReq);

    /**
     * 根据用户ID获取买入和卖出列表
     *
     * @param uidList             用户ID
     * @param startTime           开始时间 为null则查询所有
     * @param includeCommission   是否包含返佣
     * @param includeReward       是否包含奖励(买入奖励、卖出奖励、平台分红)
     * @param includeUsdtRecharge 是否包含USDT买入
     * @return {@link List<MemberAccountChangeBO>}
     */
    List<MemberAccountChangeBO> queryAccountChangeListByIds(
            List<Long> uidList,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Boolean includeCommission,
            Boolean includeReward,
            Boolean includeUsdtRecharge
    );

    /**
     * 根据订单号修改返佣标识
     *
     * @param orderNo        订单号
     * @param commissionFlag 标识
     */
    void updateCommissionFlagByOrderNo(Long memberId, Integer changeType, String orderNo, Integer commissionFlag);

    /**
     * 根据订单号查询账变记录(仅买入奖励、卖出奖励、买入返佣、卖出返佣、分红)
     *
     * @param orderNo 订单号
     */
    RestResult<MemberAccountChangeDetailResponseVO> selectByOrderNo(String orderNo);

    /**
     * 返佣奖励记录(买入返佣 + 卖出返佣)
     *
     * @param requestVO 分页参数
     */
    RestResult<PageReturn<MemberAccountChangeResponseVO>> commissionRewardPage(PageRequestHome requestVO);
}
