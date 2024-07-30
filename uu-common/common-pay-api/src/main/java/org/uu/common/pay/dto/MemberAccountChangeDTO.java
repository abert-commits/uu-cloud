package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员账变记录
 *
 * @author
 */
@Data
@ApiModel(description = "会员账变记录")
public class MemberAccountChangeDTO {
    @ApiModelProperty("主键")
    private long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String mid;

    /**
     * 币种
     */
    @ApiModelProperty("币种")
    private String currentcy;

    /**
     * 账变类型: 1-买入, 2-卖出, 3-usdt充值,4-人工上分,5-人工下分
     */
    @ApiModelProperty("账变类型")
    private String changeType;

    /**
     * 账变类别：add-增加, sub-支出
     */
    @ApiModelProperty("账变类别")
    private String changeMode;

    /**
     * 平台订单号
     */
    @ApiModelProperty("平台订单号")
    private String orderNo;

    /**
     * 账变前
     */
    @ApiModelProperty("账变前")
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    @ApiModelProperty("变化金额")
    private BigDecimal amountChange;

    /**
     * 账变后金额
     */
    @ApiModelProperty("账变后金额")
    private BigDecimal afterChange;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    @ApiModelProperty("备注")
    private String remark;

    /**
     * 商户订单号
     */
    @ApiModelProperty("商户订单号")
    private String merchantOrder;

    /**
     * 商户会员ID
     */
    @ApiModelProperty("商户会员ID")
    private String memberId;

    /**
     * 所属商户
     */
    @ApiModelProperty("所属商户")
    private String merchantName;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;


    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty("支付方式")
    private String payType;
}