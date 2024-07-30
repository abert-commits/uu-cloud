package org.uu.wallet.vo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "测试图片识别返回数据")
public class TestImageRecognitionVo implements Serializable {


    /**
     * 图片识别结果
     */
    @ApiModelProperty(value = "图片识别结果 Ads: 转账记录")
    private String riskLabel1;


    /**
     * 图片内容
     */
    @ApiModelProperty(value = "图片文字内容")
    private String ocrText;


    /**
     * requestId
     */
    @ApiModelProperty(value = "requestId")
    private String requestId;


    /**
     * riskLevel
     */
    @ApiModelProperty(value = "riskLevel")
    private String riskLevel;


    /**
     * AI接口返回数据
     */
    @ApiModelProperty(value = "AI接口返回数据")
    private JSONObject res;
}