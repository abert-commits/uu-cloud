package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "会员实名请求")
public class MemberInfoRealNameReq {


    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    @NotNull
    private String memberId;



    /**
     * 真实姓名
     */
    @NotNull(message = "真实姓名不能为空")
    @Pattern(regexp = "^[a-zA-Z]+(?:[\\s.][a-zA-Z]+)*$", message = "真实姓名格式不正确")
    @ApiModelProperty(value = "真实姓名 (格式为印度人真实姓名格式 示例: Priya)")
    private String realName;

    /**
     * 实名认证状态
     */
    @ApiModelProperty("实名认证状态: 1-已认证 2-未认证")
    @NotNull
    private String authenticationStatus;


    /**
     * 证件号
     */
    @NotNull(message = "证件号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{1,30}$", message = "证件号格式不正确")
    @ApiModelProperty(value = "证件号 (格式为印度人证件号格式 示例: 123456789012)")
    private String idCardNumber;

    /**
     * 证件照片
     */
    @NotNull(message = "证件照片不能为空")
    @ApiModelProperty(value = "证件照片地址")
    private String idCardImage;

    /**
     * 证件照片
     */
    @NotNull(message = "人脸照片不能为空")
    @ApiModelProperty(value = "人脸照片")
    private String facePhoto;


}