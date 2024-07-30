package org.uu.common.pay.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "批量归集地址请求参数")
public class BatchCollectAddrReq {


    /**
     * 地址列表
     */
    @ApiModelProperty("地址列表")
    @NotEmpty(message = "usdtAddressList cannot be empty")
    private List<UsdtAddress> usdtAddressList;


    @Data
    public static class UsdtAddress {
        private String usdtAddress;
        private BigDecimal usdtBalance;
        private BigDecimal trxBalance;

    }
}