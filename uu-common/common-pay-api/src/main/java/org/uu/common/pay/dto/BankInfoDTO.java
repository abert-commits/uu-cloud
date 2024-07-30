package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 银行表
 * </p>
 *
 * @author 
 * @since 2024-06-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bank_info")
public class BankInfoDTO {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "id")
    private Long id;
    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;

    /**
     * ifsc_code
     */
    @ApiModelProperty(value = "ifsc_code")
    private String ifscCode;

    /**
     * 删除表示: 0未删除，1已删除
     */
    @ApiModelProperty(value = "删除")
    private Integer deleted;

    /**
     * 银行代码
     */
    @ApiModelProperty(value = "银行代码")
    private String bankCode;

    /**
     * 银行logo
     */
    @ApiModelProperty(value = "银行logo")
    private String iconUrl;

    /**
     * 状态, 0: 关闭, 1: 启用
     */
    @ApiModelProperty(value = "状态")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "更新人")
    private String updateBy;
}
