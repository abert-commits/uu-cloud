package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 * 委托卖单状态返回Vo
 */
@Data
public class DelegationStatusVo implements Serializable {

    /**
     * 委托状态: 1、委托成功 0、委托失败
     */
    @ApiModelProperty(value = "委托状态")
    private String delegationStatus;
}