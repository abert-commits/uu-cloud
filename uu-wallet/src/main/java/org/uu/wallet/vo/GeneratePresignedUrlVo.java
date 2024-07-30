package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "生成上传凭证-返回数据")
public class GeneratePresignedUrlVo implements Serializable {

    @ApiModelProperty(value = "凭证URL")
    private String signedUrl;

    @ApiModelProperty(value = "baseUrl")
    private String baseUrl;
}