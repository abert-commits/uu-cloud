package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 后台控制开关表
 * </p>
 *
 * @author 
 * @since 2024-03-21
 */
@Data
@ApiModel(description = "预警配置参数")
public class TradeConfigWarningConfigUpdateReq implements Serializable {

    /**
     * 开关id
     */
    private Long id;

    /**
     * 预警余额
     */
    private BigDecimal warningBalance;

    /**
     * 确认超时未操作
     */
    private Integer warningConfirmOvertimeNotOperated;


}
