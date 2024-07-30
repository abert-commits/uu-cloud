package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.uu.common.core.enums.LanguageEnum;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("添加首页弹窗内容请求实体")
public class InsertFrontPageConfigReq implements Serializable {
    private static final long serialVersionUID = -4346793299570489242L;

    @ApiModelProperty("首页弹窗内容内容")
    @NotEmpty(message = "Please specify the text")
    private String text;

    @ApiModelProperty("操作人ID")
    @NotEmpty(message = "Please specify the id of operator")
    private String operator;

    /**
     * 语言类型  枚举详见{@link LanguageEnum}
     */
    @NotNull(message = "Please specify the text")
    @ApiModelProperty("语言类型")
    @Min(value = 1, message = "The minimum value of lang is 1")
    private Integer lang;
}
