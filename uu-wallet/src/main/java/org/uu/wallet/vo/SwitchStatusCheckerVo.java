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
@ApiModel(description = "查询开关是否开启 接口返回数据")
public class SwitchStatusCheckerVo implements Serializable {

    @ApiModelProperty("取值说明: true表示开启，false表示关闭")
    private Boolean isActive = false;
}