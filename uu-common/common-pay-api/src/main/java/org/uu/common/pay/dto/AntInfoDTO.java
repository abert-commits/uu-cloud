package org.uu.common.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AntInfoDTO implements Serializable {
    private static final long serialVersionUID = 5165280247727062917L;

    /**
     * 蚂蚁ID
     */
    private Long id;

    /**
     * 蚂蚁名称
     */
    private String antName;

    /**
     * 蚂蚁号码
     */
    private String phoneNumber;

    /**
     * 蚂蚁登录密码
     */
    private String password;

    /**
     * 注册邀请码
     */
    private String registerInviteCode;

    /**
     * 注册邀请人ID
     */
    private Long registerInviteId;
}
