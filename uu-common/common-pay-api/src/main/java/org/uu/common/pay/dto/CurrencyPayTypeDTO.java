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
@ApiModel(description = "获取币种对应的代收代付类型返回数据")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyPayTypeDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("币种表的主键id")
    private Long currencyId;


    @ApiModelProperty("代收、代付的具体类型：例如upi、银行")
    private String payType;

    @ApiModelProperty("代收、代付的具体类型：例如upi、银行")
    private String payTypeName;

}