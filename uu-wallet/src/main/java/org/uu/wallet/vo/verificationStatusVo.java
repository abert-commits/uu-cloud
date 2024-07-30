package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Simon
 * @date 2023/12/09
 */
@Data
@ApiModel(description = "查看会员交易状态接口返回数据")
public class verificationStatusVo implements Serializable {

    /**
     * 是否实名认证
     */
    @ApiModelProperty(value = "会员是否实名认证 1: 已实名  0: 未实名")
    private Integer isVerified = 0;

    /**
     * 会员是否有未完成的买入订单
     */
    @ApiModelProperty(value = "会员是否有未完成的买入订单 1: 还有未完成的买入订单  0: 没有未完成的买入订单")
    private Integer hasUnfinishedBuyOrders = 0;

    /**
     * 未完成的买入订单信息
     */
    @ApiModelProperty(value = "未完成的买入订单信息")
    private PendingOrderVo pendingOrderVo;

    /**
     * 会员是否有买入权限
     */
    @ApiModelProperty(value = "会员是否有买入权限 1: 有权限  0: 无权限")
    private Integer hasBuyPermission = 1;

    /**
     * 会员是否被禁止买入
     */
    @ApiModelProperty(value = "会员是否被禁止买入 1: 被禁止买入  0: 允许买入")
    private Integer isBuyBanned = 0;

    /**
     * 会员禁止买入信息
     */
    @ApiModelProperty(value = "会员禁止买入信息")
    private DisableBuyingVo disableBuyingVo;


    /**
     * 是否开启实名认证交易限制
     */
    @ApiModelProperty(value = "是否开启实名认证交易限制 1: 开启  0: 关闭")
    private Integer isRealNameAuthEnabled = 0;
}