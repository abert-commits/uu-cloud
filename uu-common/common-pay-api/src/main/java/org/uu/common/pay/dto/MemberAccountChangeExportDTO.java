package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员账变记录
 *
 * @author
 */
@Data
@ApiModel(description = "会员账变记录")
public class MemberAccountChangeExportDTO {
    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String mid;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;

    /**
     * 平台订单号
     */
    @ApiModelProperty("平台订单号")
    private String orderNo;

    /**
     * 商户会员ID
     */
    @ApiModelProperty("商户会员ID")
    private String memberId;


    /**
     * 商户订单号
     */
    @ApiModelProperty("商户订单号")
    private String merchantOrder;

    /**
     * 账变类型: 1-买入, 2-卖出, 3-usdt充值,4-人工上分,5-人工下分
     */
    @ApiModelProperty("账变类型")
    private String changeType;

    /**
     * 账变前
     */
    @ApiModelProperty("账变前")
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    @ApiModelProperty("账变金额")
    private BigDecimal amountChange;

    /**
     * 账变后金额
     */
    @ApiModelProperty("账变后金额")
    private BigDecimal afterChange;

    /**
     * 创建时间
     */
    @ApiModelProperty("账变时间")
    private LocalDateTime createTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("备注")
    private String remark;

}