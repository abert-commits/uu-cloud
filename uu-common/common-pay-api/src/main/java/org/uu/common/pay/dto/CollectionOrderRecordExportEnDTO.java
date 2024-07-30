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
public class CollectionOrderRecordExportEnDTO implements Serializable {


    @ApiModelProperty(value = "collection order id")
    private String collectionOrderId;

    /**
     * ：1：自动，2：人工
     */
    @ApiModelProperty(value = "collection type")
    private String collectionType;

    @ApiModelProperty(value = "collection amount")
    private BigDecimal collectionAmount;

    @ApiModelProperty(value = "fromAddress")
    private String fromAddress;

    @ApiModelProperty(value = "collectionBalance type")
    private String collectionBalanceType;

    @ApiModelProperty("create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 1:归集中, 2:归集成功, 3:归集失败
     */
    @ApiModelProperty(value = "status")
    private String status;

    @ApiModelProperty(value = "trade id")
    private String txid;
}