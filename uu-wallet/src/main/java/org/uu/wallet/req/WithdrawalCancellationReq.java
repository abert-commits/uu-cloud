package org.uu.wallet.req;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 提现取消原因配置表
*
* @author 
*/
    @Data
    public class WithdrawalCancellationReq extends PageRequest {
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
            * 修改时间
            */
    private LocalDateTime updateTime;

            /**
            * 修改人
            */
    private String updateBy;


}