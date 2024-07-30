package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "归集订单记录返回")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionOrderRecordDTO implements Serializable {

    private Long id;


    @ApiModelProperty(value = "归集订单号")
    private String collectionOrderId;


    @ApiModelProperty(value = "归集类型：1：自动，2：人工")
    private Integer collectionType;


    @ApiModelProperty(value = "归集数量")
    private BigDecimal collectionAmount;


    @ApiModelProperty(value = "源地址")
    private String fromAddress;


    @ApiModelProperty(value = "归集金额类型如：usdt,trx")
    private String collectionBalanceType;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "归集状态, 1:归集中, 2:归集成功, 3:归集失败")
    private Integer status;

    @ApiModelProperty(value = "交易id")
    private String txid;


}