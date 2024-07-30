package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

/**
 * @author Admin
 */
@Data
@ApiModel(description = "添加收款信息请求参数")
public class CollectionInfoReq extends PageRequest {
        private long id;

    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI_ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "UPI_Name")
    private String upiName;

    /**
     * 手机号码
     */
//    @Pattern(regexp = "^\\d{8,13}$", message = "手机号码格式不正确")
//    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
    private String mobileNumber;

    /**
     * 每日收款限额
     */
    @ApiModelProperty(value = "每日收款限额")
    private BigDecimal dailyLimitAmount;

    /**
     * 每日收款次数
     */
    @ApiModelProperty(value = "每日收款次数")
    private Integer dailyLimitNumber;


    /**
     * 会员账号
     */
    @ApiModelProperty(value = "会员账号")
    private String memberAccount;

    @ApiModelProperty(value = "会员ID")
    @NotBlank(message = "会员id不能为空")
    private String memberId;

    /**
     * 收款类型 1-UPI 2-银行卡
     */
    @ApiModelProperty("收款类型 1-UPI 2-银行卡")
    private Integer type;


    /**
     * 银行编码
     */
    @ApiModelProperty("银行编码")
    private String bankCode;

    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;


    /**
     * ifsc_code
     */
    @Pattern(regexp = "^[A-Za-z]{4}0[A-Za-z0-9]{6}$", message = "ifscCode格式不正确")
    @ApiModelProperty("ifsc_code")
    private String ifscCode;


    /**
     * email
     */
    @ApiModelProperty("邮箱")
    private String email;

    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人姓名")
    private String bankCardOwner;
}
