package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;


/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "会员列表请求")
public class MemberInfoListPageReq extends PageRequest {

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


    /**
     * 会员类型
     */
    @ApiModelProperty("会员类型 1内部会员2商户会员3钱包会员")
    private String memberType;


    /**
     * 会员分组
     */
    @ApiModelProperty("会员分组id")
    private Integer memberGroupId;


    /**
     * 买入金额
     */
    @ApiModelProperty("买入金额开始")
    private Integer totalBuyAmountStart;
    @ApiModelProperty("买入金额结束")
    private Integer totalBuyAmountEnd;


    /**
     * 卖出金额
     */
    @ApiModelProperty("卖出金额开始")
    private Integer totalSellAmountStart;

    @ApiModelProperty("卖出金额结束")
    private Integer totalSellAmountEnd;


    /**
     * 钱包地址
     */
    @ApiModelProperty("钱包地址")
    private String walletAddress;


    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户code")
    private String merchantCode;

    /**
     * 商户会员ID
     */
    @ApiModelProperty(value = "商户会员ID")
    private String externalMemberId;


    /**
     * 会员状态: 0-禁用 1-启用
     */
    @ApiModelProperty(value = "会员状态: 0-禁用 1-启用")
    private String status;

    /**
     * 买入状态: 0-禁用 1-启用
     */
    @ApiModelProperty(value = "买入状态: 0-禁用 1-启用")
    private String buyStatus;

    /**
     * 卖出状态: 0-禁用 1-启用
     */
    @ApiModelProperty(value = "卖出状态: 0-禁用 1-启用")
    private String sellStatus;

    /**
     * 在线状态：0-离线 1-在线
     */
    @ApiModelProperty(value = "在线状态: 0-离线 1-在线")
    private String onlineStatus;

    @ApiModelProperty(value = "关联IP")
    private String relationsIp;

    @ApiModelProperty(value = "用户等级")
    private String level;

    @ApiModelProperty(value = "最小信用分")
    private String minCreditScore;

    @ApiModelProperty(value = "最大信用分")
    private String maxCreditScore;

    @TableField(exist = false)
    @ApiModelProperty(value = "交易成功金额")
    private BigDecimal transactionSuccessAmount;


    @TableField(exist = false)
    @ApiModelProperty(value = "交易成功次数")
    private Integer transactionSuccessCount;
}