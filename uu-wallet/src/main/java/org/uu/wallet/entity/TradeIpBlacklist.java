package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 交易IP黑名单表，用于存储不允许进行交易的IP地址
 * </p>
 *
 * @author 
 * @since 2024-02-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trade_ip_blacklist")
public class TradeIpBlacklist extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 黑名单中的IP地址，不允许重复
     */
    private String ipAddress;

    /**
     * 加入黑名单的原因
     */
    private String reason;

    /**
     * 是否被删除（软删除），1表示已删除，0表示未删除
     */
    private Integer deleted;


    /**
     * 状态 默认启用
     */
    @ApiModelProperty(value = "状态 0禁用 1启用")
    private String status;

}
