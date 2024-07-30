package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author admin
 * @date 2024/3/12 18:08
 */
@Data
@ApiModel(description = "申诉")
public class AppealOrderExportDTO {


    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String mid;

    /**
     * 申诉类型: 1-提现申诉 2-充值申诉
     */
    @ApiModelProperty("申诉类型 1-提现申诉 2-充值申诉")
    private String appealType;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 提现订单号
     */
    @ApiModelProperty("卖出订单号")
    private String withdrawOrderNo;

    /**
     * 充值订单号
     */
    @ApiModelProperty("买入订单号")
    private String rechargeOrderNo;

    /**
     * 申诉状态: 1-代处理 2-申诉成功 3-申诉失败
     */
    @ApiModelProperty("申诉状态: 1-代处理 2-申诉成功 3-申诉失败")
    private String appealStatus;


    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private String orderAmount;


    @ApiModelProperty("实际金额")
    private String actualAmount;


    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


    /**
     * 收款类型 1-UPI 2-银行卡
     */
    @ApiModelProperty(value = "支付类型")
    private String payType;
}
