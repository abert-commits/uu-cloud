package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_bind_bot")
public class MerchantBindBot extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户编码
     */
    private String merchantCode;

    /**
     * 群组id
     */
    private Long groupId;

    /**
     * 群组名称
     */
    private String groupName;

    /**
     * 删除表示: 0未删除，1已删除
     */
    private Integer deleted;

}