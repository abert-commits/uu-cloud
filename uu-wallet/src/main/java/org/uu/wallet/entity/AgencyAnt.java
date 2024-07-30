package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 蚂蚁-代理关系表
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("r_agency_ant")
public class AgencyAnt implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 蚂蚁ID
     */
    @TableId(value = "ant_id")
    private Long antId;

    /**
     * 代理ID
     */
    private Long agencyId;
}
