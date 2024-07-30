package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "会员信息表")
public class MemberCreditScoreInfoDTO implements Serializable {
    @ApiModelProperty("会员id")
    private Long memberId;
    /**
     * 信用分
     */
    @ApiModelProperty("信用分")
    private BigDecimal creditScore;

    @ApiModelProperty("最近3条信用分记录")
    private List<CreditScoreLogsDTO> lastCreditScoreLogList;
}