package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;

/**
 * 会员账变记录
 *
 * @author
 */
@Data
@ApiModel(description = "账变列表请求参数")
public class MemberAccountChangeReq extends PageRequest {


    /**
     * 账变类型: 1-买入, 2-卖出, 3-usdt充值,4-人工上分,7-人工下分
     */
    @ApiModelProperty("账变类型：1-买入, 2-卖出, 3-usdt充值,4-人工上分,7-人工下分")
    private String changeType;

    @ApiModelProperty("订单号")
    private String orderNo;

    @ApiModelProperty("会员ID")
    private String id;


    @ApiModelProperty("最低交易金额")
    private BigDecimal amountChangeStart;

    @ApiModelProperty("最高交易金额")
    private BigDecimal amountChangeEnd;

    @ApiModelProperty("起始时间")
    private String createTimeStart;


    @ApiModelProperty("结束时间")
    private String createTimeEnd;

    /**
     * 商户订单号
     */
    @ApiModelProperty("商户订单号")
    private String merchantOrder;

    /**
     * 会员ID/商户会员ID/会员账号
     */
    @ApiModelProperty("所属商户")
    private String obscureId;

    /**
     * 商户订单号或者订单号
     */
    @ApiModelProperty("商户订单号/订单号")
    private String obscureOrderNo;

    /**
     * 所属商户
     */
    @ApiModelProperty("所属商户")
    private String merchantName;

    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "是否人工 0 否 1 是")
    private Integer isManual;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "语言 zh-中文 en-英文")
    private String lang;

    private String source;

}