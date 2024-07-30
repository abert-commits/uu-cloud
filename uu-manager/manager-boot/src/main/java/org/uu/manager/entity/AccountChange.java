package org.uu.manager.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AccountChange implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 币种
     */
    private String currentcy;

    /**
     * 账变类型：add-增加, sub-支出
     */
    private String changeMode;

    /**
     * 账变类型：add-增加, sub-支出
     */
    private Integer changeType;

    /**
     * 商户订单号
     */
    private String orderNo;

    /**
     * 账变前
     */
    private BigDecimal beforeChange;

    /**
     * 变化金额
     */
    private BigDecimal amountChange;


    /**
     * 账变后金额
     */
    private BigDecimal afterChange;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    private String createBy;

    /**
     * 修改人
     */
    private LocalDateTime updateBy;


}