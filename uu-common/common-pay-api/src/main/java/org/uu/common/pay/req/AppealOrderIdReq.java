package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 申诉订单
 *
 * @author
 */
@Data
@ApiModel(description ="申诉处理参数")
public class AppealOrderIdReq implements Serializable {
   @ApiModelProperty("主键")
   private Long id;







}