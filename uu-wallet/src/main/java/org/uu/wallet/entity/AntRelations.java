package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 蚂蚁关系表
 * </p>
 *
 * @author Parker
 * @since 2024-06-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("r_ant_relations")
public class AntRelations implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 蚂蚁ID
     */
    @TableId(value = "ant_id")
    private Long antId;

    /**
     * 左值
     */
    private Integer leftValue;

    /**
     * 右值
     */
    private Integer rightValue;

    /**
     * 蚂蚁层级 0-顶级
     */
    private Integer antLevel;

    /**
     * 树标识(根结点蚂蚁ID)
     */
    private Long treeFlag;
}
