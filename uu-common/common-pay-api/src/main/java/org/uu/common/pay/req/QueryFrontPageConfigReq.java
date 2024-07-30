package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.uu.common.core.enums.LanguageEnum;
import org.uu.common.core.page.PageRequestHome;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("添加首页弹窗内容请求实体")
@EqualsAndHashCode(callSuper = true)
public class QueryFrontPageConfigReq extends PageRequestHome implements Serializable {
    private static final long serialVersionUID = -4346793299570489242L;

    @ApiModelProperty("首页弹窗内容内容(模糊查询)")
    private String text;

    @ApiModelProperty("更新人ID(精确查询)")
    private String updateBy;

    @ApiModelProperty("创建人ID(精确查询)")
    private String createBy;

    /**
     * 语言类型  枚举详见{@link LanguageEnum}
     */
    @ApiModelProperty("语言类型(精确查询)")
    private Integer lang;

    @ApiModelProperty("创建时间 开始(RANGE-GE查询)")
    private LocalDateTime createTimeStart;

    @ApiModelProperty("创建时间 结束(RANG-LE查询)")
    private LocalDateTime createTimeEnd;
}
