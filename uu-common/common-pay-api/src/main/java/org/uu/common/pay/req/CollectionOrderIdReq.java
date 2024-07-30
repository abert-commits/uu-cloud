package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description ="买入订单支付接口请求参数")
public class CollectionOrderIdReq implements Serializable {


    @ApiModelProperty("主键")
    private Long id;



    /**
     * 实际金额
     */
    @ApiModelProperty("实际金额")
    private BigDecimal actualAmount;
    @ApiModelProperty("手动完成人")
    private String completedBy;

}