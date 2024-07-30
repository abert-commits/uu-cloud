package org.uu.wallet.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(description = "查询下拉列表数据(币种,支付类型)")
public class selectListVo implements Serializable {

    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private List<JSONObject> currency;

    /**
     * 支付类型
     */
    @ApiModelProperty(value = "支付类型")
    private List<JSONObject> payType;
}
