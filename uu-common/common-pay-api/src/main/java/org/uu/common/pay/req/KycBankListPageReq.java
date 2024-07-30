package org.uu.common.pay.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 添加 KYC Partner 请求参数
 *
 * @author Simon
 * @date 2023/12/26
 */
@Data
@ApiModel(description = "KYC Partner 请求参数")
public class KycBankListPageReq extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L; // 显式序列化版本ID

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
     * 状态
     */
    @ApiModelProperty("状态")
    private Integer status;


    /**
     * 连接方式, 1: 唤醒APP, 2: 跳转H5
     */
    @ApiModelProperty("连接方式, 1: 唤醒APP, 2: 跳转H5")
    private String linkType;

}
