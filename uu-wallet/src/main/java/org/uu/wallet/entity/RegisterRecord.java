package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 注册记录表
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
@TableName("t_register_record")
public class RegisterRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 蚂蚁ID
     */
    @TableId(value = "ant_id")
    private Long antId;

    /**
     * 注册IP
     */
    private String registerIp;

    /**
     * 注册邀请码
     */
    private String inviteCode;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;
}
