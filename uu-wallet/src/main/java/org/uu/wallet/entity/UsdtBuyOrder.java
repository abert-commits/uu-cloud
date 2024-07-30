package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.wallet.Enum.OrderStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("usdt_buy_order")
public class UsdtBuyOrder extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 订单号
     */
    private String platformOrder;

    /**
     * USDT地址
     */
    private String usdtAddr;

    /**
     * USDT数量
     */
    private BigDecimal usdtNum;

    /**
     * USDT实际数量
     */
    private BigDecimal usdtActualNum;

    /**
     * ARB数量
     */
    private BigDecimal arbNum;

    /**
     * ARB实际数量
     */
    private BigDecimal arbActualNum;

    /**
     * 订单状态 默认值: 待支付
     */
    private String status = OrderStatusEnum.BE_PAID.getCode();

    /**
     * USDT支付凭证
     */
    private String usdtProof;

    private String remark;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;


    @TableField(exist = false)
    @ApiModelProperty("usdt数量总计")
    private BigDecimal usdtNumTotal;
    @TableField(exist = false)
    @ApiModelProperty("itoken数量总计")
    private BigDecimal arbNumTotal;
    @TableField(exist = false)
    @ApiModelProperty("usdt实际数量总计")
    private BigDecimal usdtActualNumTotal;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 汇率
     */
    private BigDecimal exchangeRates;

    /**
     * 交易id 唯一索引限制
     */
    private String txid;

    /**
     * 付款人地址
     */
    private String fromAddress;


    /**
     * 订单完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

}