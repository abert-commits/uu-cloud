package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.wallet.Enum.UsdtBuyStatusEnum;

import java.io.Serializable;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("usdt_config")
public class UsdtConfig extends BaseEntityOrder implements Serializable {


    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 主网络
     */
    private String networkProtocol;

    /**
     * usdt地址
     */
    private String usdtAddr;

    /**
     * 状态 默认值: 启用
     */
    private String status = UsdtBuyStatusEnum.ENABLE.getCode();

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否删除 默认值: 0
     */
    private Integer deleted = 0;

}