package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author
 */
@Data
@ApiModel(description ="申诉列表请求")
public class AppealOrderPageListReq extends PageRequest {



    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String mid;



    /**
     * 提现订单号
     */
    @ApiModelProperty("提现订单号")
    private String withdrawOrderNo;

    /**
     * 充值订单号
     */
    @ApiModelProperty("充值订单号")
    private String rechargeOrderNo;


    /**
     * 创建时间
     */
    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;
    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;


    /**
     * 申诉状态
     */
    @ApiModelProperty("申诉状态")
    private String status;

    @ApiModelProperty("语言")
    private String lang ;


    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty(value = "支付方式")
    private String payType;




}