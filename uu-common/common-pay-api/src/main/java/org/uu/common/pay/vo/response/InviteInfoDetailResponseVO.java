package org.uu.common.pay.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("邀请链接详情")
public class InviteInfoDetailResponseVO implements Serializable {
    private static final long serialVersionUID = 4949367374385487209L;

    @ApiModelProperty("邀请链接ID")
    private Long id;

    @ApiModelProperty("链接名称")
    private String title;

    @ApiModelProperty("蚂蚁ID")
    private Long antId;

    @ApiModelProperty("邀请码")
    private String inviteCode;

    @ApiModelProperty("是否默认邀请链接  0-是 -1-否")
    private Integer defaultLink;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("下级注册数量")
    private Integer nextOneRegisterCount;

    @ApiModelProperty("下下级注册数量")
    private Integer nextTwoRegisterCount;

    @ApiModelProperty("返佣金额")
    private BigDecimal commissionAmount;
}
