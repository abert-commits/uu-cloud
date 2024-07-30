package org.uu.wallet.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
* 
*
* @author 
*/
    @Data
    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public class ApplyDistributedReq extends PageRequest {

    private long id;

            /**
            * 下单时间
            */
    private LocalDateTime orderTime;

            /**
            * 订单号
            */
    private String orderNo;

            /**
            * 商户
            */
    private String merchantCode;

            /**
            * 商户名称
            */
    private String username;

            /**
            * 下发usdt地址
            */
    private String usdtAddr;

            /**
            * 币种
            */
    private String currence;

            /**
            * 总额度
            */
    private BigDecimal balance;

    private BigDecimal amount;

            /**
            * 创建时间
            */
    private LocalDateTime createTime;

            /**
            * 创建人
            */
    private String createBy;

    private Integer remark;

            /**
            * 状态
            */
    private String status;

            /**
            * 操作时间
            */
    private LocalDateTime updateTime;

            /**
            * 修改人
            */
    private String updateBy;

    private String startTime;

    private String endTime;


}