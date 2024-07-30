package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2024-04-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "订单监控参数")
public class OrderMonitorReq implements Serializable {



    /**
     * 代码
     */
    @ApiModelProperty(value = "编码")
    private String code;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "完成开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;




}
