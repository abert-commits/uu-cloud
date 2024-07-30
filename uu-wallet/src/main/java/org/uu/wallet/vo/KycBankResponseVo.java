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
@ApiModel(description = "获取银行列表返回数据")
public class KycBankResponseVo implements Serializable {

    /**
     * 通信状态
     */
    @ApiModelProperty("通信状态 true: 通信成功, false: 通信失败")
    private Boolean status = false;

    /**
     * 提示信息
     */
    @ApiModelProperty("提示信息")
    private String msg;


}