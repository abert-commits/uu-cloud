package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 邀请链接表
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("t_invite_link")
public class InviteLink implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 邀请链接ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 邀请链接标题
     */
    private String title;

    /**
     * 蚂蚁ID
     */
    private Long antId;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 是否默认邀请链接  0-是 -1-否
     */
    private Integer defaultLink;

    /**
     * 树标识(根会员ID)
     */
    private Long treeFlag;

    /**
     * 邀请码所在等级
     */
    private Integer level;

    /**
     * 删除状态 0-未删除 1-删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
