package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
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
@ApiModel(description = "能量租用记录返回")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronRentEnergyDTO implements Serializable {

    private Long id;


    /**
     * 钱包地址
     */
    private String address;

    /**
     * 租用数量
     */
    private Long amount;

    /**
     * 租用时长
     */
    private String rentTime;

    /**
     * 能量提供商
     */
    private String energyProvider;

    /**
     * 请求信息
     */
    private String requestInfo;

    /**
     * 响应信息
     */
    private String responseInfo;

    /**
     * 响应编码
     */
    private String resultCode;

    /**
     * 响应信息
     */
    private String resultMessage;

    /**
     * 到账时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;
}