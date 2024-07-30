package org.uu.common.pay.dto;

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
@ApiModel(description = "会员认证列表返回")
public class MemberRealNamelistPageDTO implements Serializable {

    @ApiModelProperty("主键")
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
     * 真实姓名
     */
    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("实名认证状态")
    private String authenticationStatus;


    @ApiModelProperty("证件号码")
    private String idCardNumber;

    @ApiModelProperty("实名认证时间")
    private LocalDateTime realNameVerificationTime;

    /**
     * 手动认证人员
     */
    @ApiModelProperty("手动认证人员")
    private String verificationBy;

    /**
     * 证件图片
     */
    @ApiModelProperty("证件图片")
    private String idCardImage;

    /**
     * 人脸照片
     */
    @ApiModelProperty("人脸照片")
    private String facePhoto;


}