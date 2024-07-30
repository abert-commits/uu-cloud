package org.uu.common.pay.req;


import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 信用分配置表
 *
 *
 */
@Data
@ApiModel(description = "配置信息")
public class CreditScoreConfigUpdateReq{
    private Integer eventId;
    private String score;

}