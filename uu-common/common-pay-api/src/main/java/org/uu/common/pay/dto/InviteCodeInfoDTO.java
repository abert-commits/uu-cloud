package org.uu.common.pay.dto;

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
@ApiModel("邀请码信息DTO实体类")
public class InviteCodeInfoDTO implements Serializable {

    private static final long serialVersionUID = -2930667716006956259L;

    @ApiModelProperty("邀请码ID")
    private Long id;

    @ApiModelProperty("会员ID")
    private Long memberId;

    @ApiModelProperty("邀请码")
    private String inviteCode;

    @ApiModelProperty("是否默认邀请码 0-是 -1-否")
    private Integer defaultInviteCode;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
