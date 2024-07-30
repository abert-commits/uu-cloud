package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 会员手动操作记录
 * </p>
 *
 * @author 
 * @since 2024-02-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MemberLevelConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    /**
     * 等级
     */
    @ApiModelProperty("等级")
    private Integer level;

    /**
     * 信誉分
     */
    @ApiModelProperty("信誉分")
    private BigDecimal creditScore;

    /**
     * 卖出次数
     */
    @ApiModelProperty("卖出次数")
    private Integer sellNum;

    /**
     * 买入次数
     */
    @ApiModelProperty("买入次数")
    private Integer buyNum;

    /**
     * 买入成功率
     */
    @ApiModelProperty("买入成功率")
    private BigDecimal buySuccessRate;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}
