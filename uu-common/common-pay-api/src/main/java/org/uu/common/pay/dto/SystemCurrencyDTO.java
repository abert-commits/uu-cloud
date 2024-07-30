package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取币种列表返回数据")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemCurrencyDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * 币种符号
     */
    @ApiModelProperty("币种符号")
    private String currencyCode;

    /**
     * 币种名称
     */
    @ApiModelProperty("币种名称")
    private String currencyName;

    /**
     * 时区
     */
    private String timeZone;

}