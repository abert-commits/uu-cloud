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
@ApiModel(description = "校验UPI_ID是否重复-返回数据")
public class CheckUpiIdDuplicateVo implements Serializable {

    // 使用 @ApiModelProperty 注解描述字段信息
    @ApiModelProperty(value = "标识UPI_ID是否重复 取值说明: true: UPI_ID已存在，重复；false: UPI_ID不存在，不重复。")
    private Boolean isUpiIdDuplicate;
}