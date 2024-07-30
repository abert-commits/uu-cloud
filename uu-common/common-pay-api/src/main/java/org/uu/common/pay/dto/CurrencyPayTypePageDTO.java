package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "获取币种对应的代收代付类型返回数据")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyPayTypePageDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("币种")
    private String currency;

    @ApiModelProperty("币种表的主键id")
    private Long currencyId;

    @ApiModelProperty("1:代收 2:代付")
    private Integer type;

    @ApiModelProperty("代收、代付的类型")
    private String payType;

    @ApiModelProperty("代收、代付的具体类型：例如upi、银行")
    private String payTypeName;

    @ApiModelProperty(value = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "操作人")
    private String updateBy;

}