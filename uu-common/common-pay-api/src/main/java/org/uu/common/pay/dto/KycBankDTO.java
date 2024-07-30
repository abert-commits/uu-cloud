package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author
 * @since 2024-04-16
 */
@Data
@ApiModel(description = "获取kyc银行列表返回数据")
public class KycBankDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 服务编码
     */
    @ApiModelProperty("服务编码")
    private String serviceCode;

    /**
     * 图标地址
     */
    @ApiModelProperty("图标地址")
    private String iconUrl;

    /**
     * 状态
     */
    @ApiModelProperty("状态  0: 关闭, 1: 启用")
    private Integer status;

    /**
     * 说明
     */
    @ApiModelProperty("说明")
    private String remark;

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
     * 获取交易记录api地址
     */
    @ApiModelProperty("获取交易记录api地址")
    private String apiUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String createBy;

    private String updateBy;
}
