package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 */
@Data
@ApiModel(description = "获取kyc列表返回数据")
public class KycPartnersVo implements Serializable {

    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * 银行名称
     */
    @ApiModelProperty("银行名称")
    private String bankName;

    /**
     * 银行编码
     */
    @ApiModelProperty("银行编码")
    private String bankCode;

    /**
     * 账户
     */
    @ApiModelProperty("账户")
    private String account;

    /**
     * 账户姓名
     */
    @ApiModelProperty("账户姓名")
    private String name;

    /**
     * upi_id
     */
    @ApiModelProperty("upi_id")
    private String upiId;

    /**
     * 连接状态: 0: 未连接, 1: 已连接
     */
    @ApiModelProperty("连接状态: 0: 未连接, 1: 已连接")
    private Integer linkStatus;

    /**
     * 备注
     */
    @ApiModelProperty("remark")
    private String remark;


    /**
     * 卖出状态: 0: 关闭, 1: 开启
     */
    @ApiModelProperty("卖出状态: 0: 关闭, 1: 开启")
    private Integer sellStatus;


    /**
     * 连接地址
     */
    @ApiModelProperty("连接地址")
    private String linkUrl;


    /**
     * 连接方式, 1: 唤醒APP, 2: 跳转H5
     */
    @ApiModelProperty("连接方式, 1: 唤醒APP, 2: 跳转H5")
    private String linkType;


    /**
     * 图标地址
     */
    @ApiModelProperty("图标地址")
    private String iconUrl;


    @ApiModelProperty("状态, 0: 未启用, 1: 已启用")
    private Integer status;
}