package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author admin
 * @date 2024/3/8 14:37
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class BiMerchantDailyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    @ApiModelProperty("日期")
    private String dateTime;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户")
    private String merchantName;

    /**
     * 商户类型: 1.内部商户 2.外部商户
     */
    @ApiModelProperty("商户类型: 1.内部商户 2.外部商户")
    private String merchantType;


    /**
     * 代收金额
     */
    @ApiModelProperty("代收金额")
    private String payMoney;



    /**
     * 代收下单总笔数
     */
    @ApiModelProperty("代收下单笔数")
    private Long payOrderNum = 0L;

    /**
     * 代收成功笔数
     */
    @ApiModelProperty("代收成功笔数")
    private Long paySuccessOrderNum = 0L;


    @TableField(exist = false)
    @ApiModelProperty(value = "代收成功率")
    private String paySuccessRate;

    /**
     * 代付金额
     */
    @ApiModelProperty("代付金额")
    private String withdrawMoney;


    /**
     * 代付下单总笔数
     */
    @ApiModelProperty("代付下单笔数")
    private Long withdrawOrderNum = 0L;

    /**
     * 代付成功笔数
     */
    @ApiModelProperty("代付成功笔数")
    private Long withdrawSuccessOrderNum = 0L;



    @ApiModelProperty(value = "代付成功率")
    private String withdrawSuccessRate;

    /**
     * 收付差额
     */
    @ApiModelProperty("收付差额")
    private String difference;

    /**
     * 总费用
     */
    @ApiModelProperty("代收代付费用")
    private String totalFee;
}
