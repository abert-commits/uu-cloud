package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "充值列表返回")
public class CallBackDetailDTO implements Serializable {


    /**
     * 回调状态
     */
    @ApiModelProperty("回调状态")
    private String tradeCallbackStatus;

    /**
     * 回调请求地址
     */
    @ApiModelProperty("回调请求地址")
    private String tradeNotifyUrl;

    /**
     * 回调请求参数
     */
    @ApiModelProperty("回调请求参数")
    private String tradeCallbackRequest;

    /**
     * 回调返回参数
     */
    @ApiModelProperty("回调返回参数")
    private String tradeCallbackResponse;

}