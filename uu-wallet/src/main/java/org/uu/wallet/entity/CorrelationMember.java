package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 关联会员信息
 * </p>
 *
 * @author 
 * @since 2024-03-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("correlation_member")
public class CorrelationMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 商户会员id
     */
    private String merchantMemberId;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 会员类型
     */
    private Integer memberType;

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
     * 所属分组
     */
    private Long memberGroup;

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
     * 真实姓名
     */
    private String realName;

    /**
     * 关联IP
     */
    private String relationsIp;


}
