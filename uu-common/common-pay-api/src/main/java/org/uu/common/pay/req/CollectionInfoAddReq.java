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
public class CollectionInfoAddReq extends PageRequest {
        private long id;

    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI_ID")
    @NotBlank(message = "UPI_ID不能为空")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "UPI_Name")
    @NotBlank(message = "UPI_Name不能为空")
    private String upiName;

    /**
     * 手机号码
     */
    @NotNull(message = "手机号码不能为空")
    @Pattern(regexp = "^\\d{8,13}$", message = "手机号码格式不正确")
    @ApiModelProperty(value = "手机号码 格式为印度手机号码格式 示例: 7528988319")
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
}
