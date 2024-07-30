package org.uu.common.core.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.uu.common.core.enums.MemberAccountChangeEnum;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * kyc自动完成消息体
 *
 * @author lukas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class KycCompleteMessage implements Serializable {
    private static final long serialVersionUID = -2287509894807830107L;

    private String kycId;
    private boolean isManual;
    private String type;
    private String sellerOrder;
    private String sellerMemberId;
    private String buyerOrder;
    private String buyerMemberId;
    private BigDecimal orderAmount;
    private String currency;
    private String detail;
    private String utr;
    private String remark;
    private String updateBy;
}
