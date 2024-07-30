package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author
 * @since 2024-04-16
 */
@Data
@ApiModel(description = "KYC Partner 请求参数")
public class KycBankReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;

    /**
     * 银行编码
     */
    @ApiModelProperty(value = "银行编码")
    private String bankCode;

    /**
     * 服务编码
     */
    @ApiModelProperty(value = "服务编码")
    private String serviceCode;

    /**
     * 删除表示 0:未删除 1:已删除
     */
    @ApiModelProperty(value = "删除表示 0:未删除 1:已删除")
    private Integer deleted;

    /**
     * 图标地址
     */
    @ApiModelProperty(value = "图标地址")
    private String iconUrl;

    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer status;

    /**
     * 说明
     */
    @ApiModelProperty(value = "说明")
    private String remark;

    /**
     * 连接地址
     */
    @ApiModelProperty(value = "连接地址")
    private String linkUrl;

    /**
     * 连接方式, 1: 唤醒APP, 2: 跳转H5
     */
    @ApiModelProperty(value = "连接方式, 1: 唤醒APP, 2: 跳转H5")
    private String linkType;

    /**
     * 获取交易记录api地址
     */
    @ApiModelProperty(value = "获取交易记录api地址")
    private String apiUrl;

    /**
     * 类型 1-银行卡 3-upi
     */
    @ApiModelProperty(value = "1-银行卡 3-upi")
    private Integer type;
}
