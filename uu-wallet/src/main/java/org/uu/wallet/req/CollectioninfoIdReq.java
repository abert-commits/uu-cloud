package org.uu.wallet.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author
 */
@Data
public class CollectioninfoIdReq {

    /**
     * 收款信息id
     */
    @ApiModelProperty(value = "收款信息id")
    @NotNull(message = "Payment information id cannot be empty")
    @Min(value = 0, message = "The payment information id format is incorrect")
    private Long collectionInfoId;
}