package org.uu.common.pay.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员黑名单请求参数
 *
 * @author
 */
@Data
@ApiModel(description = "会员黑名单请求参数")
public class MemberBlackReq extends PageRequest {

    private Long id;
    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;


    /**
     * 商户会员ID
     */
    @ApiModelProperty("商户会员ID")
    private String merchantMemberId;


    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;


    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;

    @ApiModelProperty("关联IP")
    private String relationsIp;

}