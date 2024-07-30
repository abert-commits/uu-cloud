package org.uu.manager.req;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author admin
 * @date 2024/5/7 11:21
 */
@Data
@ApiModel("站内信请求参数")
public class SysMessageSendReq {
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
}
