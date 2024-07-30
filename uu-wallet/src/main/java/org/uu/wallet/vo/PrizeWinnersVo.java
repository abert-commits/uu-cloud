package org.uu.wallet.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 */
@Data
@ApiModel(description = "领奖会员信息")
public class PrizeWinnersVo implements Serializable {


    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;


    /**
     * 奖励类型
     */
    @ApiModelProperty("奖励类型")
    private String taskType;


    /**
     * 奖励金额
     */
    @ApiModelProperty("奖励金额")
    private BigDecimal rewardAmount;

}