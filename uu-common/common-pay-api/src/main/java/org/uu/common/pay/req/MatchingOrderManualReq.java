package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description ="撮合列表")
public class MatchingOrderManualReq implements Serializable {


    @ApiModelProperty("撮合Id")
    private Long id;

    @ApiModelProperty("人工审核 1-通过 2-不通过")
    private Integer isPass;


    @ApiModelProperty("不通过原因 1-买方支付凭证图片造假 2-其他")
    private Integer refuseReason;

    @ApiModelProperty("不通过原因备注")
    private String reasonRemark;

}