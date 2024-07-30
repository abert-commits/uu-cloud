package org.uu.wallet.req;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 充值取消原因
*
* @author 
*/
    @Data
    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public class CancellationRechargeReq extends PageRequest {

     private long id;
            /**
            * 原因
            */
    private String reason;

            /**
            * 排序
            */
    private Integer sort;

            /**
            * 创建时间
            */
    private LocalDateTime createTime;

            /**
            * 创建人
            */
    private String createBy;

            /**
            * 更新时间
            */
    private LocalDateTime updateTime;

            /**
            * 修改人
            */
    private String updateBy;


}