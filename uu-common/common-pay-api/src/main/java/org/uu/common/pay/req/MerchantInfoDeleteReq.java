package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.common.core.page.PageRequest;

import java.math.BigDecimal;


/**
 * @author
 */
@Data
@ApiModel(description = "商户请求参数说明")
public class MerchantInfoDeleteReq {



    @ApiModelProperty(value = "主键")
    private Long id;


    @ApiModelProperty(value = "appid就是商户号")
    private String code;



}