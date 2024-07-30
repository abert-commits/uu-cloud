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
@ApiModel(description = "禁用买入-返回数据")
public class DisableBuyingVo implements Serializable {

    /**
     * 禁用买入的小时数
     */
    @ApiModelProperty("禁用买入的小时数")
    private Integer buyDisableHours;

    /**
     * 剩余时间(秒)
     */
    @ApiModelProperty("剩余时间(秒)")
    private Long remainingSeconds;

    /**
     * 失败次数
     */
    @ApiModelProperty("失败次数")
    private Integer numberFailures;

}