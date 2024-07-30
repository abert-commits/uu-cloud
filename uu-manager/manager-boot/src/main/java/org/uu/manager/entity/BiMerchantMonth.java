package org.uu.manager.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 商户日报表
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bi_merchant_month")
public class BiMerchantMonth implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 日期
     */
    @ApiModelProperty(value = "日期")
    private String dateTime;

    /**
     * 商户编码
     */
    @ApiModelProperty(value = "商户编码")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    /**
     * 代收金额
     */
    @ApiModelProperty(value = "代收金额")
    private BigDecimal payMoney = BigDecimal.ZERO;

    /**
     * 代付金额
     */
    @ApiModelProperty(value = "代付金额")
    private BigDecimal withdrawMoney = BigDecimal.ZERO;

    /**
     * 代收下单总笔数
     */
    @ApiModelProperty(value = "代收下单总笔数")
    private Long payOrderNum = 0L;

    /**
     * 代收成功笔数
     */
    @ApiModelProperty(value = "代收成功笔数")
    private Long paySuccessOrderNum = 0L;

    /**
     * 总费用
     */
    @ApiModelProperty(value = "总费用")
    private BigDecimal totalFee = BigDecimal.ZERO;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 上一次执行时间：22:05
     */
    private String lastMinute;

    /**
     * 商户类型: 1.内部商户 2.外部商户
     */
    @ApiModelProperty(value = "商户类型: 1.内部商户 2.外部商户")
    private String merchantType;

    /**
     * 代付下单总笔数
     */
    @ApiModelProperty(value = "代付下单总笔数")
    private Long withdrawOrderNum = 0L;

    /**
     * 代付成功笔数
     */
    @ApiModelProperty(value = "代付成功笔数")
    private Long withdrawSuccessOrderNum = 0L;

    /**
     * 收付差额
     */
    @ApiModelProperty(value = "收付差额")
    private BigDecimal difference = BigDecimal.ZERO;

    @TableField(exist = false)
    @ApiModelProperty(value = "代收成功率")
    private Double paySuccessRate;

    @TableField(exist = false)
    @ApiModelProperty(value = "代付成功率")
    private Double withdrawSuccessRate;

    @ApiModelProperty(value = "新增激活用户")
    private Long activationNewUser = 0L;

    @TableField(exist = false)
    @ApiModelProperty(value = "代收金额总计")
    private BigDecimal payMoneyTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "代付金额总计")
    private BigDecimal withdrawMoneyTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "代付金额总计")
    private BigDecimal differenceTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "代收代付费用总计")
    private BigDecimal feeTotal;

}