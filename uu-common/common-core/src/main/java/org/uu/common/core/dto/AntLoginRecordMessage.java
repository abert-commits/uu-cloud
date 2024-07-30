package org.uu.common.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AntLoginRecordMessage implements Serializable {
    private static final long serialVersionUID = 8513385138330219151L;

    private Long antId;

    private String loginIp;

    private Integer loginType = 2;

    private LocalDateTime loginTime;
}
