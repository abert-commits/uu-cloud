package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* 会员分组
*
* @author 
*/
    @Data
    @ApiModel(description = "会员分组")
    @EqualsAndHashCode(callSuper = false)
    public class MemberGroupReq implements Serializable {
        @ApiModelProperty("主键")
        private long id;

            /**
            * 分组名称
            */
            @ApiModelProperty("名称")
    private String name;

            /**
            * 金额
            */
            @ApiModelProperty("金额")
    private Integer sellCount;

            /**
            * 金额
            */
            @ApiModelProperty("买入金额")
    private BigDecimal buyAmount;

            /**
            * 买入次数
            */
            @ApiModelProperty("买入次数")
    private Integer buyCount;

            /**
            * 卖出金额
            */
            @ApiModelProperty("卖出金额")
    private BigDecimal sellAmount;

//            /**
//            * 会员数量
//            */
//            @ApiModelProperty("卖出金额")
//    private Integer memberCount;

            /**
            * 授权列表
            */
    private List<String> authList;







}