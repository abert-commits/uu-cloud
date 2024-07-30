package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "买入失败-有完成的订单-返回数据")
public class PendingOrderVo implements Serializable {

    /**
     * 当前未完成的订单号
     */
    @ApiModelProperty("当前未完成的订单号")
    private String platformOrder;


    /**
     * 订单状态
     */
    @ApiModelProperty("订单状态，取值说明： 3: 待支付, 4: 确认中, 5: 确认超时, 6: 申诉中, 11: 金额错误")
    private String orderStatus;

}