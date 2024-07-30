package org.uu.manager.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2024-05-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_message")
public class SysMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送人
     */
    private String messageFrom;

    /**
     * 接收人
     */
    private String messageTo;

    /**
     * 消息类型 1-系统消息
     */
    private Integer messageType;

    /**
     * 消息
     */
    private String messageContent;

    /**
     * 消息阅读时间
     */
    private LocalDateTime messageReadTime;

    /**
     * 消息状态 0-未读 1-已读
     */
    private Integer messageStatus;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    private Integer deleted;


}
