package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员分组
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(description = "会员分组")
public class MemberGroupListPageDTO extends PageRequest {
    @ApiModelProperty("主键")
    private long id;

    /**
     * 分组名称
     */
    @ApiModelProperty("分组名称")
    private String name;

    /**
     * 金额
     */
    @ApiModelProperty("卖出数量")
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

    /**
     * 会员数量
     */
    @ApiModelProperty("会员数量")
    private Integer memberCount;

    /**
     * 授权列表
     */
    @ApiModelProperty("授权列表")
    private String authList;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String updateBy;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("修改时间")
    private String updateTime;


    @ApiModelProperty("会员账号")
    private String memberAccount;


}