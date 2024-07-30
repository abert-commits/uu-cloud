package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;

/**
 * 添加 KYC Partner 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "KYC Partner 请求参数")
public class KycPartnerListPageReq extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L; // 显式序列化版本ID
    @ApiModelProperty("id")
    private Long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;

    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String mobileNumber;

    /**
     * 银行编码
     */
    @ApiModelProperty("银行编码")
    private String bankCode;

    /**
     * upi_id
     */
    @ApiModelProperty("upiId")
    private String upiId;

    /**
     * 账户
     */
    @ApiModelProperty("账户")
    private String account;

    /**
     * 连接状态: 0: 未连接, 1: 已连接
     */
    @ApiModelProperty("连接状态: 0: 未连接, 1: 已连接")
    private Integer linkStatus;


    /**
     * 卖出状态: 0: 关闭, 1: 开启
     */
    @ApiModelProperty("卖出状态: 0: 关闭, 1: 开启")
    private Integer sellStatus;

    /**
     * 收款方式 1-银行卡 3-upi
     */
    @ApiModelProperty("收款方式 1-银行卡 3-upi")
    private Integer collectionType;


    /**
     * 在线状态: 0: 未在线, 1: 在线
     */
    @ApiModelProperty("在线状态: 0: 未在线, 1: 在线")
    private String onlineStatus;


}
