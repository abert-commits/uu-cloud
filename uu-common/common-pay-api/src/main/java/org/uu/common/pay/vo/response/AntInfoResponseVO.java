package org.uu.common.pay.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "蚂蚁信息响应实体")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AntInfoResponseVO implements Serializable {
    private static final long serialVersionUID = -9142671004973661639L;

    /**
     * 蚂蚁ID
     */
    private Long id;

    /**
     * 蚂蚁昵称
     */
    private String nickname;

    /**
     * 蚂蚁名称
     */
    private String antName;

    /**
     * 蚂蚁号码
     */
    private String phoneNumber;

    /**
     * 蚂蚁邮箱
     */
    private String email;

    /**
     * 蚂蚁状态 0-正常 1-冻结 2-限制交易
     */
    private Integer userStatus;

    /**
     * 蚂蚁头像
     */
    private String avatar;

    /**
     * 代理层级 1-顶级 2-二级 …
     */
    private Integer agencyLevel;

    /**
     * 绑定电报
     */
    private String bindTelegram;

    /**
     * 佣金比例(%)
     */
    private BigDecimal commissionRatio;

    /**
     * 蚂蚁信息创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 蚂蚁信息更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
