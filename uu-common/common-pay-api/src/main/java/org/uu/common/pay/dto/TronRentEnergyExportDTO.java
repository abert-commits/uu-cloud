package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
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
@ApiModel(description = "能量租用记录导出返回")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronRentEnergyExportDTO implements Serializable {

    @ApiModelProperty("id")
    private Long id;


    @ApiModelProperty("钱包地址")
    private String address;

   
    @ApiModelProperty("租用数量")
    private Long amount;


    @ApiModelProperty("租用时长")
    private String rentTime;


    @ApiModelProperty("能量提供商")
    private String energyProvider;


    @ApiModelProperty("到账时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;


    @ApiModelProperty("响应信息")
    private String responseInfo;


    @ApiModelProperty("状态")
    private String status;


    @ApiModelProperty("操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}