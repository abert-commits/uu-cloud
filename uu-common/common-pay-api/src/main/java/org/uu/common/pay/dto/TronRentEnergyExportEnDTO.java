package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronRentEnergyExportEnDTO implements Serializable {

    @ApiModelProperty("id")
    private Long id;


    @ApiModelProperty("wallet address")
    private String address;


    @ApiModelProperty("rent amount")
    private Long amount;


    @ApiModelProperty("rent time")
    private String rentTime;


    @ApiModelProperty("energy provider")
    private String energyProvider;


    @ApiModelProperty("receive time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;


    @ApiModelProperty("response info")
    private String responseInfo;


    @ApiModelProperty("status")
    private String status;


    @ApiModelProperty("create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}