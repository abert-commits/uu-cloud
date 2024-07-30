package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会员黑名单
 * </p>
 *
 * @author 
 * @since 2024-03-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_black")
public class MemberBlack implements Serializable {


    @TableId(type = IdType.AUTO)
    private Long id;

    private static final long serialVersionUID = 1L;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 分组 默认值: 1(默认分组)
     */
    private Long memberGroup = 1L;


    /**
     * 会员类型
     */
    private String memberType;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 在线状态
     */
    private Integer onlineStatus;

    /**
     * 买入状态
     */
    private Integer buyStatus;

    /**
     * 卖出状态
     */
    private Integer sellStatus;

    /**
     * 备注
     */
    private String remark;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 商户会员ID
     */
    private String merchantMemberId;

    /**
     * 操作时间
     */
    private LocalDateTime opTime;


}
