package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配池
 *
 * @author
 */
@Data
@ApiModel(description = "会员黑名单返回")
public class MemberBlackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;

    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    /**
     * 分组 默认值: 1(默认分组)
     */
    @ApiModelProperty("会员分组")
    private Long memberGroup = 1L;


    /**
     * 会员类型
     */
    @ApiModelProperty("会员类型")
    private String memberType;

    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 在线状态
     */
    @ApiModelProperty("在线状态")
    private Integer onlineStatus;

    /**
     * 买入状态
     */
    @ApiModelProperty("买入状态")
    private Integer buyStatus;

    /**
     * 卖出状态
     */
    @ApiModelProperty("卖出状态")
    private Integer sellStatus;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 真实姓名
     */
    @ApiModelProperty("真实姓名")
    private String realName;

    /**
     * 操作人
     */
    @ApiModelProperty("操作人")
    private String operator;

    /**
     * 商户会员ID
     */
    @ApiModelProperty("商户会员ID")
    private String merchantMemberId;

    /**
     * 操作时间
     */
    @ApiModelProperty("操作时间")
    private LocalDateTime opTime;



}