package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

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
public class AppealOrderDTO implements Serializable {

    @ApiModelProperty("主键")
    private long id;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String mid;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String mAccount;

    /**
     * 所属商户code
     */
    @ApiModelProperty("所属商户code")
    private String belongMerchantCode;

    /**
     * 提现订单号
     */
    @ApiModelProperty("提现订单号")
    private String withdrawOrderNo;

    /**
     * 充值订单号
     */
    @ApiModelProperty("充值订单号")
    private String rechargeOrderNo;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal orderAmount;

    /**
     * 申诉类型: 1-提现申诉 2-充值申诉
     */
    @ApiModelProperty("申诉类型 1-提现申诉 2-充值申诉")
    private Integer appealType;

    /**
     * 申诉状态: 1-代处理 2-申诉成功 3-申诉失败
     */
    @ApiModelProperty("申诉状态: 1-代处理 2-申诉成功 3-申诉失败")
    private Integer appealStatus;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 申诉原因
     */
    @ApiModelProperty("申诉原因")
    private String reason;

    /**
     * 图片信息
     */
    @ApiModelProperty("图片信息")
    private String picInfo;

    /**
     * 视频url
     */
    @ApiModelProperty("视频url")
    private String videoUrl;
    @ApiModelProperty("实际金额")
    private BigDecimal actualAmount;

    /**
     * 撮合ID
     */
    @ApiModelProperty("撮合ID")
    private String matchId;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 显示申诉类型 1: 未到账  2: 金额错误
     */
    @ApiModelProperty("显示申诉类型 1: 未到账  2: 金额错误")
    private Integer displayAppealType;


    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty("支付方式")
    private String payType;



}