package org.uu.common.pay.dto;

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
public class MatchingOrderVoucherUrlDTO implements Serializable {


    @ApiModelProperty("撮合Id")
    private long id;


    @ApiModelProperty("凭证")
    private String voucher;




}