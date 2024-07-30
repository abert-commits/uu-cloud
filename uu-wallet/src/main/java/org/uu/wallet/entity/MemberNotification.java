package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员通知
 *
 * @author
 */
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("member_notification")
public class MemberNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 消息类型 1-交易通知 2-其他
     */
    private Integer notificationType;

    /**
     * 内容  通知类型为1则表示订单号
     */
    private String content;

    /**
     * 是否阅读 0未阅读 1已阅读
     */
    private Integer readFlag;

    /**
     * 订单类型 1-买入 2-卖出 3-USDT充值 0-其他
     */
    private Integer orderType;

    /**
     * 订单状态  3-待支付 7-已完成 8-已取消
     */
    private Integer orderStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
