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
@ApiModel(description = "交易银行卡配置信息")
public class TradeBankConfigUpdateReq implements Serializable {

    /**
     * 开关id
     */
    private Long id;

    /**
     * 最小银行卡号长度
     */
    private Integer minBankCodeNumber;

    /**
     * 最大银行卡号长度
     */
    private Integer maxBankCodeNumber;


}
