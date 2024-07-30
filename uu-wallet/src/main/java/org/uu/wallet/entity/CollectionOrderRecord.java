package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <p>
 * 归集订单记录
 * </p>
 *
 * @author
 * @since 2024-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("collection_order_record")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionOrderRecord extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归集订单号
     */
    private String collectionOrderId;

    /**
     * 归集类型：1：自动，2：人工
     */
    private Integer collectionType;

    /**
     * 归集数量
     */
    private BigDecimal collectionAmount;

    /**
     * 源地址
     */
    private String fromAddress;

    /**
     * 目标地址
     */
    private String toAddress;

    /**
     * 归集金额类型如：usdt,trx
     */
    private String collectionBalanceType;

    /**
     * 归集状态 1: 归集中 2: 归集成功 3: 归集失败
     */
    private Integer status;

    /**
     * 是否删除 0 未删除 ,1 已删除
     */
    private Integer deleted;

    /**
     * 交易ID
     */
    private String txid;

}
