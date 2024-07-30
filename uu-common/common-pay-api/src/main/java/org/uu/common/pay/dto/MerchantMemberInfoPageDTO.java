package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "商户会员列表返回")
public class MerchantMemberInfoPageDTO implements Serializable {

    @ApiModelProperty("会员ID")
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("商户会员id")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;



    /**
     * 真实姓名
     */
    @ApiModelProperty("真实姓名")
    private String realName;

    /**
     * 状态 默认值 启用
     */
    @ApiModelProperty("状态 0禁止 1开启")
    private String status;

    /**
     * 在线状态 默认值 离线
     */
    @ApiModelProperty("在线 0离线 1 在线")
    private String onlineStatus;

    /**
     * 买入状态 默认值 开启
     */
    @ApiModelProperty("买入状态 0禁用 1启用")
    private String buyStatus;

    /**
     * 卖出状态 默认值 开启
     */
    @ApiModelProperty("卖出状态 0禁用 1启用")
    private String sellStatus;

    /**
     * 充值次数
     */
    @ApiModelProperty("充值次数")
    private Long rechargeNum = 0L;

    /**
     * 累计充值金额
     */
    @ApiModelProperty("累计充值金额")
    private BigDecimal rechargeTotalAmount = BigDecimal.ZERO;


    /**
     * 提现次数
     */
    @ApiModelProperty("提现次数")
    private Long withdrawNum = 0L;


    /**
     * 累计提现金额
     */
    @ApiModelProperty("累计提现金额")
    private BigDecimal withdrawTotalAmount = BigDecimal.ZERO;

}