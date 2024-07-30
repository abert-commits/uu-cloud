package org.uu.common.pay.bo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MemberInfoBO implements Serializable {
    private static final long serialVersionUID = 7401215682542285973L;

    private Long id;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 手机号
     */
    private String mobileNumber;

    /**
     * 累计买入成功金额
     */
    private Integer totalBuySuccessAmount;

    /**
     * 累计卖出成功金额
     */
    private Integer totalSellSuccessAmount;

    /**
     * 上级邀请码
     */
    private String referrerCode;

    /**
     * 蚂蚁层级
     */
    private Integer antLevel;

    /**
     * 返佣比例(%)
     */
    private BigDecimal commissionRatio;

//    /**
//     * 累计返佣金额
//     */
//    private BigDecimal totalCommissionAmount;

    /**
     * 分红等级 0-未分红
     */
    private Integer dividendsLevel;
}
