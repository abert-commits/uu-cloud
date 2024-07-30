package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * 波场用户钱包
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tron_address")
public class TronAddress extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 会员ID
     */
    private String memberId;

    /**
     * U地址
     */
    private String address;

    /**
     * 订单总数
     */
    @ApiModelProperty("订单总数")
    private Long orderTotal;

    /**
     * 订单成功数
     */
    @ApiModelProperty("订单成功数")
    private Long orderSuccessNum;

    /**
     * U地址_hex格式
     */
    private String hexAddress;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * TRX余额
     */
    private BigDecimal trxBalance;

    /**
     * USDT余额
     */
    private BigDecimal usdtBalance;

    /**
     * 授权标记 0 未授权  1.已授权
     */
    private Integer approveFlag;

    /**
     * 是否删除 0 未删除 ,1 已删除
     */
    private Integer deleted;
}
