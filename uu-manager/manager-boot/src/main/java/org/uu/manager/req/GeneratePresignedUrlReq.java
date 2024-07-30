package org.uu.manager.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
@ApiModel(description = "生成上传凭证-请求参数")
public class GeneratePresignedUrlReq {

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称")
    @Pattern(regexp = "^[^<>\\\"']+$", message = "File name format is incorrect")
    private String objectName;

    /**
     * contentType
     */
    @ApiModelProperty(value = "文件的实际MIME类型")
    private String contentType;
}
