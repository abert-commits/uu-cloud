package org.uu.common.core.page;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 前台
 * @author Admin
 * @date 2023/12/11
 */
@Data
@ApiModel(description = "分页查询请求参数")
public class PageRequestHome {

    //设置默认查询页码
    @ApiModelProperty(value = "查询页码, 默认查询第一页")
    @Min(value = 0, message = "页码格式不正确")
    private Long pageNo = Long.valueOf(1);

    //设置默认每页显示记录条数
    @ApiModelProperty(value = "查询记录条数, 默认查询10条记录")
    @Min(value = 0, message = "记录条数格式不正确")
    private Long pageSize = Long.valueOf(10);

}
