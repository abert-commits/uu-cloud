package org.uu.manager.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会员统计报表
 * </p>
 *
 * @author 
 * @since 2024-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_member_statistics")
public class BiMemberStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty("日期")
    private String dateTime;

    /**
     * 总会员人数
     */
    @ApiModelProperty("总会员人数")
    private Long memberTotalNum;

    /**
     * 商户会员人数
     */
    @ApiModelProperty("商户会员人数")
    private Long merchantMemberNum;

    /**
     * 钱包会员人数
     */
    @ApiModelProperty("钱包会员人数")
    private Long walletMemberNum;

    /**
     * 黑名单人数
     */
    @ApiModelProperty("黑名单人数")
    private Long blackMemberNum;

    /**
     * 昨日活跃人数
     */
    @ApiModelProperty("昨日活跃人数")
    private Long yesterdayActiveNum;

    /**
     * 近7日活跃人数
     */
    @ApiModelProperty("近7日活跃人数")
    private Long sevenActiveNum;

    /**
     * 近30活跃人数
     */
    @ApiModelProperty("近30活跃人数")
    private Long thirtyActiveNum;

    /**
     * 参与买入人数
     */
    @ApiModelProperty("参与买入人数")
    private Long buyNum;

    /**
     * 会员提现人数
     */
    @ApiModelProperty("会员提现人数")
    private Long withdrawNum;

    /**
     * 实名认证人数
     */
    @ApiModelProperty("实名认证人数")
    private Long realNameNum;

    /**
     * 参与卖出人数
     */
    @ApiModelProperty("参与卖出人数")
    private Long sellNum;

    /**
     * 参与买和卖人数
     */
    @ApiModelProperty("参与买和卖人数")
    private Long buyAndSellNum;

    /**
     * 参与usdt买入人数
     */
    @ApiModelProperty("参与usdt买入人数")
    private Long buyUsdtActiveNum;

    /**
     * 参与充值
     */
    @ApiModelProperty("参与充值")
    private Long rechargeActiveNum;

    /**
     * 买入禁用人数
     */
    @ApiModelProperty("买入禁用人数")
    private Long disableBuyNum;

    /**
     * 卖出禁用人数
     */
    @ApiModelProperty("卖出禁用人数")
    private Long disableSellNum;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
