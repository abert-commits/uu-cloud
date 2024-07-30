package org.uu.common.core.page;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Admin
 */
@Data
@ApiModel(description = "分页查询返回数据")
public class PageReturn<T> {

    @ApiModelProperty(value = "总记录数")
    private Long total;

    @ApiModelProperty(value = "当前页码")
    private Long pageNo;

    @ApiModelProperty(value = "每页显示个数")
    private Long pageSize;

    @ApiModelProperty(value = "数据列表")
    private List<T> list;

    @ApiModelProperty(value = "扩展字段")
    private JSONObject extend;
}
