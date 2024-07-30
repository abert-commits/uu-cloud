package org.uu.wallet.req;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* c2c配置信息表
*
* @author 
*/
    @Data
    public class C2cConfigReq extends PageRequest {

        private long id;


            /**
            * 充值奖励比例
            */
    private BigDecimal rechargeRewardRatio;

            /**
            * 提现奖励比例
            */
    private BigDecimal withdrawalRewardRatio;

            /**
            * 充值过期时间
            */
    private Integer rechargeExpirationTime;

            /**
            * 提现过期时间
            */
    private Integer withdrawalExpirationTime;

            /**
            * 确认超时时间
            */
    private Integer confirmExpirationTime;

            /**
            * 失败次数
            */
    private Integer numberFailures;

            /**
            * 禁用时间
            */
    private Integer disabledTime;


}