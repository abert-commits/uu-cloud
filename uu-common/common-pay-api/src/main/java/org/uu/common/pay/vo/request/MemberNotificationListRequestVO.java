package org.uu.common.pay.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequestHome;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("消息通知列表请求实体类")
@EqualsAndHashCode(callSuper = true)
public class MemberNotificationListRequestVO extends PageRequestHome implements Serializable {
    private static final long serialVersionUID = -7289231802131097912L;

    @ApiModelProperty("消息类型 1-交易通知 2-其他 默认1")
    private Integer notificationType = 1;

    @ApiModelProperty("是否阅读 0: 未阅读 1: 已阅读 -1/null:全部   默认全部")
    private Integer readFlag = -1;
}
