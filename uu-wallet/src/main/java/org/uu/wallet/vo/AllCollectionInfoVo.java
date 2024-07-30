package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "收款信息")
public class AllCollectionInfoVo implements Serializable {

    @ApiModelProperty(value = "UPI收款信息列表")
    private List<UpiCollectionInfoVo> upiCollectionInfos;

    @ApiModelProperty(value = "银行卡收款信息列表")
    private List<BankCardCollectionInfoVo> bankCardCollectionInfos;
}