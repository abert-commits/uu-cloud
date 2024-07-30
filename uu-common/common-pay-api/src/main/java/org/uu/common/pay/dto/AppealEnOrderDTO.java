package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author
 */
@Data
@ApiModel(description = "申诉")
public class AppealEnOrderDTO implements Serializable {

    @ApiModelProperty("ID")
    private long id;

    /**
     * 会员id
     */
    @ApiModelProperty("Member ID")
    private String mid;

    /**
     * 会员账号
     */
    @ApiModelProperty("Member Account")
    private String mAccount;

    /**
     * 所属商户code
     */
    @ApiModelProperty("Belong Merchant Code")
    private String belongMerchantCode;

    /**
     * 提现订单号
     */
    @ApiModelProperty("Withdraw Order Number")
    private String withdrawOrderNo;

    /**
     * 充值订单号
     */
    @ApiModelProperty("Recharge Order Number")
    private String rechargeOrderNo;

    /**
     * 订单金额
     */
    @ApiModelProperty("Order Amount")
    private BigDecimal orderAmount;

    /**
     * 申诉类型: 1-提现申诉 2-充值申诉
     */
    @ApiModelProperty("Appeal Type")
    private Integer appealType;

    /**
     * 申诉状态: 1-代处理 2-申诉成功 3-申诉失败
     */
    @ApiModelProperty("Appeal Status")
    private Integer appealStatus;

    /**
     * 创建时间
     */
    @ApiModelProperty("Create Time")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty("Update By")
    private String updateBy;

    /**
     * 修改时间
     */
    @ApiModelProperty("Update Time")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @ApiModelProperty("Create By")
    private String createBy;

    /**
     * 申诉原因
     */
    @ApiModelProperty("reason")
    private String reason;

    /**
     * 图片信息
     */
    @ApiModelProperty("picInfo")
    private String picInfo;

    /**
     * 视频url
     */
    @ApiModelProperty("Video Url")
    private String videoUrl;
    @ApiModelProperty("Actual Amount")
    private BigDecimal actualAmount;

    /**
     * 撮合ID
     */
    @ApiModelProperty("Match ID")
    private String matchId;

    /**
     * 商户名称
     */
    @ApiModelProperty("Merchant Name")
    private String merchantName;

    /**
     * 收款类型 1-UPI 2-银行卡
     */
    @ApiModelProperty(value = "collection Type")
    private String payType;
}