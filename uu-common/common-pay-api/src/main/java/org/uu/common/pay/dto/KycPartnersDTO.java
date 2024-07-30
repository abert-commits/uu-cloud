package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "获取kyc列表返回数据")
public class KycPartnersDTO implements Serializable {

    @ApiModelProperty("id")
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
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String mobileNumber;

    /**
     * 银行名称
     */
    @ApiModelProperty("银行名称")
    private String bankName;

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
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;



    /**
     * 卖出状态: 0: 关闭, 1: 开启
     */
    @ApiModelProperty("卖出状态: 0: 关闭, 1: 开启")
    private Integer sellStatus;


    /**
     * 图标地址
     */
    @ApiModelProperty("图标地址")
    private String iconUrl;

    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人")
    private String bankCardOwner;

    /**
     * ifsc
     */
    @ApiModelProperty("ifsc")
    private String bankCardIfsc;

    /**
     * 收款方式 1-银行卡 3-upi
     */
    @ApiModelProperty("收款方式 1-银行卡 3-upi")
    private Integer collectionType;

    /**
     * 总买入次数
     */
    @ApiModelProperty("总买入次数")
    private Integer totalBuySuccessCount;

    /**
     * 总买入金额
     */
    @ApiModelProperty("总买入金额")
    private BigDecimal totalBuySuccessAmount;

    /**
     * 总卖出数量
     */
    @ApiModelProperty("总卖出数量")
    private Integer totalSellSuccessCount;

    /**
     * 总卖出金额
     */
    @ApiModelProperty("总卖出金额")
    private BigDecimal totalSellSuccessAmount;


    /**
     * 唤醒类型
     */
    @ApiModelProperty("唤醒类型")
    private String linkType;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("更新人")
    private String updateBy;

    @ApiModelProperty("在线状态")
    private String onlineStatus;

    @ApiModelProperty("启用状态 0 未启用 1 启用")
    private Integer status;
}