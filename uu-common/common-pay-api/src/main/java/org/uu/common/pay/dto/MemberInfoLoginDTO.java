package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "会员列表返回")
public class MemberInfoLoginDTO implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("登录时间")
    private LocalDateTime createTime;

    @ApiModelProperty("登录ip")
    private String  ip;

    @ApiModelProperty("登录次数")
    private int  count;


}