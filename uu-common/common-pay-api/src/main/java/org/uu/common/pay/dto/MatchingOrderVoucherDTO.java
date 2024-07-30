package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description = "匹配订单返回")
public class MatchingOrderVoucherDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("撮合Id")
    private long id;


    /**
     * 充值订单号
     */
    @ApiModelProperty("充值订单号")
    private String collectionPlatformOrder;

    /**
     * 订单提交金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal orderSubmitAmount;

    /**
     * 订单实际金额
     */
    @ApiModelProperty("实际到账金额")
    private BigDecimal orderActualAmount;


    /**
     * 创建时间
     */
    @ApiModelProperty("订单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ApiModelProperty("支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @ApiModelProperty("提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    /**
     * 修改人
     */
    private String updateBy;


    /**
     * 申诉原因
     */
    @ApiModelProperty("申诉原因")
    private String reason;

    /**
     * 图片信息
     */
    @ApiModelProperty("图片信息")
    private String picInfo;

    /**
     * 视频url
     */
    @ApiModelProperty("视频")
    private String videoUrl;


    /**
     * utr
     */
    @ApiModelProperty("utr")
    private String utr;


}