package org.uu.common.pay.req;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author
 */
@Data
@ApiModel(description = "USDT配置信息参数")
public class UsdtConfigCreateReq implements Serializable {


    /**
     * 网络歇息
     */
    @ApiModelProperty("网络协议")
    private String networkProtocol;

    /**
     * usdt地址
     */
    @ApiModelProperty("usdt地址")
    private String usdtAddr;


    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;


    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;


}