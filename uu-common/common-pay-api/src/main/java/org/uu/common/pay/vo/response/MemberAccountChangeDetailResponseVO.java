package org.uu.common.pay.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("账变记录详情响应实体类")
public class MemberAccountChangeDetailResponseVO implements Serializable {
    private static final long serialVersionUID = -2499640712757178494L;

    @ApiModelProperty("金额")
    private BigDecimal amount;

    @ApiModelProperty("类型 1-买入 2-卖出 8-买入奖励 9-卖出奖励 15-买入返佣 18-卖出返佣 16-平台分红 4-人工上分 7-人工下分")
    private String changeType;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("UTR")
    private String utr;

    @ApiModelProperty("upiId")
    private String upiId;

    @ApiModelProperty("upiName")
    private String upiName;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("付款人_kycAccount")
    private String kycAccount;

    @ApiModelProperty("付款人_kycBankName")
    private String kycBankName;

    @ApiModelProperty("USDT地址")
    private String usdtAddress;

    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;
}
