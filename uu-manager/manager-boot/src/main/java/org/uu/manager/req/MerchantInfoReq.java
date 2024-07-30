package org.uu.manager.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "商户信息")
public class MerchantInfoReq extends PageRequest {
    private Long id;

    /**
     * 商户名
     */
    @ApiModelProperty(value = "商户名")
    private String username;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;

    /**
     * 登录账号
     */
    @ApiModelProperty(value = "登录账号")
    private String loginAccount;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String account;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private String status;

    /**
     * 商户编码
     */
    @ApiModelProperty(value = "商户编码")
    private String code;

    /**
     * 商户公钥
     */
    @ApiModelProperty(value = "商户公钥")
    private String publicKey;

    /**
     * 商户私钥
     */
    @ApiModelProperty(value = "商户私钥")
    private String privateKey;

    /**
     * 余额
     */
    @ApiModelProperty(value = "余额")
    private BigDecimal balance;

    /**
     * 国家
     */
    @ApiModelProperty(value = "国家")
    private String country;

    /**
     * 冻结金额
     */
    @ApiModelProperty(value = "冻结金额")
    private BigDecimal frozenAmount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    /**
     * 修改人
     */
    @ApiModelProperty(value = "修改人")
    private String updateBy;

    @Size(min = 1, message = "roleIds 不能为空")
    @ApiModelProperty(value = "roleIds")
    private List<Long> roleIds;


    /**
     * md5Key
     */
    @ApiModelProperty(value = "md5Key")
    private String md5Key;
}
