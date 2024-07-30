package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("修改首页弹窗内容请求实体")
public class UpdateFrontPageConfigReq implements Serializable {
    private static final long serialVersionUID = -4346793299570489242L;

    @ApiModelProperty("首页弹窗内容ID")
    @NotNull(message = "Please specify the id")
    private Long id;

    @ApiModelProperty("首页弹窗内容内容")
//    @NotEmpty(message = "Please specify the content")
    private String content;
}
