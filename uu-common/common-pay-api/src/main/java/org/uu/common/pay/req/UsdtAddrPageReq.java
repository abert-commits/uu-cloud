package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "usdt地址管理请求参数")
public class UsdtAddrPageReq extends PageRequest {


    /**
     * 会员ID
     */
    @ApiModelProperty("会员ID")
    private String memberId;

    /**
     * 商户会员code
     */
    @ApiModelProperty("商户会员code")
    private String merchantId;

    /**
     * 商户会员name
     */
    @ApiModelProperty("商户会员name")
    private String merchantName;

    /**
     * usdt地址
     */
    @ApiModelProperty("usdt地址")
    private String usdtAddr;


    /**
     * 主网络
     */
    @ApiModelProperty("主网络")
    private String networkProtocol;

    /**
     * 排序字段
     */
    @ApiModelProperty("排序字段")
    private String column;

    /**
     * 排序方式  asc----true为升序，false为倒序
     */
    @ApiModelProperty("排序字段")
    private boolean asc;

    /**
     * 订单开始时间
     */
    @ApiModelProperty("订单开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeStart;

    @ApiModelProperty("订单开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeEnd;

    /**
     * 1:平台会员, 2:商户会员
     */
    @ApiModelProperty("排序字段")
    private Integer type;

    @ApiModelProperty("语言")
    private String lang;


}