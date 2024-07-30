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
@ApiModel(description = "会员信息表")
public class MemberLevelInfoDTO implements Serializable {

    /**
     * 等级
     */
    @ApiModelProperty("等级")
    private Integer level;

    /**
     * 人数
     */
    @ApiModelProperty("人数")
    private Long num;
}