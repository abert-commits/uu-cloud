package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
* 会员分组
*
* @author 
*/
    @Data
    @ApiModel(description = "会员分组")
    @EqualsAndHashCode(callSuper = false)
    public class MemberGroupAddReq implements Serializable {


            /**
            * 分组名称
            */
            @ApiModelProperty("分组名称")
    private String name;

            /**
            * 金额
            */
            @ApiModelProperty("卖出次数")
    private Integer sellCount;

            /**
            * 金额
            */
            @ApiModelProperty("买入金额")
    private BigDecimal buyAmount;

    /**
     * 卖出金额
     */
    @ApiModelProperty("卖出金额")
    private BigDecimal sellAmount;



    /**
            * 买入次数
            */
            @ApiModelProperty("买入次数")
    private Integer buyCount;


            /**
            * 授权列表
            */
            @ApiModelProperty("授权列表")
    private List<String> authList;







}