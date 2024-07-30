package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description ="撮合列表")
public class RelationshipOrderReq extends PageRequest {



    @ApiModelProperty("关联IP")
    private String relationsIp;

    @ApiModelProperty("会员ID/商户名称/商户会员ID")
    private String idSet;

    @ApiModelProperty("买入订单号/卖出订单号/匹配订单号")
    private String orderSet;

    @ApiModelProperty("订单状态")
    private String orderStatus;


}