package org.uu.wallet.req;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.common.core.page.PageRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("usdt_config")
public class UsdtConfigReq extends PageRequest {
    private long id;

    /**
     * 网络歇息
     */
    private String networkProtocol;

    /**
     * usdt地址
     */
    private String usdtAddr;

    /**
     * 创建人
     */
    private String createBy;


    /**
     * 状态
     */
    private String status;


    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 备注
     */
    private String remark;


}