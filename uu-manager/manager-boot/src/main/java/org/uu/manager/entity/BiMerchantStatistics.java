package org.uu.manager.entity;

import java.math.BigDecimal;

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
 * 商户统计报表
 * </p>
 *
 * @author 
 * @since 2024-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_merchant_statistics")
public class BiMerchantStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty("日期")
    private String dateTime;

    /**
     * 商户编码
     */
    @ApiModelProperty("商户编码")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 商户类型: 1.内部商户 2.外部商户
     */
    @ApiModelProperty("商户类型: 1.内部商户 2.外部商户")
    private String merchantType;

    /**
     * 会员ARB金额
     */
    @ApiModelProperty("会员ARB金额")
    private BigDecimal memberBalance;

    /**
     * 会员数量
     */
    @ApiModelProperty("会员数量")
    private Long memberNum;

    /**
     * 会员买入人数
     */
    @ApiModelProperty("会员买入人数")
    private Long buyNum;

    /**
     * 会员卖出人数
     */
    @ApiModelProperty("会员卖出人数")
    private Long sellNum;

    /**
     * 会员充值人数
     */
    @ApiModelProperty("会员充值人数")
    private Long rechargeNum;

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
     * 手续费
     */
    @ApiModelProperty("手续费")
    private BigDecimal cost;

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
     * 近30天活跃人数
     */
    @ApiModelProperty("近30天活跃人数")
    private Long thirtyActiveNum;

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
