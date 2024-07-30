package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 银行表
 * </p>
 *
 * @author 
 * @since 2024-06-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bank_info")
public class BankInfo extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 银行名称
     */
    private String bankName;

    /**
     * 银行代码
     */
    private String bankCode;

    /**
     * ifsc_code
     */
    private String ifscCode;

    /**
     * 删除表示: 0未删除，1已删除
     */
    private Integer deleted;

    /**
     * 银行logo
     */
    private String iconUrl;

    /**
     * 状态, 0: 关闭, 1: 启用
     */
    private Integer status;


}
