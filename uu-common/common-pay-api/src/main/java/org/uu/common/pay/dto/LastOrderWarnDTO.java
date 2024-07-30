package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author admin
 * @date 2024/4/29 16:51
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "商户最后一笔订单发生时间过久告警")
public class LastOrderWarnDTO {
    private String merchantName;

    private LocalDateTime lastOrderCreateTime;
}
