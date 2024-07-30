package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import javax.validation.constraints.NotNull;


/**
 * 会员信息表
 *
 * @author
 */
@Data
@ApiModel(description = "会员实名请求")
public class MemberInfoRealNameListReq extends PageRequest {


    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;



    /**
     * 证件号码
     */
    @ApiModelProperty("证件号码")
    private String idCardNumber;


    /**
     * 实名认证状态
     */
    @ApiModelProperty("实名认证状态: 1-已认证 2-未认证 3-手动认证")
    private String authenticationStatus;


}