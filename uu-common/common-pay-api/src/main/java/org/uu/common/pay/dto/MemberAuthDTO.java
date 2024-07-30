package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@ApiModel(description = "会员权限")
public class MemberAuthDTO {

    /**
     * 会员ID
     */
    @ApiModelProperty("会员ID")
    private Long userId;

    /**
     * 会员ID
     */
    @ApiModelProperty("会员ID")
    private String memberId;


    /**
     * 首次登录ip
     */
    @ApiModelProperty("首次登录ip")
    private String firstLoginIp;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String username;

    /**
     * 登录密码
     */
    @ApiModelProperty("登录密码")
    private String password;

    /**
     * 登录密码
     */
    @ApiModelProperty("会员类型")
    private String memberType;

    /**
     * 状态：1-启用；0-禁用
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 层级
     */
    @ApiModelProperty("层级")
    private Integer antLevel;

    /**
     * 用户角色编码集合 ["ROOT","ADMIN"]
     */
    @ApiModelProperty("角色")
    private List<String> roles;

    @ApiModelProperty("买入奖励比例")
    private BigDecimal buyReWardRatio;
}
