package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@Accessors(chain = true)
@TableName("member_group")
public class MemberGroup implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 金额
     */
    private Integer sellCount;

    /**
     * 金额
     */
    private BigDecimal buyAmount;

    /**
     * 买入次数
     */
    private Integer buyCount;

    /**
     * 会员数量
     */
    @TableField(exist = false)
    private Long memberGroupCount;

    /**
     * 卖出金额
     */
    private BigDecimal sellAmount;

    /**
     * 会员数量
     */
    private Integer memberCount;


    private BigDecimal betAmount;

    /**
     * 授权列表
     */
    private String authList;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;


}