package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "USDT买入返回")
public class UsdtBuyOrderReq extends PageRequest {


    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;


    /**
     * 订单开始时间
     */
    @ApiModelProperty("订单开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @ApiModelProperty("订单开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;


    /**
     * 修改人
     */
    @ApiModelProperty("操作人")
    private String updateBy;

    /**
     * 订单号
     */
    @ApiModelProperty("订单号")
    private String platformOrder;

    /**
     * USDT地址
     */
    @ApiModelProperty("USDT地址")
    private String usdtAddr;

    /**
     * USDT数量
     */
    @ApiModelProperty("USDT数量")
    private BigDecimal usdtNum;

    /**
     * ARB数量
     */
    @ApiModelProperty("ARB数量")
    private BigDecimal arbNum;

    /**
     * 订单状态 默认值: 待支付
     */
    @ApiModelProperty("1待匹配2匹配超市3待支付4确认中5确认超时6申诉中7已完成8已取消9订单失效10买入失败11金额错误12未支付13支付超时14进行中15手动完成")
    private String status;
    @ApiModelProperty("商户号")
    private String merchantCode;

    @ApiModelProperty("语言")
    private String lang;

    /**
     * 该会员操作转款的自己的USDT地址
     */
    @ApiModelProperty("会员操作转款的自己的USDT地址")
    private String memberUsdtAddr;


}